package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.repository.TransactionRecordGiftRep;
import com.batm.repository.TransactionRecordRep;
import com.batm.util.Constant;
import com.batm.util.Util;
import com.twilio.rest.api.v2010.account.Message;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@EnableScheduling
public class TransactionService {

    @Autowired
    private TransactionRecordRep transactionRecordRep;

    @Autowired
    private TransactionRecordGiftRep transactionRecordGiftRep;

    @Autowired
    private UserService userService;

    @Autowired
    private ChainalysisService chainalysisService;

    @Autowired
    private MessageService messageService;

    @Value("${gb.url}")
    private String gbUrl;

    public void saveGift(Identity identity, String txId, Coin coin, SubmitTransactionDTO dto, boolean receiverExist) {
        try {
            TransactionRecordGift gift = new TransactionRecordGift();
            gift.setTxId(txId);
            gift.setType(dto.getType());
            gift.setAmount(dto.getCryptoAmount());
            gift.setStatus(TransactionStatus.PENDING.getValue());
            gift.setPhone(dto.getPhone());
            gift.setMessage(dto.getMessage());
            gift.setImageId(dto.getImageId());
            gift.setStep(receiverExist ? Constant.GIFT_USER_EXIST : Constant.GIFT_USER_NOT_EXIST);
            gift.setIdentity(identity);
            gift.setCoin(coin);
            gift.setRefTxId(dto.getRefTxId());

            transactionRecordGiftRep.save(gift);

            if (receiverExist) {
                TransactionRecordGift gift2 = new TransactionRecordGift();
                gift2.setTxId(gift.getTxId());
                gift2.setType(TransactionType.RECEIVE_GIFT.getValue());
                gift2.setStatus(TransactionStatus.PENDING.getValue());
                gift2.setPhone(gift.getPhone());
                gift2.setMessage(gift.getMessage());
                gift2.setImageId(gift.getImageId());
                gift2.setStep(Constant.GIFT_USER_EXIST);
                gift2.setIdentity(userService.findByPhone(gift.getPhone()).get().getIdentity());
                gift2.setCoin(gift.getCoin());
                gift2.setAmount(gift.getAmount());

                transactionRecordGiftRep.save(gift2);
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
            BigDecimal dailyLimit = user.getIdentity().getLimitCashPerDay().get(0).getAmount();
            BigDecimal txLimit = user.getIdentity().getLimitCashPerTransaction().get(0).getAmount();

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

    @Scheduled(fixedDelay = 60_000)
    public void processCronTasks() {
        processPendingGifts();
        notifySellTransactions();
        processNotTrackedTransactions();
    }

    private void processPendingGifts() {
        try {
            List<TransactionRecordGift> list = transactionRecordGiftRep.findByStatus(TransactionStatus.PENDING.getValue(), PageRequest.of(0, 10));
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

    public void processNotTrackedTransactions() {
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

    public void notifySellTransactions() {
        try {
            List<TransactionRecord> list = transactionRecordRep.findNotNotifiedSellTransactions(PageRequest.of(0, 50));

            if (!list.isEmpty()) {
                list.stream().forEach(e -> {
                    User user = e.getIdentity().getUser();

                    if (user != null) {
                        Message.Status status = messageService.sendMessage(user.getPhone(), "Your sell transaction is confirmed");
                        System.out.println("status:" + status);

                        if (status != null) {
                            e.setNotified(1);
                        } else {
                            e.setNotified(2);
                        }
                    } else {
                        e.setNotified(3);
                    }
                });

                transactionRecordRep.saveAll(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processStoredGifts() {
        try {
            List<TransactionRecordGift> list = transactionRecordGiftRep.findByTypeAndStatusAndStep(TransactionType.SEND_GIFT.getValue(), TransactionStatus.COMPLETE.getValue(), Constant.GIFT_USER_NOT_EXIST, PageRequest.of(0, 10));
            List<TransactionRecordGift> confirmedList = new ArrayList<>();

            list.stream().forEach(t -> {
                try {
                    if (userService.findByPhone(t.getPhone()).isPresent()) {
                        CoinService.CoinEnum coinCode = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                        String hex = coinCode.sign(t.getIdentity().getUser().getCoinAddress(t.getCoin().getCode()), t.getAmount());

                        SubmitTransactionDTO dto = new SubmitTransactionDTO();
                        dto.setHex(hex);
                        dto.setRefTxId(t.getTxId());
                        dto.setType(TransactionType.SEND_GIFT.getValue());
                        dto.setPhone(t.getPhone());
                        dto.setImageId(t.getImageId());
                        dto.setMessage(t.getMessage());

                        String txId = coinCode.submitTransaction(t.getIdentity().getUser().getId(), dto);

                        if (StringUtils.isNotEmpty(txId)) {
                            t.setStep(Constant.GIFT_USER_TRANSACTION_CREATED);

                            confirmedList.add(t);
                        }
                    }
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
}