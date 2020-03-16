package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.CashStatus;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.repository.TransactionRecordGiftRep;
import com.batm.repository.TransactionRecordRep;
import com.batm.repository.TransactionRecordWalletRep;
import com.batm.util.Constant;
import com.batm.util.Util;
import com.twilio.rest.api.v2010.account.Message;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
@EnableScheduling
public class TransactionService {

    @Autowired
    private TransactionRecordRep transactionRecordRep;

    @Autowired
    private TransactionRecordGiftRep transactionRecordGiftRep;

    @Autowired
    private TransactionRecordWalletRep transactionRecordWalletRep;

    @Autowired
    private UserService userService;

    @Autowired
    private ChainalysisService chainalysisService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WalletService walletService;

    @Value("${gb.url}")
    private String gbUrl;

    public TransactionDTO getTransactionDetails(Long userId, CoinService.CoinEnum coin, String txId) {
        User user = userService.findById(userId);

        TransactionDTO dto = new TransactionDTO();
        Optional<TransactionRecord> txRecord;

        if (org.apache.commons.lang.StringUtils.isNumeric(txId)) {  /** consider as txDbId */
            txRecord = transactionRecordRep.findById(Long.valueOf(txId));
        } else {                                                    /** consider as txId */
            String address = user.getCoinAddress(coin.name());
            dto = coin.getTransaction(txId, address);
            txRecord = transactionRecordRep
                    .findOneByIdentityAndDetailAndCryptoCurrency(user.getIdentity(), txId, coin.name());
        }

        Optional<TransactionRecordGift> txGift = transactionRecordGiftRep
                .findOneByIdentityAndTxIdAndCoinCode(user.getIdentity(), txId, coin.name());

        if (txGift.isPresent()) {
            TransactionRecordGift txRecordGift = txGift.get();
            dto.setPhone(txRecordGift.getPhone());
            dto.setImageId(txRecordGift.getImageId());
            dto.setMessage(txRecordGift.getMessage());
            dto.setType(TransactionType.getGiftType(dto.getType()));
        } else if (txRecord.isPresent()) {
            TransactionRecord transactionRecord = txRecord.get();
            // to return either txId or txDbId, not both
            if (StringUtils.isBlank(dto.getTxId())) {
                if (StringUtils.isNotBlank(transactionRecord.getDetail())) {
                    dto.setTxId(transactionRecord.getDetail());
                } else {
                    dto.setTxDbId(transactionRecord.getId().toString());
                }
            }

            dto.setType(transactionRecord.getTransactionType());
            dto.setStatus(transactionRecord.getTransactionStatus(dto.getType()));
            dto.setCryptoAmount(transactionRecord.getCryptoAmount().stripTrailingZeros());
            dto.setFiatAmount(transactionRecord.getCashAmount().setScale(0));
            dto.setToAddress(transactionRecord.getCryptoAddress());
            dto.setDate2(transactionRecord.getServerTime());

            if (dto.getType() == TransactionType.SELL) {
                dto.setCashStatus(CashStatus.getCashStatus(transactionRecord.getCanBeCashedOut(), transactionRecord.getWithdrawn()));
                dto.setSellInfo(coin.getName() + ":" + transactionRecord.getCryptoAddress()
                        + "?amount=" + transactionRecord.getCryptoAmount()
                        + "&label=" + transactionRecord.getRemoteTransactionId()
                        + "&uuid=" + transactionRecord.getUuid());
            }
        }

        return dto;
    }

    public TransactionListDTO getTransactionHistory(Long userId, CoinService.CoinEnum coinCode, Integer startIndex) {
        User user = userService.findById(userId);
        String address = user.getCoinAddress(coinCode.name());
        List<TransactionRecordGift> gifts = transactionRecordGiftRep.findAllByIdentityAndCoinCode(user.getIdentity(), coinCode.name());
        List<TransactionRecord> txs = transactionRecordRep.findAllByIdentityAndCryptoCurrency(user.getIdentity(), coinCode.name());

        return coinCode.getTransactionList(address, startIndex, Constant.TRANSACTIONS_COUNT, gifts, txs);
    }

    public void saveGift(Long userId, CoinService.CoinEnum coinCode, String txId, SubmitTransactionDTO dto) {
        try {
            if (TransactionType.SEND_GIFT.getValue() == dto.getType()) {
                if (StringUtils.isNotBlank(txId)) {
                    User user = userService.findById(userId);

                    Optional<User> receiver = userService.findByPhone(dto.getPhone());
                    Coin coin = userService.getUserCoin(userId, coinCode.name()).getCoin();

                    /** gift submission from server wallet */
                    if (BooleanUtils.isTrue(dto.getFromServerWallet())) {
                        TransactionRecordGift receiverGiftTx = new TransactionRecordGift();
                        receiverGiftTx.setTxId(txId);
                        receiverGiftTx.setType(TransactionType.RECEIVE_GIFT.getValue());
                        receiverGiftTx.setStatus(TransactionStatus.PENDING.getValue());
                        receiverGiftTx.setPhone(dto.getPhone());
                        receiverGiftTx.setMessage(dto.getMessage());
                        receiverGiftTx.setImageId(dto.getImageId());
                        receiverGiftTx.setReceiverStatus(TransactionRecordGift.RECEIVER_EXIST);
                        receiverGiftTx.setIdentity(receiver.get().getIdentity());
                        receiverGiftTx.setCoin(coin);
                        receiverGiftTx.setAmount(dto.getCryptoAmount());
                        transactionRecordGiftRep.save(receiverGiftTx);

                        // for wallet history
                        TransactionRecordWallet receiverWalletTx = convertGiftToWalletTx(receiverGiftTx);
                        transactionRecordWalletRep.save(receiverWalletTx);
                    }
                    /** gift submission from API */
                    else {
                        messageService.sendGiftMessage(coinCode, dto, receiver.isPresent());

                        TransactionRecordGift senderGiftTx = new TransactionRecordGift();
                        senderGiftTx.setTxId(txId);
                        senderGiftTx.setType(dto.getType());
                        senderGiftTx.setAmount(dto.getCryptoAmount());
                        senderGiftTx.setStatus(TransactionStatus.PENDING.getValue());
                        senderGiftTx.setPhone(dto.getPhone());
                        senderGiftTx.setMessage(dto.getMessage());
                        senderGiftTx.setImageId(dto.getImageId());
                        senderGiftTx.setReceiverStatus(receiver.isPresent() ? TransactionRecordGift.RECEIVER_EXIST : TransactionRecordGift.RECEIVER_NOT_EXIST);
                        senderGiftTx.setIdentity(user.getIdentity());
                        senderGiftTx.setCoin(coin);
                        senderGiftTx.setRefTxId(dto.getRefTxId());

                        transactionRecordGiftRep.save(senderGiftTx);

                        if (receiver.isPresent()) {
                            TransactionRecordGift receiverGiftTx = new TransactionRecordGift();
                            receiverGiftTx.setTxId(senderGiftTx.getTxId());
                            receiverGiftTx.setType(TransactionType.RECEIVE_GIFT.getValue());
                            receiverGiftTx.setStatus(TransactionStatus.PENDING.getValue());
                            receiverGiftTx.setPhone(senderGiftTx.getPhone());
                            receiverGiftTx.setMessage(senderGiftTx.getMessage());
                            receiverGiftTx.setImageId(senderGiftTx.getImageId());
                            receiverGiftTx.setReceiverStatus(TransactionRecordGift.RECEIVER_EXIST);
                            receiverGiftTx.setIdentity(receiver.get().getIdentity());
                            receiverGiftTx.setCoin(senderGiftTx.getCoin());
                            receiverGiftTx.setAmount(senderGiftTx.getAmount());

                            transactionRecordGiftRep.save(receiverGiftTx);
                        } else {
                            // for wallet history
                            TransactionRecordWallet senderWalletTx = convertGiftToWalletTx(senderGiftTx);
                            transactionRecordWalletRep.save(senderWalletTx);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserLimitDTO getUserTransactionLimits(Long userId) {
        UserLimitDTO dto = new UserLimitDTO();
        dto.setDailyLimit(new AmountDTO(BigDecimal.ZERO));
        dto.setTxLimit(new AmountDTO(BigDecimal.ZERO));
        dto.setSellProfitRate(BigDecimal.ONE);

        try {
            User user = userService.findById(userId);
            BigDecimal txAmount = transactionRecordRep.getTransactionsSumByDate(user.getIdentity(), Util.getStartDate(), new Date());

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

    @Scheduled(fixedDelay = 300_000) //5 min
    public void processCronTasks() {
        updatePendingGifts();
        updatePendingWallets();
        chainalysisSubmitting();
        notifyConfirmedSell();
        deliverReservedGifts();
    }

    private void updatePendingGifts() {
        try {
            List<TransactionRecordGift> list = transactionRecordGiftRep
                    .findByStatusAndHoursAgo(TransactionStatus.PENDING.getValue(), 2, PageRequest.of(0, 50));
            List<TransactionRecordGift> confirmedList = new ArrayList<>();

            list.stream().forEach(t -> {
                try {
                    CoinService.CoinEnum coinId = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                    TransactionStatus status = coinId.getTransactionStatus(t.getTxId());
                    t.setStatus(status.getValue());

                    confirmedList.add(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if (!confirmedList.isEmpty()) {
                transactionRecordGiftRep.saveAll(confirmedList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePendingWallets() {
        try {
            List<TransactionRecordWallet> list = transactionRecordWalletRep
                    .findByStatusAndHoursAgo(TransactionStatus.PENDING.getValue(), 2, PageRequest.of(0, 50));
            List<TransactionRecordWallet> confirmedList = new ArrayList<>();

            list.stream().forEach(t -> {
                try {
                    CoinService.CoinEnum coinId = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                    TransactionStatus status = coinId.getTransactionStatus(t.getTxId());
                    t.setStatus(status.getValue());

                    confirmedList.add(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if (!confirmedList.isEmpty()) {
                transactionRecordWalletRep.saveAll(confirmedList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chainalysisSubmitting() {
        try {
            List<TransactionRecord> list = transactionRecordRep.findNotTrackedTransactions(PageRequest.of(0, 50));

            if (!list.isEmpty()) {
                List<TransactionRecord> listRes = chainalysisService.process(list);
                transactionRecordRep.saveAll(listRes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyConfirmedSell() {
        try {
            List<TransactionRecord> list = transactionRecordRep.findNotNotifiedSellTransactions(PageRequest.of(0, 50));

            if (!list.isEmpty()) {
                list.stream().forEach(e -> {
                    User user = e.getIdentity().getUser();

                    if (user != null) {
                        Message.Status status = messageService.sendMessage(user.getPhone(), "Your sell transaction is confirmed");

                        if (status != null && status == Message.Status.QUEUED) {
                            e.setNotified(Constant.SELL_NOTIFIED);
                        } else {
                            e.setNotified(Constant.SELL_NOT_NOTIFIED);
                        }
                    }
                });

                transactionRecordRep.saveAll(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverReservedGifts() {
        try {
            List<TransactionRecordGift> list = transactionRecordGiftRep.findByTypeAndStatusAndStepAndDaysAgo(
                    TransactionType.SEND_GIFT.getValue(),
                    TransactionStatus.COMPLETE.getValue(),
                    TransactionRecordGift.RECEIVER_NOT_EXIST,
                    7,
                    PageRequest.of(0, 10));
            List<TransactionRecordGift> confirmedGiftList = new ArrayList<>();

            list.stream().forEach(t -> {
                try {
                    Optional<User> receiver = userService.findByPhone(t.getPhone());

                    if (receiver.isPresent()) {
                        CoinService.CoinEnum coinCode = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                        BigDecimal transactionFee = coinCode.getTransactionFee();
                        BigDecimal withdrawAmount = t.getAmount().subtract(transactionFee);
                        BigDecimal walletBalance = walletService.getBalance(coinCode);

                        if (walletBalance != null && walletBalance.compareTo(withdrawAmount.add(transactionFee)) >= 0) {
                            String fromAddress = coinCode.getWalletAddress();
                            String toAddress = userService.getUserCoin(receiver.get().getId(), t.getCoin().getCode()).getAddress();
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
                            saveGift(t.getIdentity().getUser().getId(), coinCode, txId, dto);

                            if (StringUtils.isNotBlank(txId)) {
                                t.setReceiverStatus(TransactionRecordGift.RECEIVER_EXIST);
                                confirmedGiftList.add(t);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if (!confirmedGiftList.isEmpty()) {
                transactionRecordGiftRep.saveAll(confirmedGiftList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TransactionRecordWallet convertGiftToWalletTx(TransactionRecordGift gift) {
        TransactionRecordWallet walletTx = new TransactionRecordWallet();
        walletTx.setTxId(gift.getTxId());
        walletTx.setAmount(gift.getAmount());
        walletTx.setCoin(gift.getCoin());
        walletTx.setStatus(gift.getStatus());
        walletTx.setType(gift.getType());
        walletTx.setTransactionRecordGift(gift);

        return walletTx;
    }
}