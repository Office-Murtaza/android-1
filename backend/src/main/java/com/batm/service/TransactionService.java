package com.batm.service;

import com.batm.dto.AmountDTO;
import com.batm.dto.PreSubmitDTO;
import com.batm.dto.SubmitTransactionDTO;
import com.batm.dto.UserLimitDTO;
import com.batm.entity.Coin;
import com.batm.entity.Identity;
import com.batm.entity.TransactionRecordGift;
import com.batm.entity.User;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.repository.TransactionRecordGiftRepository;
import com.batm.repository.TransactionRecordRepository;
import com.batm.util.Constant;
import com.batm.util.Util;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @Autowired
    private TransactionRecordGiftRepository transactionRecordGiftRepository;

    @Autowired
    private UserService userService;

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
            gift.setImage(dto.getImageId());
            gift.setStep(receiverExist ? Constant.GIFT_USER_EXIST : Constant.GIFT_USER_NOT_EXIST);
            gift.setIdentity(identity);
            gift.setCoin(coin);
            gift.setRefTxId(dto.getRefTxId());

            transactionRecordGiftRepository.save(gift);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserLimitDTO getUserTransactionLimits(Long userId) {
        UserLimitDTO dto = new UserLimitDTO();
        dto.setDailyLimit(new AmountDTO(BigDecimal.ZERO));
        dto.setTxLimit(new AmountDTO(BigDecimal.ZERO));
        dto.setSellProfitRate(new AmountDTO(BigDecimal.ONE));

        try {
            User user = userService.findById(userId);
            BigDecimal txAmount = transactionRecordRepository.getTransactionsSumByDate(user.getIdentity(), Util.getStartDate(), new Date());
            BigDecimal dailyLimit = user.getIdentity().getLimitCashPerDay().get(0).getAmount();
            BigDecimal txLimit = user.getIdentity().getLimitCashPerTransaction().get(0).getAmount();

            if (txAmount != null) {
                dailyLimit.subtract(txAmount);
            }

            dto.setDailyLimit(new AmountDTO(Util.format2(dailyLimit)));
            dto.setTxLimit(new AmountDTO(Util.format2(txLimit)));
            dto.setSellProfitRate(new AmountDTO(new BigDecimal("1.025")));
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

    @Scheduled(fixedDelay = 600_000)
    public void processCronTasks() {
        processPendingGifts();
        processStoredGifts();
    }

    private void processPendingGifts() {
        try {
            List<TransactionRecordGift> list = transactionRecordGiftRepository.findByStatus(TransactionStatus.PENDING.getValue(), PageRequest.of(0, 10));
            List<TransactionRecordGift> confirmedList = new ArrayList<>();

            list.stream().forEach(t -> {
                try {
                    CoinService.CoinEnum coinId = CoinService.CoinEnum.valueOf(t.getCoin().getId());
                    TransactionStatus status = coinId.getTransactionStatus(t.getTxId());

                    if (status == TransactionStatus.COMPLETE) {
                        t.setStatus(TransactionStatus.COMPLETE.getValue());

                        if (t.getStep() == Constant.GIFT_USER_EXIST) {
                            TransactionRecordGift gift = new TransactionRecordGift();
                            gift.setTxId(t.getTxId());
                            gift.setType(TransactionType.RECEIVE_GIFT.getValue());
                            gift.setStatus(TransactionStatus.COMPLETE.getValue());
                            gift.setPhone(t.getPhone());
                            gift.setMessage(t.getMessage());
                            gift.setImage(t.getImage());
                            gift.setStep(0);
                            gift.setIdentity(userService.findByPhone(t.getPhone()).get().getIdentity());
                            gift.setCoin(t.getCoin());
                            gift.setAmount(t.getAmount());

                            confirmedList.add(gift);
                        }

                        confirmedList.add(t);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            transactionRecordGiftRepository.saveAll(confirmedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processStoredGifts() {
        try {
            List<TransactionRecordGift> list = transactionRecordGiftRepository.findByTypeAndStatusAndStep(TransactionType.SEND_GIFT.getValue(), TransactionStatus.COMPLETE.getValue(), Constant.GIFT_USER_NOT_EXIST, PageRequest.of(0, 10));
            List<TransactionRecordGift> confirmedList = new ArrayList<>();

            list.stream().forEach(t -> {
                try {
                    if (userService.findByPhone(t.getPhone()).isPresent()) {
                        //create TW transaction
                        //submit transaction
                        t.setStep(Constant.GIFT_USER_TRANSACTION_CREATED);

                        confirmedList.add(t);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            transactionRecordGiftRepository.saveAll(confirmedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}