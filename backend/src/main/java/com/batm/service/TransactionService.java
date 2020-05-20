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
    private TransactionRecordRep recRep;

    @Autowired
    private TransactionRecordGiftRep giftRep;

    @Autowired
    private TransactionRecordWalletRep walletRep;

    @Autowired
    private TransactionRecordC2CRep c2cRep;

    @Autowired
    private TransactionRecordReserveRep reserveRep;

    @Autowired
    private TradeRep tradeRep;

    @Autowired
    private UserCoinRep userCoinRep;

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

    public TransactionDetailsDTO getTransactionDetails(Long userId, CoinService.CoinEnum coin, String txId) {
        User user = userService.findById(userId);

        TransactionDetailsDTO dto = new TransactionDetailsDTO();
        Optional<TransactionRecord> buySellTx;

        if (org.apache.commons.lang.StringUtils.isNumeric(txId)) {  /** consider as txDbId */
            buySellTx = recRep.findById(Long.valueOf(txId));
        } else {                                                    /** consider as txId */
            String address = user.getCoinAddress(coin.name());
            dto = coin.getTransaction(txId, address);
            buySellTx = recRep
                    .findOneByIdentityAndDetailAndCryptoCurrency(user.getIdentity(), txId, coin.name());
        }

        Optional<TransactionRecordGift> giftTx = giftRep.findOneByIdentityAndTxIdAndCoinCode(user.getIdentity(), txId, coin.name());
        Optional<TransactionRecordC2C> c2cTx = c2cRep.findOneByIdentityAndTxIdAndCoinCode(user.getIdentity(), txId, coin.name());

        if (giftTx.isPresent()) {
            TransactionRecordGift gift = giftTx.get();

            dto.setPhone(gift.getPhone());
            dto.setImageId(gift.getImageId());
            dto.setMessage(gift.getMessage());
            dto.setType(TransactionType.convert(dto.getType(), TransactionGroupType.GIFT));
        } else if (c2cTx.isPresent()) {
            TransactionRecordC2C c2c = c2cTx.get();

            String code = c2c.getRefCoin().getCode();
            dto.setRefTxId(c2c.getRefTxId());
            dto.setRefLink(CoinService.CoinEnum.valueOf(code).getExplorerUrl() + "/" + c2c.getRefTxId());
            dto.setRefCoin(code);
            dto.setRefCryptoAmount(c2c.getRefAmount());
            dto.setType(TransactionType.convert(dto.getType(), TransactionGroupType.C2C));
        } else if (buySellTx.isPresent()) {
            TransactionRecord buySell = buySellTx.get();

            // to return either txId or txDbId, not both
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
        String address = user.getCoinAddress(coinCode.name());

        TxListDTO txDTO = new TxListDTO();
        txDTO.setBuySellList(recRep.findAllByIdentityAndCryptoCurrency(user.getIdentity(), coinCode.name()));
        txDTO.setGiftList(giftRep.findAllByIdentityAndCoinCode(user.getIdentity(), coinCode.name()));
        txDTO.setC2cList(c2cRep.findAllByIdentityAndCoinCode(user.getIdentity(), coinCode.name()));

        return coinCode.getTransactionList(address, startIndex, Constant.TRANSACTIONS_COUNT, txDTO);
    }

    public void saveGift(Long userId, CoinService.CoinEnum coinCode, String txId, SubmitTransactionDTO dto) {
        try {
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
                giftRep.save(receiverGiftTx);

                // for wallet history
                TransactionRecordWallet receiverWalletTx = convertGiftToWalletTx(receiverGiftTx);
                walletRep.save(receiverWalletTx);
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

                giftRep.save(senderGiftTx);

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

                    giftRep.save(receiverGiftTx);
                } else {
                    // for wallet history
                    TransactionRecordWallet senderWalletTx = convertGiftToWalletTx(senderGiftTx);
                    walletRep.save(senderWalletTx);
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
            BigDecimal txAmount = recRep.getTransactionsSumByDate(user.getIdentity(), Util.getStartDate(), new Date());

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

            TransactionRecordC2C record = new TransactionRecordC2C();
            record.setTxId(txId);
            record.setIdentity(userService.findById(userId).getIdentity());
            record.setCoin(coinCode.getCoinEntity());
            record.setAmount(dto.getCryptoAmount());
            record.setType(TransactionType.SEND_EXCHANGE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setProfitC2C(coinCode.getCoinEntity().getProfitC2C());
            record.setRefCoin(refCoinCode.getCoinEntity());

            BigDecimal refAmount = dto.getCryptoAmount()
                    .multiply(coinCode.getPrice())
                    .divide(refCoinCode.getPrice(), refCoinCode.getCoinEntity().getScale(), RoundingMode.HALF_DOWN)
                    .multiply(BigDecimal.valueOf(100).subtract(coinCode.getCoinEntity().getProfitC2C()).divide(BigDecimal.valueOf(100)))
                    .setScale(refCoinCode.getCoinEntity().getScale(), BigDecimal.ROUND_DOWN).stripTrailingZeros();

            record.setRefAmount(refAmount);

            c2cRep.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reserve(Long userId, CoinService.CoinEnum coinCode, String txId, SubmitTransactionDTO dto) {
        try {
            TransactionRecordReserve record = new TransactionRecordReserve();
            record.setTxId(txId);
            record.setIdentity(userService.findById(userId).getIdentity());
            record.setCoin(coinCode.getCoinEntity());
            record.setAmount(dto.getCryptoAmount());
            record.setType(TransactionType.RESERVE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());

            reserveRep.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String recall(Long userId, CoinService.CoinEnum coinCode, SubmitTransactionDTO dto) {
        try {
            BigDecimal txFee = coinCode.getCoinSettings().getTxFee();
            BigDecimal withdrawAmount = dto.getCryptoAmount().subtract(txFee);
            BigDecimal walletBalance = walletService.getBalance(coinCode);

            if (walletBalance.compareTo(withdrawAmount.add(txFee)) >= 0) {
                String fromAddress = coinCode.getWalletAddress();
                String toAddress = userService.getUserCoin(userId, coinCode.name()).getAddress();
                String hex = coinCode.sign(fromAddress, toAddress, withdrawAmount);
                String txId = coinCode.submitTransaction(hex);

                if (StringUtils.isNotBlank(txId)) {
                    TransactionRecordReserve record = new TransactionRecordReserve();
                    record.setTxId(txId);
                    record.setIdentity(userService.findById(userId).getIdentity());
                    record.setCoin(coinCode.getCoinEntity());
                    record.setAmount(dto.getCryptoAmount());
                    record.setType(TransactionType.RECALL.getValue());
                    record.setStatus(TransactionStatus.PENDING.getValue());

                    reserveRep.save(record);

                    return txId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Long postTrade(Long userId, CoinService.CoinEnum coinCode, TradeDTO dto) {
        try {
            Trade trade;

            if (dto.getId() == null) {
                trade = new Trade();
                trade.setIdentity(userService.findById(userId).getIdentity());
                trade.setCoin(coinCode.getCoinEntity());
            } else {
                trade = tradeRep.findById(dto.getId()).get();
            }

            trade.setType(dto.getType());
            trade.setPaymentMethod(dto.getPaymentMethod());
            trade.setMargin(dto.getMargin());
            trade.setMinLimit(dto.getMinLimit());
            trade.setMaxLimit(dto.getMaxLimit());
            trade.setTerms(dto.getTerms());

            return tradeRep.save(trade).getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deleteTrade(Long id) {
        tradeRep.deleteById(id);
    }

    public TradeListDTO getTrades(Long userId, CoinService.CoinEnum coinCode, Integer type, Integer index) {
        int page = index / 10;

        if (type == null) {
            TradeListDTO dto = new TradeListDTO();
            dto.setBuyTotal(tradeRep.countTradeByType(TradeType.BUY.getValue()));
            List<Trade> buyTrades = tradeRep.findAllByTypeOrderByMarginAsc(TradeType.BUY.getValue(), PageRequest.of(page, 10));
            dto.setBuyTrades(getTradeDetailsList(buyTrades, coinCode, index));

            dto.setSellTotal(tradeRep.countTradeByType(TradeType.SELL.getValue()));
            List<Trade> sellTrades = tradeRep.findAllByTypeOrderByMarginDesc(TradeType.SELL.getValue(), PageRequest.of(page, 10));
            dto.setSellTrades(getTradeDetailsList(sellTrades, coinCode, index));

            return dto;
        } else {
            if (type == TradeType.BUY.getValue()) {
                List<Trade> trades = tradeRep.findAllByTypeOrderByMarginAsc(type, PageRequest.of(page, 10));

                TradeListDTO dto = new TradeListDTO();
                dto.setBuyTotal(tradeRep.countTradeByType(type));
                dto.setBuyTrades(getTradeDetailsList(trades, coinCode, index));

                return dto;
            } else if (type == TradeType.SELL.getValue()) {
                List<Trade> trades = tradeRep.findAllByTypeOrderByMarginDesc(type, PageRequest.of(page, 10));

                TradeListDTO dto = new TradeListDTO();
                dto.setSellTotal(tradeRep.countTradeByType(type));
                dto.setSellTrades(getTradeDetailsList(trades, coinCode, index));

                return dto;
            }
        }

        return null;
    }

    @Scheduled(fixedDelay = 300_000) //5 min
    public void processCronTasks() {
        completePendingGifts();
        completePendingWallets();
        completePendingExchange();
        completePendingReserve();
        completePendingRecall();

        chainalysisSubmitting();
        deliverReservedGifts();
        deliverReservedC2C();
    }

    private void completePendingGifts() {
        try {
            List<TransactionRecordGift> list = giftRep.findByStatusAndHoursAgo(TransactionStatus.PENDING.getValue(), 2, PageRequest.of(0, 50));
            List<TransactionRecordGift> confirmedList = massStatusCheck(list);

            giftRep.saveAll(confirmedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completePendingWallets() {
        try {
            List<TransactionRecordWallet> list = walletRep.findByStatusAndHoursAgo(TransactionStatus.PENDING.getValue(), 2, PageRequest.of(0, 50));
            List<TransactionRecordWallet> confirmedList = massStatusCheck(list);

            walletRep.saveAll(confirmedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completePendingExchange() {
        try {
            List<TransactionRecordC2C> list = c2cRep.findByStatusAndHoursAgo(TransactionStatus.PENDING.getValue(), 2, PageRequest.of(0, 50));
            List<TransactionRecordC2C> confirmedList = massStatusCheck(list);

            c2cRep.saveAll(confirmedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completePendingReserve() {
        try {
            List<TransactionRecordReserve> list = reserveRep.findByTypeAndStatusAndHoursAgo(TransactionType.RESERVE.getValue(), TransactionStatus.PENDING.getValue(), 2, PageRequest.of(0, 50));
            List<TransactionRecordReserve> confirmedList = massStatusCheck(list);

            confirmedList.forEach(e -> {
                UserCoin userCoin = userService.getUserCoin(e.getIdentity().getUser().getId(), e.getCoin().getCode());
                userCoin.setReservedBalance(userCoin.getReservedBalance().add(e.getAmount()));

                userCoinRep.save(userCoin);
                reserveRep.save(e);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completePendingRecall() {
        try {
            List<TransactionRecordReserve> list = reserveRep.findByTypeAndStatusAndHoursAgo(TransactionType.RECALL.getValue(), TransactionStatus.PENDING.getValue(), 2, PageRequest.of(0, 50));
            List<TransactionRecordReserve> confirmedList = massStatusCheck(list);

            confirmedList.forEach(e -> {
                UserCoin userCoin = userService.getUserCoin(e.getIdentity().getUser().getId(), e.getCoin().getCode());
                userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(e.getAmount()));

                userCoinRep.save(userCoin);
                reserveRep.save(e);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T extends BaseTxEntity> List<T> massStatusCheck(List<T> list) {
        List<T> confirmedList = new ArrayList<>();

        list.stream().forEach(t -> {
            try {
                CoinService.CoinEnum coinId = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                TransactionStatus status = coinId.getTransactionStatus(t.getTxId());

                if (status != null) {
                    t.setStatus(status.getValue());

                    confirmedList.add(t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return confirmedList;
    }

    private void chainalysisSubmitting() {
        try {
            List<TransactionRecord> list = recRep.findNotTrackedTransactions(PageRequest.of(0, 50));

            if (!list.isEmpty()) {
                List<TransactionRecord> listRes = chainalysisService.process(list);
                recRep.saveAll(listRes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverReservedC2C() {
        try {
            List<TransactionRecordC2C> list = c2cRep.findByTypeAndStatusAndRefTxIdNullAndHoursAgo(TransactionType.SEND_EXCHANGE.getValue(), TransactionStatus.COMPLETE.getValue(), 2, PageRequest.of(0, 10));
            List<TransactionRecordC2C> confirmedList = new ArrayList<>();

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
                            TransactionRecordC2C c2cRec = new TransactionRecordC2C();
                            c2cRec.setTxId(txId);
                            c2cRec.setIdentity(identity);
                            c2cRec.setCoin(coinCode.getCoinEntity());
                            c2cRec.setAmount(t.getRefAmount());
                            c2cRec.setType(TransactionType.RECEIVE_EXCHANGE.getValue());
                            c2cRec.setStatus(TransactionStatus.PENDING.getValue());
                            c2cRec.setProfitC2C(t.getProfitC2C());
                            c2cRec.setRefCoin(t.getCoin());
                            c2cRec.setRefAmount(t.getAmount());
                            c2cRec.setRefTxId(t.getTxId());

                            c2cRep.save(c2cRec);

                            t.setRefTxId(txId);
                            confirmedList.add(t);

                            TransactionRecordWallet walletRec = new TransactionRecordWallet();
                            walletRec.setTxId(c2cRec.getTxId());
                            walletRec.setAmount(c2cRec.getAmount());
                            walletRec.setCoin(c2cRec.getCoin());
                            walletRec.setStatus(c2cRec.getStatus());
                            walletRec.setType(c2cRec.getType());
                            walletRec.setTransactionRecordC2C(c2cRec);

                            walletRep.save(walletRec);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            c2cRep.saveAll(confirmedList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverReservedGifts() {
        try {
            List<TransactionRecordGift> list = giftRep.findByTypeAndStatusAndStepAndDaysAgo(
                    TransactionType.SEND_GIFT.getValue(),
                    TransactionStatus.COMPLETE.getValue(),
                    TransactionRecordGift.RECEIVER_NOT_EXIST,
                    7,
                    PageRequest.of(0, 10));
            List<TransactionRecordGift> confirmedList = new ArrayList<>();

            list.stream().forEach(t -> {
                try {
                    Optional<User> receiver = userService.findByPhone(t.getPhone());

                    if (receiver.isPresent()) {
                        CoinService.CoinEnum coinCode = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                        BigDecimal txFee = coinCode.getCoinSettings().getTxFee();
                        BigDecimal withdrawAmount = t.getAmount().subtract(txFee);
                        BigDecimal walletBalance = walletService.getBalance(coinCode);

                        if (walletBalance.compareTo(withdrawAmount.add(txFee)) >= 0) {
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

                            if (StringUtils.isNotBlank(txId)) {
                                saveGift(t.getIdentity().getUser().getId(), coinCode, txId, dto);

                                t.setReceiverStatus(TransactionRecordGift.RECEIVER_EXIST);
                                confirmedList.add(t);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            giftRep.saveAll(confirmedList);
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

    private List<TradeDetailsDTO> getTradeDetailsList(List<Trade> trades, CoinService.CoinEnum coinCode, Integer index) {
        List<TradeDetailsDTO> list = new LinkedList<>();

        for (int i = 0; i < trades.size(); i++) {
            Trade trade = trades.get(i);

            TradeDetailsDTO details = new TradeDetailsDTO();
            details.setId(trade.getId());
            details.setIndex(index + i);
            details.setUsername(trade.getIdentity().getPublicId());
            details.setTradeCount(50);
            details.setRate(new BigDecimal("4.5"));
            details.setDistance(2);
            details.setPrice(coinCode.getPrice().multiply(trade.getMargin().divide(new BigDecimal(100)).add(BigDecimal.ONE)));
            details.setPaymentMethod(trade.getPaymentMethod());
            details.setMinLimit(trade.getMinLimit());
            details.setMaxLimit(trade.getMaxLimit());
            details.setTerms(trade.getTerms());

            list.add(details);
        }

        return list;
    }
}