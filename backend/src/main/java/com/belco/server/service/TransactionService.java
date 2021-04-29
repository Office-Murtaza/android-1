package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.entity.*;
import com.belco.server.model.*;
import com.belco.server.repository.TransactionRecordRep;
import com.belco.server.repository.UserCoinRep;
import com.belco.server.util.Constant;
import com.belco.server.util.Util;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
@EnableScheduling
public class TransactionService {

    private static final String TERMINAL_SERIAL_NUMBER = "BT100872";

    private final TransactionRecordRep recordRep;
    private final UserCoinRep userCoinRep;
    private final UserService userService;
    private final TwilioService twilioService;
    private final NotificationService notificationService;
    private final WalletService walletService;
    private final GethService gethService;
    private final SettingsService settingsService;
    private final SocketService socketService;
    private final MongoTemplate mongo;

    @Value("${gb.url}")
    private String gbUrl;

    public TransactionService(TransactionRecordRep recordRep, UserCoinRep userCoinRep, UserService userService, TwilioService twilioService, NotificationService notificationService, WalletService walletService, GethService gethService, SettingsService settingsService, SocketService socketService, MongoTemplate mongo) {
        this.recordRep = recordRep;
        this.userCoinRep = userCoinRep;
        this.userService = userService;
        this.twilioService = twilioService;
        this.notificationService = notificationService;
        this.walletService = walletService;
        this.gethService = gethService;
        this.settingsService = settingsService;
        this.socketService = socketService;
        this.mongo = mongo;
    }

    public static TransactionHistoryDTO buildTxs(Map<String, TransactionDetailsDTO> map, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
        mergeTransactionRecords(map, transactionRecords);
        mergeTransactionDetails(map, details);

        List<TransactionDetailsDTO> list = convertAndSort(map);

        return new TransactionHistoryDTO(list.size(), list);
    }

    private static void mergeTransactionDetails(Map<String, TransactionDetailsDTO> map, List<TransactionDetailsDTO> details) {
        if (details != null && !details.isEmpty()) {
            details.stream().forEach(e -> {
                if (map.containsKey(e.getTxId())) {
                    Integer type = map.get(e.getTxId()).getType();
                    map.get(e.getTxId()).setType(TransactionType.convert(type, e.getType()));
                }
            });
        }
    }

    private static void mergeTransactionRecords(Map<String, TransactionDetailsDTO> map, List<TransactionRecord> txList) {
        if (txList != null && !txList.isEmpty()) {
            txList.stream().forEach(e -> {
                TransactionType type = e.getTransactionType();
                TransactionStatus status = e.getTransactionStatus(type);

                if (org.apache.commons.lang.StringUtils.isNotBlank(e.getDetail()) && map.containsKey(e.getDetail())) {
                    map.get(e.getDetail()).setType(type.getValue());
                    map.get(e.getDetail()).setStatus(status.getValue());
                } else {
                    Long txDBId = e.getId();

                    TransactionDetailsDTO tx = new TransactionDetailsDTO();
                    tx.setTxDBId(txDBId);
                    tx.setType(type.getValue());
                    tx.setStatus(status.getValue());
                    tx.setCryptoAmount(Util.format(e.getCryptoAmount(), 6));
                    tx.setTimestamp(e.getServerTime().getTime());

                    map.put(txDBId.toString(), tx);
                }
            });
        }
    }

    private static List<TransactionDetailsDTO> convertAndSort(Map<String, TransactionDetailsDTO> map) {
        if (!map.isEmpty()) {
            List<TransactionDetailsDTO> list = new ArrayList<>(map.values());
            list.sort(Comparator.comparing(TransactionDetailsDTO::getTimestamp).reversed());

            return list;
        }

        return new ArrayList<>();
    }

    public TransactionDetailsDTO getTransactionDetails(Long userId, CoinService.CoinEnum coinCode, String txId) {
        User user = userService.findById(userId);
        Coin coin = coinCode.getCoinEntity();

        TransactionDetailsDTO dto = new TransactionDetailsDTO();
        Optional<TransactionRecord> buySellRecOpt;

        if (org.apache.commons.lang.StringUtils.isNumeric(txId)) {  /** consider as txDbId */
            buySellRecOpt = recordRep.findById(Long.valueOf(txId));
        } else {                                                    /** consider as txId */
            String address = user.getUserCoin(coinCode.name()).getAddress();
            TransactionDetailsDTO tx = mongo.findOne(new Query(Criteria.where("txId").is(txId)), TransactionDetailsDTO.class);

            if (tx == null) {
                dto = coinCode.getTransactionDetails(txId, address);
            } else {
                dto = tx;
                dto.setType(TransactionType.getType(dto.getFromAddress(), dto.getToAddress(), address).getValue());
            }

            buySellRecOpt = recordRep.findOneByIdentityAndDetailAndCryptoCurrency(user.getIdentity(), txId, coinCode.name());
        }

        if (buySellRecOpt.isPresent()) {
            TransactionRecord buySell = buySellRecOpt.get();

            //return txId or txDBId
            if (StringUtils.isBlank(dto.getTxId())) {
                if (StringUtils.isNotBlank(buySell.getDetail())) {
                    dto.setTxId(buySell.getDetail());
                } else {
                    dto.setTxDBId(buySell.getId());
                }
            }

            dto.setType(buySell.getTransactionType().getValue());
            dto.setStatus(buySell.getTransactionStatus(TransactionType.valueOf(dto.getType())).getValue());
            dto.setCryptoAmount(buySell.getCryptoAmount().stripTrailingZeros());
            dto.setFiatAmount(buySell.getCashAmount().setScale(0));
            dto.setToAddress(buySell.getCryptoAddress());
            dto.setTimestamp(buySell.getServerTime().getTime());

            if (dto.getType() == TransactionType.SELL.getValue()) {
                dto.setCashStatus(CashStatus.getCashStatus(buySell.getCanBeCashedOut(), buySell.getWithdrawn()).getValue());
                dto.setSellInfo(coin.getName() + ":" + buySell.getCryptoAddress()
                        + "?amount=" + buySell.getCryptoAmount()
                        + "&label=" + buySell.getRemoteTransactionId()
                        + "&uuid=" + buySell.getUuid());
            }
        }

        return dto;
    }

    public TransactionHistoryDTO getTransactionHistory(Long userId, CoinService.CoinEnum coinCode) {
        User user = userService.findById(userId);
        String address = user.getUserCoin(coinCode.name()).getAddress();
        List<TransactionRecord> transactionRecords = recordRep.findAllByIdentityAndCryptoCurrency(user.getIdentity(), coinCode.name());

        Query query = new Query();
        query.addCriteria(
                new Criteria().andOperator(
                        Criteria.where("coin").is(coinCode.name()),
                        new Criteria().orOperator(
                                Criteria.where("userId").is(user.getId()),
                                Criteria.where("fromAddress").is(address),
                                Criteria.where("toAddress").is(address))));

        List<TransactionDetailsDTO> details = mongo.find(query, TransactionDetailsDTO.class);

        return coinCode.getTransactionHistory(address, transactionRecords, details);
    }

    public TransactionDetailsDTO submit(Long userId, CoinService.CoinEnum coin, TransactionDTO dto) throws InterruptedException {
        String txId;

        if (TransactionType.RECALL.getValue() == dto.getType()) {
            txId = recall(userId, coin, dto);
        } else {
            txId = coin.submitTransaction(dto);
            Thread.sleep(2000);
        }

        if (StringUtils.isNotBlank(txId) && coin.isTransactionSeenOnBlockchain(txId)) {
            if (TransactionType.RESERVE.getValue() == dto.getType()) {
                reserve(userId, coin, txId, dto);
            }

            if (TransactionType.SEND_TRANSFER.getValue() == dto.getType()) {
                persistTransfer(userId, coin, txId, dto);
            }

            if (TransactionType.SEND_SWAP.getValue() == dto.getType()) {
                swap(userId, coin, txId, dto);
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
        }

        return getTransactionDetails(userId, coin, txId);
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void updatePendingRecords() {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(Criteria.where("status").is(TransactionStatus.PENDING.getValue()), Criteria.where("timestamp").gt(System.currentTimeMillis() - 3600000)));
        query.limit(100);

        mongo.find(query, TransactionDetailsDTO.class).stream().forEach(t -> {
            CoinService.CoinEnum coin = CoinService.CoinEnum.valueOf(t.getCoin());
            Integer status = coin.getTransactionDetails(t.getTxId(), StringUtils.EMPTY).getStatus();
            User user = userService.findById(t.getUserId());

            if (status == null || status == TransactionStatus.FAIL.getValue()) {
                t.setStatus(TransactionStatus.FAIL.getValue());

                if (t.getType() == TransactionType.RECALL.getValue()) {
                    UserCoin userCoin = user.getUserCoin(t.getCoin());
                    userCoin.setReservedBalance(userCoin.getReservedBalance().add(t.getCryptoAmount().add(t.getCryptoFee())));
                    userCoinRep.save(userCoin);
                } else if (t.getType() == TransactionType.SEND_SWAP.getValue()) {
                    t.setProcessed(ProcessedType.FAIL.getValue());
                } else if (t.getType() == TransactionType.SEND_TRANSFER.getValue()) {
                    t.setProcessed(ProcessedType.FAIL.getValue());
                } else if (t.getType() == TransactionType.CREATE_STAKE.getValue()) {
                    updateStaking(t.getUserId(), coin.name(), StakingStatus.NOT_EXIST);
                } else if (t.getType() == TransactionType.CANCEL_STAKE.getValue()) {
                    updateStaking(t.getUserId(), coin.name(), StakingStatus.CREATED);
                } else if (t.getType() == TransactionType.WITHDRAW_STAKE.getValue()) {
                    updateStaking(t.getUserId(), coin.name(), StakingStatus.CANCELED);
                }
            } else if (status == TransactionStatus.COMPLETE.getValue()) {
                if (t.getType() == TransactionType.RESERVE.getValue()) {
                    UserCoin userCoin = user.getUserCoin(t.getCoin());
                    userCoin.setReservedBalance(userCoin.getReservedBalance().add(t.getCryptoAmount()));
                    userCoinRep.save(userCoin);
                } else if (t.getType() == TransactionType.SEND_SWAP.getValue()) {
                    deliverSwap(t);
                } else if (t.getType() == TransactionType.CREATE_STAKE.getValue()) {
                    updateStaking(t.getUserId(), coin.name(), StakingStatus.CREATED);
                } else if (t.getType() == TransactionType.CANCEL_STAKE.getValue()) {
                    updateStaking(t.getUserId(), coin.name(), StakingStatus.CANCELED);
                } else if (t.getType() == TransactionType.WITHDRAW_STAKE.getValue()) {
                    updateStaking(t.getUserId(), coin.name(), StakingStatus.WITHDRAWN);
                }
            }

            if (status != TransactionStatus.PENDING.getValue()) {
                t.setStatus(status);
                TransactionDetailsDTO tSaved = mongo.save(t);
                socketService.pushTransaction(userService.findById(t.getUserId()).getPhone(), tSaved);
            }
        });
    }

    public void persistTransfer(Long userId, CoinService.CoinEnum coin, String txId, TransactionDTO dto) {
        try {
            User user = userService.findById(userId);
            Optional<User> receiverOpt = userService.findByPhone(dto.getPhone());

            TransactionDetailsDTO tx = new TransactionDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setLink(coin.getExplorerUrl() + "/" + txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.SEND_TRANSFER.getValue());
            tx.setStatus(TransactionStatus.PENDING.getValue());
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            tx.setFromPhone(user.getPhone());
            tx.setToPhone(dto.getPhone());
            tx.setMessage(dto.getMessage());
            tx.setImage(dto.getImage());
            tx.setProcessed(receiverOpt.isPresent() ? ProcessedType.COMPLETE.getValue() : ProcessedType.PENDING.getValue());
            tx.setTimestamp(System.currentTimeMillis());
            mongo.save(tx);

            if (receiverOpt.isPresent()) {
                TransactionDetailsDTO tx2 = new TransactionDetailsDTO();
                tx2.setUserId(userId);
                tx2.setTxId(txId);
                tx.setLink(coin.getExplorerUrl() + "/" + txId);
                tx2.setCoin(coin.name());
                tx2.setType(TransactionType.RECEIVE_TRANSFER.getValue());
                tx2.setStatus(TransactionStatus.PENDING.getValue());
                tx2.setCryptoAmount(dto.getCryptoAmount());
                tx2.setFromAddress(dto.getFromAddress());
                tx2.setToAddress(dto.getToAddress());
                tx2.setFromPhone(user.getPhone());
                tx2.setToPhone(dto.getPhone());
                tx2.setMessage(dto.getMessage());
                tx2.setImage(dto.getImage());
                tx2.setTimestamp(System.currentTimeMillis());
                mongo.save(tx2);

                String token = receiverOpt.get().getNotificationsToken();
                StringBuilder messageBuilder = new StringBuilder("You just received " + dto.getCryptoAmount().stripTrailingZeros() + " " + coin.name());

                if (StringUtils.isNotBlank(dto.getMessage())) {
                    messageBuilder.append("\n\n").append("\"").append(dto.getMessage()).append("\"").append("\n");
                }

                notificationService.sendMessageWithData(new NotificationDTO("New incoming transfer", messageBuilder.toString(), null, token));
            } else {
                twilioService.sendTransferMessageToNotExistingUser(coin, dto.getPhone(), dto.getMessage(), dto.getImage(), dto.getCryptoAmount().stripTrailingZeros());
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
            BigDecimal txAmount = recordRep.getTransactionsSumByDate(user.getIdentity(), Util.getStartOfDay(), new Date());

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

    public PreSubmitDTO preSubmit(Long userId, CoinService.CoinEnum coinId, TransactionDTO transaction) {
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

    public void swap(Long userId, CoinService.CoinEnum coin, String txId, TransactionDTO dto) {
        try {
            TransactionDetailsDTO tx = new TransactionDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setLink(coin.getExplorerUrl() + "/" + txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.SEND_SWAP.getValue());
            tx.setStatus(TransactionStatus.PENDING.getValue());
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            tx.setRefCoin(dto.getRefCoin());
            tx.setRefCryptoAmount(dto.getRefCryptoAmount());
            tx.setProcessed(ProcessedType.PENDING.getValue());
            tx.setTimestamp(System.currentTimeMillis());
            mongo.save(tx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reserve(Long userId, CoinService.CoinEnum coin, String txId, TransactionDTO dto) {
        try {
            TransactionDetailsDTO tx = new TransactionDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setLink(coin.getExplorerUrl() + "/" + txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.RESERVE.getValue());
            tx.setStatus(TransactionStatus.PENDING.getValue());
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            tx.setProcessed(ProcessedType.PENDING.getValue());
            tx.setTimestamp(System.currentTimeMillis());
            mongo.save(tx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String recall(Long userId, CoinService.CoinEnum coinCode, TransactionDTO dto) {
        try {
            UserCoin userCoin = userService.getUserCoin(userId, coinCode.name());
            BigDecimal reserved = userCoin.getReservedBalance();
            BigDecimal txFee = coinCode == CoinService.CoinEnum.CATM || coinCode == CoinService.CoinEnum.USDC ? walletService.convertToFee(coinCode) : coinCode.getTxFee();
            String fromAddress = coinCode.getWalletAddress(Constant.APP_WALLET_ID);

            if (walletService.isEnoughBalance(coinCode, fromAddress, dto.getCryptoAmount()) && reserved.compareTo(dto.getCryptoAmount().add(txFee)) >= 0) {
                String toAddress = userCoin.getAddress();
                String hex = coinCode.sign(Constant.APP_WALLET_ID, fromAddress, toAddress, dto.getCryptoAmount());

                TransactionDTO submit = new TransactionDTO();
                submit.setHex(hex);
                submit.setFromAddress(fromAddress);
                submit.setToAddress(toAddress);
                submit.setCryptoAmount(dto.getCryptoAmount());

                String txId = coinCode.submitTransaction(submit);

                if (StringUtils.isNotBlank(txId)) {
                    TransactionDetailsDTO tx = new TransactionDetailsDTO();
                    tx.setUserId(userId);
                    tx.setTxId(txId);
                    tx.setLink(coinCode.getExplorerUrl() + "/" + txId);
                    tx.setCoin(coinCode.name());
                    tx.setType(TransactionType.RECALL.getValue());
                    tx.setStatus(TransactionStatus.PENDING.getValue());
                    tx.setCryptoAmount(dto.getCryptoAmount());
                    tx.setCryptoFee(txFee);
                    tx.setFromAddress(fromAddress);
                    tx.setToAddress(toAddress);
                    tx.setTimestamp(System.currentTimeMillis());
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

    public void createStake(Long userId, CoinService.CoinEnum coin, String txId, TransactionDTO dto) {
        try {
            TransactionDetailsDTO tx = new TransactionDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setLink(coin.getExplorerUrl() + "/" + txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.CREATE_STAKE.getValue());
            tx.setStatus(TransactionStatus.PENDING.getValue());
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            tx.setTimestamp(System.currentTimeMillis());
            mongo.save(tx);

            StakingDetailsDTO staking = new StakingDetailsDTO();
            staking.setStatus(StakingStatus.CREATE_PENDING.getValue());
            staking.setUserId(userId);
            staking.setCoin(coin.name());
            staking.setCryptoAmount(dto.getCryptoAmount());
            staking.setAnnualPercent(gethService.getStakingAnnualPercent());
            staking.setBasePeriod(gethService.getStakingBasePeriod());
            staking.setHoldPeriod(gethService.getStakingHoldPeriod());
            staking.setCreateTxId(txId);
            staking.setCreateTimestamp(System.currentTimeMillis());
            mongo.save(staking);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelStake(Long userId, CoinService.CoinEnum coin, String txId, TransactionDTO dto) {
        try {
            TransactionDetailsDTO tx = new TransactionDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setLink(coin.getExplorerUrl() + "/" + txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.CANCEL_STAKE.getValue());
            tx.setStatus(TransactionStatus.PENDING.getValue());
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            tx.setTimestamp(System.currentTimeMillis());
            mongo.save(tx);

            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(Criteria.where("userId").is(userId), Criteria.where("coin").is(coin.name())));
            query.with(new Sort(Sort.Direction.DESC, "createTimestamp"));

            StakingDetailsDTO staking = mongo.findOne(query, StakingDetailsDTO.class);
            staking.setStatus(StakingStatus.CANCEL_PENDING.getValue());
            staking.setCancelTxId(txId);
            staking.setCancelTimestamp(System.currentTimeMillis());
            mongo.save(staking);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void withdrawStake(Long userId, CoinService.CoinEnum coin, String txId, TransactionDTO dto) {
        try {
            TransactionDetailsDTO tx = new TransactionDetailsDTO();
            tx.setUserId(userId);
            tx.setTxId(txId);
            tx.setLink(coin.getExplorerUrl() + "/" + txId);
            tx.setCoin(coin.name());
            tx.setType(TransactionType.CANCEL_STAKE.getValue());
            tx.setStatus(TransactionStatus.PENDING.getValue());
            tx.setCryptoAmount(dto.getCryptoAmount());
            tx.setFromAddress(dto.getFromAddress());
            tx.setToAddress(dto.getToAddress());
            tx.setTimestamp(System.currentTimeMillis());
            mongo.save(tx);

            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(Criteria.where("userId").is(userId), Criteria.where("coin").is(coin.name())));
            query.with(new Sort(Sort.Direction.DESC, "cancelTimestamp"));

            StakingDetailsDTO staking = mongo.findOne(query, StakingDetailsDTO.class);
            staking.setStatus(StakingStatus.WITHDRAW_PENDING.getValue());
            staking.setWithdrawTxId(txId);
            staking.setWithdrawTimestamp(System.currentTimeMillis());
            mongo.save(staking);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StakingDetailsDTO getStakingDetails(Long userId, CoinService.CoinEnum coin) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(Criteria.where("userId").is(userId), Criteria.where("coin").is(coin.name())));
        query.with(new Sort(Sort.Direction.DESC, "createTimestamp"));

        StakingDetailsDTO staking = mongo.findOne(query, StakingDetailsDTO.class);

        if (staking == null || staking.getStatus() == StakingStatus.WITHDRAWN.getValue()) {
            staking = new StakingDetailsDTO();
            staking.setStatus(StakingStatus.NOT_EXIST.getValue());
            staking.setAnnualPercent(gethService.getStakingAnnualPercent());
            staking.setBasePeriod(gethService.getStakingBasePeriod());
            staking.setAnnualPeriod(gethService.getStakingAnnualPeriod());
            staking.setHoldPeriod(gethService.getStakingHoldPeriod());
        }

        return staking;
    }

    private void deliverSwap(TransactionDetailsDTO t) {
        try {
            CoinService.CoinEnum coin = CoinService.CoinEnum.valueOf(t.getRefCoin());
            String fromAddress = coin.getWalletAddress(Constant.APP_WALLET_ID);

            if (walletService.isEnoughBalance(coin, fromAddress, t.getRefCryptoAmount())) {
                String toAddress = userService.getUserCoin(t.getUserId(), t.getRefCoin()).getAddress();
                String hex = coin.sign(Constant.APP_WALLET_ID, fromAddress, toAddress, t.getRefCryptoAmount());

                TransactionDTO submit = new TransactionDTO();
                submit.setHex(hex);
                submit.setFromAddress(fromAddress);
                submit.setToAddress(toAddress);
                submit.setCryptoAmount(t.getRefCryptoAmount());

                String txId = coin.submitTransaction(submit);

                if (StringUtils.isNotBlank(txId)) {
                    TransactionDetailsDTO tx = new TransactionDetailsDTO();
                    tx.setUserId(t.getUserId());
                    tx.setTxId(txId);
                    tx.setLink(coin.getExplorerUrl() + "/" + txId);
                    tx.setCoin(coin.name());
                    tx.setType(TransactionType.RECEIVE_SWAP.getValue());
                    tx.setStatus(TransactionStatus.PENDING.getValue());
                    tx.setCryptoAmount(t.getRefCryptoAmount());
                    tx.setFromAddress(fromAddress);
                    tx.setToAddress(toAddress);
                    tx.setRefTxId(t.getTxId());
                    tx.setRefCoin(t.getCoin());
                    tx.setRefCryptoAmount(t.getCryptoAmount());
                    mongo.save(tx);

                    t.setRefTxId(txId);
                    t.setProcessed(ProcessedType.COMPLETE.getValue());
                } else {
                    t.setProcessed(ProcessedType.FAIL.getValue());
                }
            } else {
                t.setProcessed(ProcessedType.INSUFFICIENT_BALANCE.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deliverPendingTransfers(String toPhone) {
        try {
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(Criteria.where("type").is(TransactionType.SEND_TRANSFER.getValue()), Criteria.where("status").is(TransactionStatus.COMPLETE.getValue()), Criteria.where("processed").is(ProcessedType.PENDING.getValue()), Criteria.where("toPhone").is(toPhone)));

            mongo.find(query, TransactionDetailsDTO.class).stream().forEach(t -> {
                try {
                    Optional<User> receiverOpt = userService.findByPhone(t.getToPhone());

                    if (receiverOpt.isPresent()) {
                        CoinService.CoinEnum coin = CoinService.CoinEnum.valueOf(t.getCoin());
                        BigDecimal txFee = coin == CoinService.CoinEnum.CATM || coin == CoinService.CoinEnum.USDC ? walletService.convertToFee(coin) : coin.getTxFee();
                        BigDecimal withdrawAmount = t.getCryptoAmount().subtract(txFee);
                        String fromAddress = coin.getWalletAddress(Constant.APP_WALLET_ID);

                        if (walletService.isEnoughBalance(coin, fromAddress, t.getCryptoAmount())) {
                            String toAddress = userService.getUserCoin(receiverOpt.get().getId(), t.getCoin()).getAddress();
                            String hex = coin.sign(Constant.APP_WALLET_ID, fromAddress, toAddress, withdrawAmount);

                            TransactionDTO dto = new TransactionDTO();
                            dto.setHex(hex);
                            dto.setCryptoAmount(withdrawAmount);
                            dto.setType(TransactionType.RECEIVE_TRANSFER.getValue());
                            dto.setPhone(t.getToPhone());
                            dto.setImage(t.getImage());
                            dto.setMessage(t.getMessage());

                            String txId = coin.submitTransaction(dto);

                            if (StringUtils.isNotBlank(txId)) {
                                TransactionDetailsDTO tx = new TransactionDetailsDTO();
                                tx.setUserId(receiverOpt.get().getId());
                                tx.setTxId(txId);
                                tx.setLink(coin.getExplorerUrl() + "/" + txId);
                                tx.setCoin(coin.name());
                                tx.setType(TransactionType.RECEIVE_TRANSFER.getValue());
                                tx.setStatus(TransactionStatus.PENDING.getValue());
                                tx.setCryptoAmount(withdrawAmount);
                                tx.setFromAddress(fromAddress);
                                tx.setToAddress(toAddress);
                                tx.setFromPhone(t.getFromPhone());
                                tx.setToPhone(t.getToPhone());
                                tx.setMessage(t.getMessage());
                                tx.setImage(t.getImage());
                                mongo.save(tx);

                                t.setProcessed(ProcessedType.COMPLETE.getValue());
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

                mongo.save(t);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStaking(Long userId, String coin, StakingStatus status) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(Criteria.where("userId").is(userId), Criteria.where("coin").is(coin)));
        query.with(new Sort(Sort.Direction.DESC, "createTimestamp"));

        StakingDetailsDTO staking = mongo.findOne(query, StakingDetailsDTO.class);
        staking.setStatus(status.getValue());
        mongo.save(staking);
    }
}