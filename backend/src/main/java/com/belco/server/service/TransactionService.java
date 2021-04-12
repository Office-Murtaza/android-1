package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.entity.*;
import com.belco.server.model.*;
import com.belco.server.repository.TransactionRecordRep;
import com.belco.server.repository.TransactionRecordWalletRep;
import com.belco.server.repository.UserCoinRep;
import com.belco.server.util.Util;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@EnableScheduling
public class TransactionService {

    private static final String TERMINAL_SERIAL_NUMBER = "BT100872";
    private static final Pageable page = PageRequest.of(0, 100);

    private final TransactionRecordRep recordRep;
    private final TransactionRecordWalletRep walletRep;
    private final UserCoinRep userCoinRep;
    private final UserService userService;
    private final TwilioService twilioService;
    private final NotificationService notificationService;
    private final WalletService walletService;
    private final GethService gethService;
    private final PlatformService platformService;
    private final SocketService socketService;
    private final MongoTemplate mongo;

    @Value("${gb.url}")
    private String gbUrl;

    public TransactionService(TransactionRecordRep recordRep, TransactionRecordWalletRep walletRep, UserCoinRep userCoinRep, UserService userService, TwilioService twilioService, NotificationService notificationService, WalletService walletService, GethService gethService, PlatformService platformService, SocketService socketService, MongoTemplate mongo) {
        this.recordRep = recordRep;
        this.walletRep = walletRep;
        this.userCoinRep = userCoinRep;
        this.userService = userService;
        this.twilioService = twilioService;
        this.notificationService = notificationService;
        this.walletService = walletService;
        this.gethService = gethService;
        this.platformService = platformService;
        this.socketService = socketService;
        this.mongo = mongo;
    }

    public static TxHistoryDTO buildTxs(Map<String, TxDetailsDTO> map, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionRecordWallet> transactionRecordWallets) {
        mergeTransactionRecords(map, transactionRecords);
        mergeTransactionRecordWallets(map, transactionRecordWallets);

        List<TxDetailsDTO> list = convertAndSort(map);

        int fromIndex = (startIndex - 1) * limit;
        int toIndex = Math.min(list.size(), (startIndex - 1) * limit + limit);

        if (fromIndex <= list.size()) {
            return new TxHistoryDTO(list.size(), list.subList(fromIndex, toIndex));
        } else {
            return new TxHistoryDTO(list.size(), new ArrayList<>());
        }
    }

    private static void mergeTransactionRecordWallets(Map<String, TxDetailsDTO> map, List<TransactionRecordWallet> list) {
        if (list != null && !list.isEmpty()) {
            list.stream().forEach(e -> {
                if (map.containsKey(e.getTxId())) {
                    TransactionType type = map.get(e.getTxId()).getType();
                    map.get(e.getTxId()).setType(TransactionType.convert(type, TransactionType.valueOf(e.getType())));
                }
            });
        }
    }

    private static void mergeTransactionRecords(Map<String, TxDetailsDTO> map, List<TransactionRecord> txList) {
        if (txList != null && !txList.isEmpty()) {
            txList.stream().forEach(e -> {
                TransactionType type = e.getTransactionType();
                TransactionStatus status = e.getTransactionStatus(type);

                if (org.apache.commons.lang.StringUtils.isNotBlank(e.getDetail()) && map.containsKey(e.getDetail())) {
                    map.get(e.getDetail()).setType(type);
                    map.get(e.getDetail()).setStatus(status);
                } else {
                    Long txDBId = e.getId();
                    map.put(txDBId.toString(), new TxDetailsDTO(txDBId, Util.format(e.getCryptoAmount(), 6), type, status, e.getServerTime().getTime()));
                }
            });
        }
    }

    private static List<TxDetailsDTO> convertAndSort(Map<String, TxDetailsDTO> map) {
        if (!map.isEmpty()) {
            List<TxDetailsDTO> list = new ArrayList<>(map.values());
            list.sort(Comparator.comparing(TxDetailsDTO::getTimestamp).reversed());

            return list;
        }

        return new ArrayList<>();
    }

    public TxDetailsDTO getTransactionDetails(Long userId, CoinService.CoinEnum coinCode, String txId) {
        User user = userService.findById(userId);
        Identity identity = user.getIdentity();
        Coin coin = coinCode.getCoinEntity();

        TxDetailsDTO dto = new TxDetailsDTO();
        Optional<TransactionRecord> buySellRecOpt;

        if (org.apache.commons.lang.StringUtils.isNumeric(txId)) {  /** consider as txDbId */
            buySellRecOpt = recordRep.findById(Long.valueOf(txId));
        } else {                                                    /** consider as txId */
            String address = user.getUserCoin(coinCode.name()).getAddress();
            dto = coinCode.getTransactionDetails(txId, address);
            buySellRecOpt = recordRep.findOneByIdentityAndDetailAndCryptoCurrency(user.getIdentity(), txId, coinCode.name());
        }

        Optional<TransactionRecordWallet> giftRecOpt = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.SEND_TRANSFER.getValue(), TransactionType.RECEIVE_TRANSFER.getValue()));
        Optional<TransactionRecordWallet> swapRecOpt = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.SEND_SWAP.getValue(), TransactionType.RECEIVE_SWAP.getValue()));
        Optional<TransactionRecordWallet> stakeRecOpt = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.CREATE_STAKE.getValue(), TransactionType.CANCEL_STAKE.getValue(), TransactionType.WITHDRAW_STAKE.getValue()));
        Optional<TransactionRecordWallet> reserveRecOpt = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.RESERVE.getValue(), TransactionType.RECALL.getValue()));

        if (giftRecOpt.isPresent()) {
            TransactionRecordWallet gift = giftRecOpt.get();

            if (gift.getType() == TransactionType.SEND_TRANSFER.getValue()) {
                dto.setToPhone(gift.getToPhone());
            } else if (gift.getType() == TransactionType.RECEIVE_TRANSFER.getValue()) {
                dto.setFromPhone(gift.getFromPhone());
            }

            dto.setImageId(gift.getImageId());
            dto.setMessage(gift.getMessage());
            dto.setType(TransactionType.convert(dto.getType(), TransactionType.valueOf(gift.getType())));
        } else if (swapRecOpt.isPresent()) {
            TransactionRecordWallet exchange = swapRecOpt.get();

            String code = exchange.getRefCoin().getCode();
            dto.setRefCoin(code);
            dto.setRefTxId(exchange.getRefTxId());
            dto.setRefLink(CoinService.CoinEnum.valueOf(code).getExplorerUrl() + "/" + exchange.getRefTxId());
            dto.setRefCryptoAmount(exchange.getRefAmount());
            dto.setType(TransactionType.convert(dto.getType(), TransactionType.valueOf(exchange.getType())));
        } else if (buySellRecOpt.isPresent()) {
            TransactionRecord buySell = buySellRecOpt.get();

            //return txId or txDBId
            if (StringUtils.isBlank(dto.getTxId())) {
                if (StringUtils.isNotBlank(buySell.getDetail())) {
                    dto.setTxId(buySell.getDetail());
                } else {
                    dto.setTxDBId(buySell.getId());
                }
            }

            dto.setType(buySell.getTransactionType());
            dto.setStatus(buySell.getTransactionStatus(dto.getType()));
            dto.setCryptoAmount(buySell.getCryptoAmount().stripTrailingZeros());
            dto.setFiatAmount(buySell.getCashAmount().setScale(0));
            dto.setToAddress(buySell.getCryptoAddress());
            dto.setTimestamp(buySell.getServerTime().getTime());

            if (dto.getType() == TransactionType.SELL) {
                dto.setCashStatus(CashStatus.getCashStatus(buySell.getCanBeCashedOut(), buySell.getWithdrawn()));
                dto.setSellInfo(coin.getName() + ":" + buySell.getCryptoAddress()
                        + "?amount=" + buySell.getCryptoAmount()
                        + "&label=" + buySell.getRemoteTransactionId()
                        + "&uuid=" + buySell.getUuid());
            }
        } else if (stakeRecOpt.isPresent()) {
            dto.setType(TransactionType.valueOf(stakeRecOpt.get().getType()));
        } else if (reserveRecOpt.isPresent()) {
            dto.setType(TransactionType.valueOf(reserveRecOpt.get().getType()));
        }

        return dto;
    }

    public TxHistoryDTO getTransactionHistory(Long userId, CoinService.CoinEnum coinCode, Integer startIndex) {
        User user = userService.findById(userId);
        Identity identity = user.getIdentity();
        Coin coin = coinCode.getCoinEntity();
        String address = user.getUserCoin(coinCode.name()).getAddress();

        List<TransactionRecord> transactionRecords = recordRep.findAllByIdentityAndCryptoCurrency(user.getIdentity(), coinCode.name());
        List<TransactionRecordWallet> transactionRecordWallets = walletRep.findAllByIdentityAndCoin(identity, coin);

        return coinCode.getTransactionHistory(address, startIndex, 10, transactionRecords, transactionRecordWallets);
    }

    public String submit(Long userId, CoinService.CoinEnum coin, TxSubmitDTO dto) throws InterruptedException {
        String txId;

        if (TransactionType.RECALL.getValue() == dto.getType()) {
            txId = recall(userId, coin, dto);
        } else {
            txId = coin.submitTransaction(dto);

            Thread.sleep(2000);
        }

        if (StringUtils.isNotBlank(txId) && coin.isTransactionSeenOnBlockchain(txId)) {
            if (TransactionType.SEND_TRANSFER.getValue() == dto.getType()) {
                persistTransfer(userId, coin, txId, dto);
            }

            if (TransactionType.SEND_SWAP.getValue() == dto.getType()) {
                swap(userId, coin, txId, dto);
            }

            if (TransactionType.RESERVE.getValue() == dto.getType()) {
                reserve(userId, coin, txId, dto);
            }

            if (TransactionType.CREATE_STAKE.getValue() == dto.getType()) {
                createStake(userId, coin, txId, dto);
            }

            if (TransactionType.CANCEL_STAKE.getValue() == dto.getType()) {
                cancelStake(userId, coin, txId, dto);
            }

            if (TransactionType.WITHDRAW_STAKE.getValue() == dto.getType()) {
                withdrawStake(userId, coin, txId, dto);
            }

            postSubmit(userId, coin, txId);
        }

        return txId;
    }

    public void persistTransfer(Long userId, CoinService.CoinEnum coinCode, String txId, TxSubmitDTO dto) {
        try {
            User user = userService.findById(userId);
            Optional<User> receiverOpt = userService.findByPhone(dto.getPhone());
            Coin coin = userService.getUserCoin(userId, coinCode.name()).getCoin();

            //TODO remove
                TransactionRecordWallet sendRecord = new TransactionRecordWallet();
                sendRecord.setTxId(txId);
                sendRecord.setType(TransactionType.SEND_TRANSFER.getValue());
                sendRecord.setAmount(dto.getCryptoAmount());
                sendRecord.setStatus(TransactionStatus.PENDING.getValue());
                sendRecord.setFromPhone(user.getPhone());
                sendRecord.setToPhone(dto.getPhone());
                sendRecord.setMessage(dto.getMessage());
                sendRecord.setImageId(dto.getImage());
                sendRecord.setReceiverStatus(receiverOpt.isPresent() ? TransactionRecordWallet.RECEIVER_EXIST : TransactionRecordWallet.RECEIVER_NOT_EXIST);
                sendRecord.setIdentity(user.getIdentity());
                sendRecord.setCoin(coin);
                walletRep.save(sendRecord);

            TxDetailsDTO tx = new TxDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setCoin(coinCode.name());
            tx.setType(TransactionType.SEND_TRANSFER);
            tx.setStatus(TransactionStatus.PENDING);
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            tx.setFromPhone(user.getPhone());
            tx.setToPhone(dto.getPhone());
            tx.setMessage(dto.getMessage());
            tx.setImageId(dto.getImage());
            tx.setProcessed(receiverOpt.isPresent() ? ProcessedType.COMPLETE : ProcessedType.PENDING);
            mongo.save(tx);


                if (receiverOpt.isPresent()) {
                    //TODO remove
                    TransactionRecordWallet receiveRecord = new TransactionRecordWallet();
                    receiveRecord.setTxId(sendRecord.getTxId());
                    receiveRecord.setType(TransactionType.RECEIVE_TRANSFER.getValue());
                    receiveRecord.setStatus(TransactionStatus.PENDING.getValue());
                    receiveRecord.setFromPhone(sendRecord.getFromPhone());
                    receiveRecord.setToPhone(sendRecord.getToPhone());
                    receiveRecord.setMessage(sendRecord.getMessage());
                    receiveRecord.setImageId(sendRecord.getImageId());
                    receiveRecord.setReceiverStatus(TransactionRecordWallet.RECEIVER_EXIST);
                    receiveRecord.setIdentity(receiverOpt.get().getIdentity());
                    receiveRecord.setCoin(sendRecord.getCoin());
                    receiveRecord.setAmount(sendRecord.getAmount());
                    walletRep.save(receiveRecord);

                    TxDetailsDTO tx2 = new TxDetailsDTO();
                    tx2.setUserId(userId);
                    tx2.setTxId(txId);
                    tx2.setCoin(coinCode.name());
                    tx2.setType(TransactionType.RECEIVE_TRANSFER);
                    tx2.setStatus(TransactionStatus.PENDING);
                    tx2.setCryptoAmount(dto.getCryptoAmount());
                    tx2.setFromAddress(dto.getFromAddress());
                    tx2.setToAddress(dto.getToAddress());
                    tx2.setFromPhone(user.getPhone());
                    tx2.setToPhone(dto.getPhone());
                    tx2.setMessage(dto.getMessage());
                    tx2.setImageId(dto.getImage());
                    mongo.save(tx2);

                    String token = receiverOpt.get().getNotificationsToken();
                    StringBuilder messageBuilder = new StringBuilder("You just received " + dto.getCryptoAmount().stripTrailingZeros() + " " + coinCode.name());

                    if (StringUtils.isNotBlank(dto.getMessage())) {
                        messageBuilder.append("\n\n").append("\"").append(dto.getMessage()).append("\"").append("\n");
                    }

                    notificationService.sendMessageWithData(new NotificationDTO("New incoming transfer", messageBuilder.toString(), null, token));
                } else {
                    twilioService.sendTransferMessageToNotExistingUser(coinCode, dto.getPhone(), dto.getMessage(), dto.getImage(), dto.getCryptoAmount().stripTrailingZeros());
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserLimitDTO getLimits(Long userId) {
        UserLimitDTO dto = new UserLimitDTO();
        dto.setDailyLimit(BigDecimal.ZERO);
        dto.setTxLimit(BigDecimal.ZERO);
        dto.setSellProfitRate(BigDecimal.ONE);

        try {
            User user = userService.findById(userId);
            BigDecimal txAmount = recordRep.getTransactionsSumByDate(user.getIdentity(), Util.getStartDate(), new Date());

            // find latest limits
            BigDecimal dailyLimit = user.getIdentity().getLimitCashPerDay().stream()
                    .sorted(Comparator.comparingLong(Limit::getId).reversed())
                    .findFirst().get().getAmount();
            BigDecimal txLimit = user.getIdentity().getLimitCashPerTransaction().stream()
                    .sorted(Comparator.comparingLong(Limit::getId).reversed())
                    .findFirst().get().getAmount();

            if (txAmount != null) {
                dailyLimit.subtract(txAmount);
            }

            dto.setDailyLimit(Util.format(dailyLimit, 2));
            dto.setTxLimit(Util.format(txLimit, 2));
            dto.setSellProfitRate(new BigDecimal("1.05"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public PreSubmitDTO preSubmit(Long userId, CoinService.CoinEnum coinId, TxSubmitDTO transaction) {
        PreSubmitDTO dto = new PreSubmitDTO();

        try {
            User user = userService.findById(userId);

            StringBuilder params = new StringBuilder();
            params.append("?serial_number=").append(TERMINAL_SERIAL_NUMBER);
            params.append("&fiat_amount=").append(transaction.getFiatAmount());
            params.append("&fiat_currency=").append(transaction.getFiatCurrency());
            params.append("&crypto_amount=").append(transaction.getCryptoAmount());
            params.append("&crypto_currency=").append(coinId.name());
            params.append("&identity_public_id=").append(user.getIdentity().getPublicId());

            JSONObject res = Util.insecureRequest(gbUrl + "/extensions/example/sell_crypto" + params.toString());

            dto.setAddress(res.optString("cryptoAddress"));
            dto.setCryptoAmount(BigDecimal.valueOf(res.optDouble("cryptoAmount")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public void swap(Long userId, CoinService.CoinEnum coin, String txId, TxSubmitDTO dto) {
        try {
            //TODO remove
            CoinService.CoinEnum refCoin = CoinService.CoinEnum.valueOf(dto.getRefCoin());
            TransactionRecordWallet record = new TransactionRecordWallet();
            record.setTxId(txId);
            record.setIdentity(userService.findById(userId).getIdentity());
            record.setCoin(coin.getCoinEntity());
            record.setAmount(dto.getCryptoAmount());
            record.setType(TransactionType.SEND_SWAP.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setProfitPercent(platformService.getPlatformSwapFee());
            record.setRefCoin(refCoin.getCoinEntity());
            record.setRefAmount(dto.getRefCryptoAmount());
            walletRep.save(record);

            TxDetailsDTO tx = new TxDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.SEND_SWAP);
            tx.setStatus(TransactionStatus.PENDING);
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            tx.setRefCoin(dto.getRefCoin());
            tx.setRefCryptoAmount(dto.getRefCryptoAmount());
            tx.setProcessed(ProcessedType.PENDING);
            mongo.save(tx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reserve(Long userId, CoinService.CoinEnum coin, String txId, TxSubmitDTO dto) {
        try {
            //TODO: remove
            TransactionRecordWallet record = new TransactionRecordWallet();
            record.setTxId(txId);
            record.setIdentity(userService.findById(userId).getIdentity());
            record.setCoin(coin.getCoinEntity());
            record.setAmount(dto.getCryptoAmount());
            record.setType(TransactionType.RESERVE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            walletRep.save(record);

            TxDetailsDTO tx = new TxDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.RESERVE);
            tx.setStatus(TransactionStatus.PENDING);
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            tx.setProcessed(ProcessedType.PENDING);
            mongo.save(tx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String recall(Long userId, CoinService.CoinEnum coinCode, TxSubmitDTO dto) {
        try {
            UserCoin userCoin = userService.getUserCoin(userId, coinCode.name());
            BigDecimal reserved = userCoin.getReservedBalance();
            BigDecimal txFee = coinCode == CoinService.CoinEnum.CATM || coinCode == CoinService.CoinEnum.USDC ? walletService.convertToFee(coinCode) : coinCode.getTxFee();
            String walletAddress = coinCode.getWalletAddress();

            if (walletService.isEnoughBalance(coinCode, walletAddress, dto.getCryptoAmount()) && reserved.compareTo(dto.getCryptoAmount().add(txFee)) >= 0) {
                String fromAddress = coinCode.getWalletAddress();
                String toAddress = userCoin.getAddress();
                String hex = coinCode.sign(fromAddress, toAddress, dto.getCryptoAmount());

                TxSubmitDTO submit = new TxSubmitDTO();
                submit.setHex(hex);
                submit.setFromAddress(fromAddress);
                submit.setToAddress(toAddress);
                submit.setCryptoAmount(dto.getCryptoAmount());

                String txId = coinCode.submitTransaction(submit);

                if (StringUtils.isNotBlank(txId)) {
                    //TODO remove
                    TransactionRecordWallet record = new TransactionRecordWallet();
                    record.setTxId(txId);
                    record.setIdentity(userService.findById(userId).getIdentity());
                    record.setCoin(coinCode.getCoinEntity());
                    record.setAmount(dto.getCryptoAmount());
                    record.setType(TransactionType.RECALL.getValue());
                    record.setStatus(TransactionStatus.PENDING.getValue());
                    walletRep.save(record);

                    TxDetailsDTO tx = new TxDetailsDTO();
                    tx.setUserId(userId);
                    tx.setTxId(txId);
                    tx.setCoin(coinCode.name());
                    tx.setType(TransactionType.RECALL);
                    tx.setStatus(TransactionStatus.PENDING);
                    tx.setCryptoAmount(dto.getCryptoAmount());
                    tx.setFromAddress(fromAddress);
                    tx.setToAddress(toAddress);
                    mongo.save(tx);

                    userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(dto.getCryptoAmount().add(txFee)));
                    userCoinRep.save(userCoin);

                    return txId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void createStake(Long userId, CoinService.CoinEnum coin, String txId, TxSubmitDTO dto) {
        try {
            //TODO remove
            TransactionRecordWallet record = new TransactionRecordWallet();
            record.setIdentity(userService.findByUserId(userId));
            record.setCoin(coin.getCoinEntity());
            record.setType(TransactionType.CREATE_STAKE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setAmount(dto.getCryptoAmount());
            record.setTxId(txId);
            walletRep.save(record);

            TxDetailsDTO tx = new TxDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.CREATE_STAKE);
            tx.setStatus(TransactionStatus.PENDING);
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            mongo.save(tx);

            //TODO map record to stake collection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelStake(Long userId, CoinService.CoinEnum coin, String txId, TxSubmitDTO dto) {
        try {
            //TODO remove
            TransactionRecordWallet record = new TransactionRecordWallet();
            Identity identity = userService.findByUserId(userId);
            record.setIdentity(identity);
            record.setCoin(coin.getCoinEntity());
            record.setType(TransactionType.CANCEL_STAKE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setAmount(dto.getCryptoAmount());
            record.setTxId(txId);
            walletRep.save(record);

            Optional<TransactionRecordWallet> createStakeRecOpt = walletRep.findFirstByIdentityAndCoinAndTypeAndStatusOrderByCreateDateDesc(identity, coin.getCoinEntity(), TransactionType.CREATE_STAKE.getValue(), TransactionStatus.COMPLETE.getValue());

            TransactionRecordWallet previousTransaction = linkPreviousTransaction(txId, createStakeRecOpt);
            if (previousTransaction != null) {
                walletRep.save(previousTransaction);
            }

            TxDetailsDTO tx = new TxDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.CANCEL_STAKE);
            tx.setStatus(TransactionStatus.PENDING);
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            mongo.save(tx);

            //TODO map record to stake collection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void withdrawStake(Long userId, CoinService.CoinEnum coin, String txId, TxSubmitDTO dto) {
        try {
            TransactionRecordWallet record = new TransactionRecordWallet();
            Identity identity = userService.findByUserId(userId);
            record.setIdentity(identity);
            record.setCoin(coin.getCoinEntity());
            record.setType(TransactionType.WITHDRAW_STAKE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setAmount(dto.getCryptoAmount());
            record.setTxId(txId);

            walletRep.save(record);

            Optional<TransactionRecordWallet> cancelStakeRecOpt = walletRep.findFirstByIdentityAndCoinAndTypeAndStatusOrderByCreateDateDesc(identity, coin.getCoinEntity(), TransactionType.CANCEL_STAKE.getValue(), TransactionStatus.COMPLETE.getValue());

            TransactionRecordWallet previousTransaction = linkPreviousTransaction(txId, cancelStakeRecOpt);
            if (previousTransaction != null) {
                walletRep.save(previousTransaction);
            }

            TxDetailsDTO tx = new TxDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.CANCEL_STAKE);
            tx.setStatus(TransactionStatus.PENDING);
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            mongo.save(tx);

            //TODO map record to stake collection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StakingDetailsDTO getStakeDetails(Long userId, CoinService.CoinEnum coin) {
        StakingDetailsDTO dto = new StakingDetailsDTO();
        dto.setStatus(StakingStatus.NOT_EXIST);
        dto.setAnnualPercent(gethService.getStakingAnnualPercent());
        dto.setHoldPeriod(gethService.getStakingHoldPeriod() / gethService.getStakingBasePeriod());

        try {
            Identity identity = userService.findByUserId(userId);
            Optional<TransactionRecordWallet> createStakeRecOpt = walletRep.findFirstByIdentityAndCoinAndTypeAndStatusNotOrderByCreateDateDesc(identity, coin.getCoinEntity(), TransactionType.CREATE_STAKE.getValue(), TransactionStatus.FAIL.getValue());

            if (createStakeRecOpt.isPresent()) {
                TransactionRecordWallet createStakeRec = createStakeRecOpt.get();

                dto.setStatus(StakingStatus.convert(TransactionType.valueOf(createStakeRec.getType()), TransactionStatus.valueOf(createStakeRec.getStatus())));
                dto.setAmount(createStakeRec.getAmount().stripTrailingZeros());
                dto.setCreateTimestamp(createStakeRec.getCreateDate().getTime());

                if (StringUtils.isBlank(createStakeRec.getRefTxId())) {
                    int days = Seconds.secondsBetween(new DateTime(createStakeRec.getCreateDate()), DateTime.now()).getSeconds() / gethService.getStakingBasePeriod();
                    BigDecimal percent = calculateStakeRewardPercent(days);

                    dto.setDuration(days);
                    dto.setRewardPercent(percent);
                    dto.setRewardAmount(createStakeRec.getAmount().multiply(Util.convertPercentToDecimal(percent)).stripTrailingZeros());
                    dto.setRewardAnnualAmount(createStakeRec.getAmount().multiply(Util.convertPercentToDecimal(BigDecimal.valueOf(gethService.getStakingAnnualPercent()))).stripTrailingZeros());
                } else {
                    TransactionRecordWallet cancelStakeRec = walletRep.findFirstByTxId(createStakeRec.getRefTxId()).get();

                    int days = Seconds.secondsBetween(new DateTime(createStakeRec.getCreateDate()), new DateTime(cancelStakeRec.getCreateDate())).getSeconds() / gethService.getStakingBasePeriod();
                    int holdDays = Seconds.secondsBetween(new DateTime(cancelStakeRec.getCreateDate()), DateTime.now()).getSeconds() / gethService.getStakingBasePeriod();
                    BigDecimal percent = calculateStakeRewardPercent(days);

                    dto.setDuration(days);
                    dto.setRewardPercent(percent);
                    dto.setStatus(StakingStatus.convert(TransactionType.valueOf(cancelStakeRec.getType()), TransactionStatus.valueOf(cancelStakeRec.getStatus())));
                    dto.setCancelTimestamp(cancelStakeRec.getCreateDate().getTime());
                    dto.setRewardAmount(createStakeRec.getAmount().multiply(Util.convertPercentToDecimal(percent)).stripTrailingZeros());
                    dto.setRewardAnnualAmount(createStakeRec.getAmount().multiply(Util.convertPercentToDecimal(BigDecimal.valueOf(gethService.getStakingAnnualPercent()))).stripTrailingZeros());
                    dto.setTillWithdrawal(Math.max(0, gethService.getStakingHoldPeriod() / gethService.getStakingBasePeriod() - holdDays));

                    if (StringUtils.isNotBlank(cancelStakeRec.getRefTxId())) {
                        TransactionRecordWallet withdrawStakeRec = walletRep.findFirstByTxId(cancelStakeRec.getRefTxId()).get();
                        dto.setStatus(StakingStatus.convert(TransactionType.valueOf(withdrawStakeRec.getType()), TransactionStatus.valueOf(withdrawStakeRec.getStatus())));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public void postSubmit(Long userId, CoinService.CoinEnum coin, String txId) {
        User user = userService.findById(userId);
        TxDetailsDTO dto = getTransactionDetails(userId, coin, txId);

        socketService.pushTransaction(user.getPhone(), dto);
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void processCronTasks() {
        completePendingRecords();

        deliverTransfers();
        deliverSwaps();
    }

    private void completePendingRecords() {
        try {
            List<TransactionRecordWallet> pendingRecords = walletRep.findAllByProcessedAndStatus(ProcessedType.COMPLETE.getValue(), TransactionStatus.PENDING.getValue(), page);
            List<TransactionRecordWallet> completeRecords = massStatusUpdate(pendingRecords);

            completeRecords.forEach(e -> {
                if (e.getType() == TransactionType.RESERVE.getValue() && e.getStatus() == TransactionStatus.COMPLETE.getValue()) {
                    UserCoin userCoin = userService.getUserCoin(e.getIdentity().getUser().getId(), e.getCoin().getCode());
                    userCoin.setReservedBalance(userCoin.getReservedBalance().add(e.getAmount()));

                    userCoinRep.save(userCoin);
                }
            });

            walletRep.saveAll(completeRecords);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<TransactionRecordWallet> massStatusUpdate(List<TransactionRecordWallet> list) {
        List<TransactionRecordWallet> confirmedList = new ArrayList<>();

        list.stream().forEach(t -> {
            try {
                CoinService.CoinEnum coin = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                TransactionStatus status = coin.getTransactionDetails(t.getTxId(), StringUtils.EMPTY).getStatus();

                if (status != null && status != TransactionStatus.PENDING) {
                    t.setStatus(status.getValue());

                    confirmedList.add(t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return confirmedList;
    }

    private void deliverSwaps() {
        try {
            List<TransactionRecordWallet> list = walletRep.findAllByProcessedAndTypeAndStatusAndRefTxIdNull(ProcessedType.COMPLETE.getValue(), TransactionType.SEND_SWAP.getValue(), TransactionStatus.COMPLETE.getValue(), page);

            list.stream().forEach(t -> {
                try {
                    CoinService.CoinEnum coinCode = CoinService.CoinEnum.valueOf(t.getRefCoin().getCode());
                    String walletAddress = coinCode.getWalletAddress();

                    if (walletService.isEnoughBalance(coinCode, walletAddress, t.getRefAmount())) {
                        Identity identity = t.getIdentity();
                        String fromAddress = coinCode.getWalletAddress();
                        String toAddress = userService.getUserCoin(identity.getUser().getId(), coinCode.name()).getAddress();
                        String hex = coinCode.sign(fromAddress, toAddress, t.getRefAmount());

                        TxSubmitDTO submit = new TxSubmitDTO();
                        submit.setHex(hex);
                        submit.setFromAddress(fromAddress);
                        submit.setToAddress(toAddress);
                        submit.setCryptoAmount(t.getRefAmount());

                        String txId = coinCode.submitTransaction(submit);

                        if (StringUtils.isNotBlank(txId)) {
                            TransactionRecordWallet rec = new TransactionRecordWallet();
                            rec.setTxId(txId);
                            rec.setIdentity(identity);
                            rec.setCoin(coinCode.getCoinEntity());
                            rec.setAmount(t.getRefAmount());
                            rec.setType(TransactionType.RECEIVE_SWAP.getValue());
                            rec.setStatus(TransactionStatus.PENDING.getValue());
                            rec.setProfitPercent(t.getProfitPercent());
                            rec.setRefCoin(t.getCoin());
                            rec.setRefAmount(t.getAmount());
                            rec.setRefTxId(t.getTxId());

                            walletRep.save(rec);

                            t.setRefTxId(txId);
                        } else {
                            t.setProcessed(ProcessedType.FAIL.getValue());
                        }
                    } else {
                        t.setProcessed(ProcessedType.INSUFFICIENT_BALANCE.getValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            walletRep.saveAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverTransfers() {
        try {
            List<TransactionRecordWallet> list = walletRep.findAllByProcessedAndTypeAndStatusAndReceiverStatus(ProcessedType.COMPLETE.getValue(),
                    TransactionType.SEND_TRANSFER.getValue(),
                    TransactionStatus.COMPLETE.getValue(),
                    TransactionRecordWallet.RECEIVER_NOT_EXIST, page);

            list.stream().forEach(t -> {
                try {
                    Optional<User> receiverOpt = userService.findByPhone(t.getToPhone());

                    if (receiverOpt.isPresent()) {
                        CoinService.CoinEnum coinCode = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                        BigDecimal txFee = coinCode == CoinService.CoinEnum.CATM || coinCode == CoinService.CoinEnum.USDC ? walletService.convertToFee(coinCode) : coinCode.getTxFee();
                        BigDecimal withdrawAmount = t.getAmount().subtract(txFee);
                        String walletAddress = coinCode.getWalletAddress();

                        if (walletService.isEnoughBalance(coinCode, walletAddress, t.getAmount())) {
                            String fromAddress = coinCode.getWalletAddress();
                            String toAddress = userService.getUserCoin(receiverOpt.get().getId(), t.getCoin().getCode()).getAddress();
                            String hex = coinCode.sign(fromAddress, toAddress, withdrawAmount);

                            TxSubmitDTO dto = new TxSubmitDTO();
                            dto.setHex(hex);
                            dto.setCryptoAmount(withdrawAmount);
                            dto.setType(TransactionType.SEND_TRANSFER.getValue());
                            dto.setPhone(t.getToPhone());
                            dto.setImage(t.getImageId());
                            dto.setMessage(t.getMessage());

                            String txId = coinCode.submitTransaction(dto);

                            if (StringUtils.isNotBlank(txId)) {
                                User user = t.getIdentity().getUser();

                                //TODO: remove
                                TransactionRecordWallet record = new TransactionRecordWallet();
                                record.setTxId(txId);
                                record.setType(TransactionType.RECEIVE_TRANSFER.getValue());
                                record.setStatus(TransactionStatus.PENDING.getValue());
                                record.setFromPhone(user.getPhone());
                                record.setToPhone(dto.getPhone());
                                record.setMessage(dto.getMessage());
                                record.setImageId(dto.getImage());
                                record.setReceiverStatus(TransactionRecordWallet.RECEIVER_EXIST);
                                record.setIdentity(receiverOpt.get().getIdentity());
                                record.setCoin(coinCode.getCoinEntity());
                                record.setAmount(dto.getCryptoAmount());
                                walletRep.save(record);

                                TxDetailsDTO tx = new TxDetailsDTO();
                                tx.setUserId(receiverOpt.get().getId());
                                tx.setTxId(txId);
                                tx.setCoin(coinCode.name());
                                tx.setType(TransactionType.RECEIVE_TRANSFER);
                                tx.setStatus(TransactionStatus.PENDING);
                                tx.setCryptoAmount(dto.getCryptoAmount());
                                tx.setFromAddress(dto.getFromAddress());
                                tx.setToAddress(dto.getToAddress());
                                tx.setFromPhone(user.getPhone());
                                tx.setToPhone(dto.getPhone());
                                tx.setMessage(dto.getMessage());
                                tx.setImageId(dto.getImage());
                                mongo.save(tx);

                                t.setReceiverStatus(TransactionRecordWallet.RECEIVER_EXIST);
                            } else {
                                t.setProcessed(ProcessedType.FAIL.getValue());
                            }
                        } else {
                            t.setProcessed(ProcessedType.INSUFFICIENT_BALANCE.getValue());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            walletRep.saveAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TransactionRecordWallet linkPreviousTransaction(String txId, Optional<TransactionRecordWallet> previousTransactionRecOpt) {
        if (previousTransactionRecOpt.isPresent()) {
            TransactionRecordWallet previousTransactionRec = previousTransactionRecOpt.get();

            if (StringUtils.isBlank(previousTransactionRec.getRefTxId())) {
                previousTransactionRec.setRefTxId(txId);
            } else {
                Optional<TransactionRecordWallet> cancelStakeRecOpt = walletRep.findFirstByTxId(previousTransactionRec.getRefTxId());

                if (cancelStakeRecOpt.isPresent() && cancelStakeRecOpt.get().getStatus() == TransactionStatus.FAIL.getValue()) {
                    previousTransactionRec.setRefTxId(txId);
                }
            }

            return previousTransactionRec;
        }

        return null;
    }

    private BigDecimal calculateStakeRewardPercent(int days) {
        return new BigDecimal(days * gethService.getStakingAnnualPercent()).divide(new BigDecimal(gethService.getStakingAnnualPeriod() / gethService.getStakingBasePeriod()), 2, RoundingMode.HALF_DOWN).stripTrailingZeros();
    }
}