package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.*;
import com.batm.repository.*;
import com.batm.util.Constant;
import com.batm.util.Util;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@EnableScheduling
public class TransactionService {

    @Autowired
    private TransactionRecordRep recordRep;

    @Autowired
    private TransactionRecordWalletRep walletRep;

    @Autowired
    private UserCoinRep userCoinRep;

    @Autowired
    private UserService userService;

    @Autowired
    private ChainalysisService chainalysisService;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private WalletService walletService;

    @Value("${gb.url}")
    private String gbUrl;

    public TransactionDetailsDTO getTransactionDetails(Long userId, CoinService.CoinEnum coinCode, String txId) {
        User user = userService.findById(userId);
        Identity identity = user.getIdentity();
        Coin coin = coinCode.getCoinEntity();

        TransactionDetailsDTO dto = new TransactionDetailsDTO();
        Optional<TransactionRecord> buySellTx;

        if (org.apache.commons.lang.StringUtils.isNumeric(txId)) {  /** consider as txDbId */
            buySellTx = recordRep.findById(Long.valueOf(txId));
        } else {                                                    /** consider as txId */
            String address = user.getUserCoin(coinCode.name()).getAddress();
            dto = coinCode.getTransaction(txId, address);
            buySellTx = recordRep.findOneByIdentityAndDetailAndCryptoCurrency(user.getIdentity(), txId, coinCode.name());
        }

        Optional<TransactionRecordWallet> giftTx = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.SEND_GIFT.getValue(), TransactionType.RECEIVE_GIFT.getValue()));
        Optional<TransactionRecordWallet> exchangeTx = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.SEND_EXCHANGE.getValue(), TransactionType.RECEIVE_EXCHANGE.getValue()));

        if (giftTx.isPresent()) {
            TransactionRecordWallet gift = giftTx.get();

            dto.setPhone(gift.getPhone());
            dto.setImageId(gift.getImageId());
            dto.setMessage(gift.getMessage());
            dto.setType(TransactionType.convert(dto.getType(), TransactionType.valueOf(gift.getType())));
        } else if (exchangeTx.isPresent()) {
            TransactionRecordWallet exchange = exchangeTx.get();

            String code = exchange.getRefCoin().getCode();
            dto.setRefTxId(exchange.getRefTxId());
            dto.setRefLink(CoinService.CoinEnum.valueOf(code).getExplorerUrl() + "/" + exchange.getRefTxId());
            dto.setRefCoin(code);
            dto.setRefCryptoAmount(exchange.getRefAmount());
            dto.setType(TransactionType.convert(dto.getType(), TransactionType.valueOf(exchange.getType())));
        } else if (buySellTx.isPresent()) {
            TransactionRecord buySell = buySellTx.get();

            //return either txId or txDbId
            if (StringUtils.isBlank(dto.getTxId())) {
                if (StringUtils.isNotBlank(buySell.getDetail())) {
                    dto.setTxId(buySell.getDetail());
                } else {
                    dto.setTxDbId(buySell.getId().toString());
                }
            }

            dto.setType(buySell.getTransactionType());
            dto.setStatus(buySell.getTransactionStatus(dto.getType()));
            dto.setCryptoAmount(buySell.getCryptoAmount().stripTrailingZeros());
            dto.setFiatAmount(buySell.getCashAmount().setScale(0));
            dto.setToAddress(buySell.getCryptoAddress());
            dto.setDate2(buySell.getServerTime());

            if (dto.getType() == TransactionType.SELL) {
                dto.setCashStatus(CashStatus.getCashStatus(buySell.getCanBeCashedOut(), buySell.getWithdrawn()));
                dto.setSellInfo(coin.getName() + ":" + buySell.getCryptoAddress()
                        + "?amount=" + buySell.getCryptoAmount()
                        + "&label=" + buySell.getRemoteTransactionId()
                        + "&uuid=" + buySell.getUuid());
            }
        }

        return dto;
    }

    public TransactionListDTO getTransactionHistory(Long userId, CoinService.CoinEnum coinCode, Integer startIndex) {
        User user = userService.findById(userId);
        Identity identity = user.getIdentity();
        Coin coin = coinCode.getCoinEntity();
        String address = user.getUserCoin(coinCode.name()).getAddress();

        TxListDTO txDTO = new TxListDTO();
        txDTO.setTransactionRecords(recordRep.findAllByIdentityAndCryptoCurrency(user.getIdentity(), coinCode.name()));
        txDTO.setTransactionRecordWallets(walletRep.findAllByIdentityAndCoinAndTypeIn(identity, coin, TransactionType.getWalletTypes()));

        return coinCode.getTransactionList(address, startIndex, Constant.TRANSACTIONS_COUNT, txDTO);
    }

    public void saveGift(Long userId, CoinService.CoinEnum coinCode, String txId, SubmitTransactionDTO dto) {
        try {
            User user = userService.findById(userId);
            User receiver = userService.findByPhone(dto.getPhone());
            Coin coin = userService.getUserCoin(userId, coinCode.name()).getCoin();

            if (BooleanUtils.isTrue(dto.getFromServerWallet())) {
                TransactionRecordWallet record = new TransactionRecordWallet();
                record.setTxId(txId);
                record.setType(TransactionType.RECEIVE_GIFT.getValue());
                record.setStatus(TransactionStatus.PENDING.getValue());
                record.setPhone(dto.getPhone());
                record.setMessage(dto.getMessage());
                record.setImageId(dto.getImageId());
                record.setReceiverStatus(TransactionRecordWallet.RECEIVER_EXIST);
                record.setIdentity(receiver.getIdentity());
                record.setCoin(coin);
                record.setAmount(dto.getCryptoAmount());

                walletRep.save(record);
            } else {
                boolean receiverExists = receiver != null;
                twilioService.sendGiftMessage(coinCode, dto, receiverExists);

                TransactionRecordWallet sendRecord = new TransactionRecordWallet();
                sendRecord.setTxId(txId);
                sendRecord.setType(dto.getType());
                sendRecord.setAmount(dto.getCryptoAmount());
                sendRecord.setStatus(TransactionStatus.PENDING.getValue());
                sendRecord.setPhone(dto.getPhone());
                sendRecord.setMessage(dto.getMessage());
                sendRecord.setImageId(dto.getImageId());
                sendRecord.setReceiverStatus(receiverExists ? TransactionRecordWallet.RECEIVER_EXIST : TransactionRecordWallet.RECEIVER_NOT_EXIST);
                sendRecord.setIdentity(user.getIdentity());
                sendRecord.setCoin(coin);
                sendRecord.setRefTxId(dto.getRefTxId());

                walletRep.save(sendRecord);

                if (receiverExists) {
                    TransactionRecordWallet receiveRecord = new TransactionRecordWallet();
                    receiveRecord.setTxId(sendRecord.getTxId());
                    receiveRecord.setType(TransactionType.RECEIVE_GIFT.getValue());
                    receiveRecord.setStatus(TransactionStatus.PENDING.getValue());
                    receiveRecord.setPhone(sendRecord.getPhone());
                    receiveRecord.setMessage(sendRecord.getMessage());
                    receiveRecord.setImageId(sendRecord.getImageId());
                    receiveRecord.setReceiverStatus(TransactionRecordWallet.RECEIVER_EXIST);
                    receiveRecord.setIdentity(receiver.getIdentity());
                    receiveRecord.setCoin(sendRecord.getCoin());
                    receiveRecord.setAmount(sendRecord.getAmount());

                    walletRep.save(receiveRecord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserLimitDTO getLimits(Long userId) {
        UserLimitDTO dto = new UserLimitDTO();
        dto.setDailyLimit(new AmountDTO(BigDecimal.ZERO));
        dto.setTxLimit(new AmountDTO(BigDecimal.ZERO));
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

            dto.setDailyLimit(new AmountDTO(Util.format2(dailyLimit)));
            dto.setTxLimit(new AmountDTO(Util.format2(txLimit)));
            dto.setSellProfitRate(new BigDecimal("1.05"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public PreSubmitDTO preSubmit(Long userId, CoinService.CoinEnum coinId, SubmitTransactionDTO transaction) {
        PreSubmitDTO dto = new PreSubmitDTO();

        try {
            User user = userService.findById(userId);

            StringBuilder params = new StringBuilder();
            params.append("?serial_number=").append(Constant.TERMINAL_SERIAL_NUMBER);
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

    public void exchange(Long userId, CoinService.CoinEnum coinCode, String txId, SubmitTransactionDTO dto) {
        try {
            CoinService.CoinEnum refCoinCode = CoinService.CoinEnum.valueOf(dto.getRefCoin());

            TransactionRecordWallet record = new TransactionRecordWallet();
            record.setTxId(txId);
            record.setIdentity(userService.findById(userId).getIdentity());
            record.setCoin(coinCode.getCoinEntity());
            record.setAmount(dto.getCryptoAmount());
            record.setType(TransactionType.SEND_EXCHANGE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setProfit(coinCode.getCoinEntity().getProfitExchange());
            record.setRefCoin(refCoinCode.getCoinEntity());

            BigDecimal refAmount = dto.getCryptoAmount()
                    .multiply(coinCode.getPrice())
                    .divide(refCoinCode.getPrice(), refCoinCode.getCoinEntity().getScale(), RoundingMode.HALF_DOWN)
                    .multiply(BigDecimal.valueOf(100).subtract(coinCode.getCoinEntity().getProfitExchange()).divide(BigDecimal.valueOf(100)))
                    .setScale(refCoinCode.getCoinEntity().getScale(), BigDecimal.ROUND_DOWN).stripTrailingZeros();

            record.setRefAmount(refAmount);

            walletRep.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reserve(Long userId, CoinService.CoinEnum coinCode, String txId, SubmitTransactionDTO dto) {
        try {
            TransactionRecordWallet record = new TransactionRecordWallet();
            record.setTxId(txId);
            record.setIdentity(userService.findById(userId).getIdentity());
            record.setCoin(coinCode.getCoinEntity());
            record.setAmount(dto.getCryptoAmount());
            record.setType(TransactionType.RESERVE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());

            walletRep.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String recall(Long userId, CoinService.CoinEnum coinCode, SubmitTransactionDTO dto) {
        try {
            UserCoin userCoin = userService.getUserCoin(userId, coinCode.name());
            BigDecimal reserved = userCoin.getReservedBalance();
            BigDecimal txFee = Util.nvl(coinCode.getCoinSettings().getRecallFee(), coinCode.getCoinSettings().getTxFee());
            BigDecimal walletBalance = walletService.getBalance(coinCode);

            if (reserved.compareTo(dto.getCryptoAmount().add(txFee)) >= 0 && walletBalance.compareTo(dto.getCryptoAmount()) >= 0) {
                String fromAddress = coinCode.getWalletAddress();
                String toAddress = userCoin.getAddress();
                String hex = coinCode.sign(fromAddress, toAddress, dto.getCryptoAmount());
                System.out.println("hex: " + hex);

                String txId = coinCode.submitTransaction(hex);
                System.out.println("txId: " + txId);

                if (StringUtils.isNotBlank(txId)) {
                    TransactionRecordWallet record = new TransactionRecordWallet();
                    record.setTxId(txId);
                    record.setIdentity(userService.findById(userId).getIdentity());
                    record.setCoin(coinCode.getCoinEntity());
                    record.setAmount(dto.getCryptoAmount());
                    record.setType(TransactionType.RECALL.getValue());
                    record.setStatus(TransactionStatus.PENDING.getValue());

                    walletRep.save(record);

                    dto.setFromAddress(fromAddress);
                    dto.setToAddress(toAddress);
                    dto.setCryptoAmount(dto.getCryptoAmount().add(txFee));
                    dto.setFee(txFee);

                    userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(dto.getCryptoAmount()));
                    userCoinRep.save(userCoin);

                    return txId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void stake(Long userId, CoinService.CoinEnum coinCode, String txId, BigDecimal amount) {
        try {
            TransactionRecordWallet record = new TransactionRecordWallet();
            record.setIdentity(userService.findByUserId(userId));
            record.setCoin(coinCode.getCoinEntity());
            record.setType(TransactionType.STAKE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setAmount(amount);
            record.setTxId(txId);

            walletRep.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unstake(Long userId, CoinService.CoinEnum coinCode, String txId, BigDecimal amount) {
        try {
            TransactionRecordWallet record = new TransactionRecordWallet();
            record.setIdentity(userService.findByUserId(userId));
            record.setCoin(coinCode.getCoinEntity());
            record.setType(TransactionType.UNSTAKE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setAmount(amount);
            record.setTxId(txId);

            walletRep.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StakeDetailsDTO getStakeDetails(Long userId, CoinService.CoinEnum coinCode) {
        try {
            Identity identity = userService.findByUserId(userId);
            Coin coin = coinCode.getCoinEntity();

            List<TransactionRecordWallet> records = walletRep.findAllByIdentityAndCoinAndTypeIn(identity, coin, Arrays.asList(TransactionType.STAKE.getValue()));

            for (TransactionRecordWallet record : records) {
                if (StringUtils.isBlank(record.getRefTxId())) {
                    int days = Days.daysBetween(new DateTime(record.getCreateDate()), DateTime.now()).getDays();

                    StakeDetailsDTO dto = new StakeDetailsDTO();
                    dto.setExist(true);
                    dto.setStakedAmount(record.getAmount());
                    dto.setStakedDays(days);
                    dto.setRewardsPercent(new BigDecimal(days).divide(new BigDecimal(365)).multiply(new BigDecimal(12)).stripTrailingZeros());
                    dto.setRewardsAmount(record.getAmount().multiply(BigDecimal.ONE.add(dto.getRewardsPercent().divide(new BigDecimal(100)))).stripTrailingZeros());

                    return dto;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new StakeDetailsDTO();
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void processCronTasks() {
        completePendingRecords();

        chainalysisTracking();
        deliverReservedGifts();
        deliverReservedExchange();
    }

    private void completePendingRecords() {
        try {
            List<TransactionRecordWallet> pendingRecords = walletRep.findAllByStatusAndHoursAgo(TransactionStatus.PENDING.getValue(), 2, PageRequest.of(0, 50));
            List<TransactionRecordWallet> completeRecords = massStatusCheck(pendingRecords);

            completeRecords.forEach(e -> {
                if (e.getType() == TransactionType.RESERVE.getValue()) {
                    UserCoin userCoin = userService.getUserCoin(e.getIdentity().getUser().getId(), e.getCoin().getCode());
                    userCoin.setReservedBalance(userCoin.getReservedBalance().add(e.getAmount()));

                    userCoinRep.save(userCoin);
                }

                if (e.getType() == TransactionType.UNSTAKE.getValue()) {
                    List<TransactionRecordWallet> stakedRecords = walletRep.findAllByIdentityAndCoinAndTypeIn(e.getIdentity(), e.getCoin(), Arrays.asList(TransactionType.STAKE.getValue()));

                    for (TransactionRecordWallet record : stakedRecords) {
                        record.setRefTxId(e.getTxId());

                        walletRep.save(record);

                        break;
                    }
                }
            });

            walletRep.saveAll(completeRecords);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<TransactionRecordWallet> massStatusCheck(List<TransactionRecordWallet> list) {
        List<TransactionRecordWallet> confirmedList = new ArrayList<>();

        list.stream().forEach(t -> {
            try {
                CoinService.CoinEnum coinId = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                TransactionStatus status = coinId.getTransactionStatus(t.getTxId());

                if (status == TransactionStatus.COMPLETE) {
                    t.setStatus(status.getValue());

                    confirmedList.add(t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return confirmedList;
    }

    private void chainalysisTracking() {
        try {
            List<TransactionRecord> list = recordRep.findNotTrackedTransactions(PageRequest.of(0, 50));

            if (!list.isEmpty()) {
                List<TransactionRecord> listRes = chainalysisService.process(list);
                recordRep.saveAll(listRes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverReservedExchange() {
        try {
            List<TransactionRecordWallet> list = walletRep.findAllByTypeAndStatusAndRefTxIdNullAndHoursAgo(TransactionType.SEND_EXCHANGE.getValue(), TransactionStatus.COMPLETE.getValue(), 2, PageRequest.of(0, 10));
            List<TransactionRecordWallet> confirmedList = new ArrayList<>();

            list.stream().forEach(t -> {
                try {
                    CoinService.CoinEnum coinCode = CoinService.CoinEnum.valueOf(t.getRefCoin().getCode());
                    BigDecimal txFee = coinCode.getCoinSettings().getTxFee();
                    BigDecimal withdrawAmount = t.getRefAmount().subtract(txFee);
                    BigDecimal walletBalance = walletService.getBalance(coinCode);

                    if (walletBalance.compareTo(withdrawAmount.add(txFee)) >= 0) {
                        Identity identity = t.getIdentity();
                        String fromAddress = coinCode.getWalletAddress();
                        String toAddress = userService.getUserCoin(identity.getUser().getId(), coinCode.name()).getAddress();
                        String hex = coinCode.sign(fromAddress, toAddress, withdrawAmount);
                        String txId = coinCode.submitTransaction(hex);

                        if (StringUtils.isNotBlank(txId)) {
                            TransactionRecordWallet c2cRec = new TransactionRecordWallet();
                            c2cRec.setTxId(txId);
                            c2cRec.setIdentity(identity);
                            c2cRec.setCoin(coinCode.getCoinEntity());
                            c2cRec.setAmount(t.getRefAmount());
                            c2cRec.setType(TransactionType.RECEIVE_EXCHANGE.getValue());
                            c2cRec.setStatus(TransactionStatus.PENDING.getValue());
                            c2cRec.setProfit(t.getProfit());
                            c2cRec.setRefCoin(t.getCoin());
                            c2cRec.setRefAmount(t.getAmount());
                            c2cRec.setRefTxId(t.getTxId());

                            walletRep.save(c2cRec);

                            t.setRefTxId(txId);
                            confirmedList.add(t);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            walletRep.saveAll(confirmedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverReservedGifts() {
        try {
            List<TransactionRecordWallet> list = walletRep.findAllByTypeAndStatusAndStepAndDaysAgo(
                    TransactionType.SEND_GIFT.getValue(),
                    TransactionStatus.COMPLETE.getValue(),
                    TransactionRecordWallet.RECEIVER_NOT_EXIST,
                    7, PageRequest.of(0, 10));

            List<TransactionRecordWallet> confirmedList = new ArrayList<>();

            list.stream().forEach(t -> {
                try {
                    User receiver = userService.findByPhone(t.getPhone());

                    if (receiver != null) {
                        CoinService.CoinEnum coinCode = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                        BigDecimal txFee = coinCode.getCoinSettings().getTxFee();
                        BigDecimal withdrawAmount = t.getAmount().subtract(txFee);
                        BigDecimal walletBalance = walletService.getBalance(coinCode);

                        if (walletBalance.compareTo(withdrawAmount.add(txFee)) >= 0) {
                            String fromAddress = coinCode.getWalletAddress();
                            String toAddress = userService.getUserCoin(receiver.getId(), t.getCoin().getCode()).getAddress();
                            String hex = coinCode.sign(fromAddress, toAddress, withdrawAmount);

                            SubmitTransactionDTO dto = new SubmitTransactionDTO();
                            dto.setHex(hex);
                            dto.setCryptoAmount(withdrawAmount);
                            dto.setRefTxId(t.getTxId());
                            dto.setType(TransactionType.SEND_GIFT.getValue());
                            dto.setPhone(t.getPhone());
                            dto.setImageId(t.getImageId());
                            dto.setMessage(t.getMessage());
                            dto.setFromServerWallet(true);

                            String txId = coinCode.submitTransaction(hex);

                            if (StringUtils.isNotBlank(txId)) {
                                saveGift(t.getIdentity().getUser().getId(), coinCode, txId, dto);

                                t.setReceiverStatus(TransactionRecordWallet.RECEIVER_EXIST);
                                confirmedList.add(t);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            walletRep.saveAll(confirmedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}