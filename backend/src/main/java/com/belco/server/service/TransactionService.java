package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.entity.*;
import com.belco.server.model.*;
import com.belco.server.repository.TransactionRecordRep;
import com.belco.server.repository.TransactionRecordWalletRep;
import com.belco.server.repository.UserCoinRep;
import com.belco.server.util.Util;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final WalletService walletService;

    @Value("${gb.url}")
    private String gbUrl;
    @Value("${stake.base-period}")
    private int stakeBasePeriod;
    @Value("${stake.hold-period}")
    private int stakeHoldPeriod;
    @Value("${stake.annual-period}")
    private int stakeAnnualPeriod;
    @Value("${stake.annual-percent}")
    private int stakeAnnualPercent;
    @Value("${swap.profit-percent}")
    private BigDecimal swapProfitPercent;

    public TransactionService(TransactionRecordRep recordRep, TransactionRecordWalletRep walletRep, UserCoinRep userCoinRep, UserService userService, TwilioService twilioService, WalletService walletService) {
        this.recordRep = recordRep;
        this.walletRep = walletRep;
        this.userCoinRep = userCoinRep;
        this.userService = userService;
        this.twilioService = twilioService;
        this.walletService = walletService;
    }

    public TransactionDetailsDTO getTransactionDetails(Long userId, CoinService.CoinEnum coinCode, String txId) {
        User user = userService.findById(userId);
        Identity identity = user.getIdentity();
        Coin coin = coinCode.getCoinEntity();

        TransactionDetailsDTO dto = new TransactionDetailsDTO();
        Optional<TransactionRecord> buySellRecOpt;

        if (org.apache.commons.lang.StringUtils.isNumeric(txId)) {  /** consider as txDbId */
            buySellRecOpt = recordRep.findById(Long.valueOf(txId));
        } else {                                                    /** consider as txId */
            String address = user.getUserCoin(coinCode.name()).getAddress();
            dto = coinCode.getTransactionDetails(txId, address);
            buySellRecOpt = recordRep.findOneByIdentityAndDetailAndCryptoCurrency(user.getIdentity(), txId, coinCode.name());
        }

        Optional<TransactionRecordWallet> giftRecOpt = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.TRANSFER_SEND.getValue(), TransactionType.TRANSFER_RECEIVE.getValue()));
        Optional<TransactionRecordWallet> swapRecOpt = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.SWAP_SEND.getValue(), TransactionType.SWAP_RECEIVE.getValue()));
        Optional<TransactionRecordWallet> stakeRecOpt = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.CREATE_STAKE.getValue(), TransactionType.CANCEL_STAKE.getValue(), TransactionType.WITHDRAW_STAKE.getValue()));
        Optional<TransactionRecordWallet> reserveRecOpt = walletRep.findFirstByIdentityAndCoinAndTxIdAndTypeIn(identity, coin, txId, Arrays.asList(TransactionType.RESERVE.getValue(), TransactionType.RECALL.getValue()));

        if (giftRecOpt.isPresent()) {
            TransactionRecordWallet gift = giftRecOpt.get();

            if (gift.getType() == TransactionType.TRANSFER_SEND.getValue()) {
                dto.setToPhone(gift.getToPhone());
            } else if (gift.getType() == TransactionType.TRANSFER_RECEIVE.getValue()) {
                dto.setFromPhone(gift.getFromPhone());
            }

            dto.setImageId(gift.getImageId());
            dto.setMessage(gift.getMessage());
            dto.setType(TransactionType.convert(dto.getType(), TransactionType.valueOf(gift.getType())));
        } else if (swapRecOpt.isPresent()) {
            TransactionRecordWallet exchange = swapRecOpt.get();

            String code = exchange.getRefCoin().getCode();
            dto.setRefTxId(exchange.getRefTxId());
            dto.setRefLink(CoinService.CoinEnum.valueOf(code).getExplorerUrl() + "/" + exchange.getRefTxId());
            dto.setRefCoin(code);
            dto.setRefCryptoAmount(exchange.getRefAmount());
            dto.setType(TransactionType.convert(dto.getType(), TransactionType.valueOf(exchange.getType())));
        } else if (buySellRecOpt.isPresent()) {
            TransactionRecord buySell = buySellRecOpt.get();

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
        } else if (stakeRecOpt.isPresent()) {
            dto.setType(TransactionType.valueOf(stakeRecOpt.get().getType()));
        } else if (reserveRecOpt.isPresent()) {
            dto.setType(TransactionType.valueOf(reserveRecOpt.get().getType()));
        }

        return dto;
    }

    public TransactionHistoryDTO getTransactionHistory(Long userId, CoinService.CoinEnum coinCode, Integer startIndex) {
        User user = userService.findById(userId);
        Identity identity = user.getIdentity();
        Coin coin = coinCode.getCoinEntity();
        String address = user.getUserCoin(coinCode.name()).getAddress();

        List<TransactionRecord> transactionRecords = recordRep.findAllByIdentityAndCryptoCurrency(user.getIdentity(), coinCode.name());
        List<TransactionRecordWallet> transactionRecordWallets = walletRep.findAllByIdentityAndCoin(identity, coin);

        return coinCode.getTransactionHistory(address, startIndex, 10, transactionRecords, transactionRecordWallets);
    }

    public void saveGift(Long userId, CoinService.CoinEnum coinCode, String txId, SubmitTransactionDTO dto) {
        try {
            User user = userService.findById(userId);
            Optional<User> receiverOpt = userService.findByPhone(dto.getPhone());
            Coin coin = userService.getUserCoin(userId, coinCode.name()).getCoin();

            if (BooleanUtils.isTrue(dto.getFromServerWallet())) {
                TransactionRecordWallet record = new TransactionRecordWallet();
                record.setTxId(txId);
                record.setType(TransactionType.TRANSFER_RECEIVE.getValue());
                record.setStatus(TransactionStatus.PENDING.getValue());
                record.setFromPhone(user.getPhone());
                record.setToPhone(dto.getPhone());
                record.setMessage(dto.getMessage());
                record.setImageId(dto.getImageId());
                record.setReceiverStatus(TransactionRecordWallet.RECEIVER_EXIST);
                record.setIdentity(receiverOpt.get().getIdentity());
                record.setCoin(coin);
                record.setAmount(dto.getCryptoAmount());

                walletRep.save(record);
            } else {
                twilioService.sendGiftMessage(coinCode, dto, receiverOpt.isPresent());

                TransactionRecordWallet sendRecord = new TransactionRecordWallet();
                sendRecord.setTxId(txId);
                sendRecord.setType(TransactionType.TRANSFER_SEND.getValue());
                sendRecord.setAmount(dto.getCryptoAmount());
                sendRecord.setStatus(TransactionStatus.PENDING.getValue());
                sendRecord.setFromPhone(user.getPhone());
                sendRecord.setToPhone(dto.getPhone());
                sendRecord.setMessage(dto.getMessage());
                sendRecord.setImageId(dto.getImageId());
                sendRecord.setReceiverStatus(receiverOpt.isPresent() ? TransactionRecordWallet.RECEIVER_EXIST : TransactionRecordWallet.RECEIVER_NOT_EXIST);
                sendRecord.setIdentity(user.getIdentity());
                sendRecord.setCoin(coin);
                sendRecord.setRefTxId(dto.getRefTxId());

                walletRep.save(sendRecord);

                if (receiverOpt.isPresent()) {
                    TransactionRecordWallet receiveRecord = new TransactionRecordWallet();
                    receiveRecord.setTxId(sendRecord.getTxId());
                    receiveRecord.setType(TransactionType.TRANSFER_RECEIVE.getValue());
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
                }
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

    public PreSubmitDTO preSubmit(Long userId, CoinService.CoinEnum coinId, SubmitTransactionDTO transaction) {
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

    public void swap(Long userId, CoinService.CoinEnum coin, String txId, SubmitTransactionDTO dto) {
        try {
            CoinService.CoinEnum refCoin = CoinService.CoinEnum.valueOf(dto.getRefCoin());

            TransactionRecordWallet record = new TransactionRecordWallet();
            record.setTxId(txId);
            record.setIdentity(userService.findById(userId).getIdentity());
            record.setCoin(coin.getCoinEntity());
            record.setAmount(dto.getCryptoAmount());
            record.setType(TransactionType.SWAP_SEND.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setProfitPercent(swapProfitPercent);
            record.setRefCoin(refCoin.getCoinEntity());
            record.setRefAmount(dto.getRefCryptoAmount());

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

    public Response recall(Long userId, CoinService.CoinEnum coinCode, SubmitTransactionDTO dto) {
        try {
            UserCoin userCoin = userService.getUserCoin(userId, coinCode.name());
            BigDecimal reserved = userCoin.getReservedBalance();
            BigDecimal txFee = coinCode == CoinService.CoinEnum.CATM ? dto.getFee() : coinCode.getTxFee();

            if (walletService.isEnoughBalance(coinCode, dto.getCryptoAmount()) && reserved.compareTo(dto.getCryptoAmount().add(txFee)) >= 0) {
                String fromAddress = coinCode.getWalletAddress();
                String toAddress = userCoin.getAddress();
                String hex = coinCode.sign(fromAddress, toAddress, dto.getCryptoAmount());

                SubmitTransactionDTO submit = new SubmitTransactionDTO();
                submit.setHex(hex);
                submit.setFromAddress(fromAddress);
                submit.setToAddress(toAddress);
                submit.setCryptoAmount(dto.getCryptoAmount());
                submit.setFee(txFee);

                String txId = coinCode.submitTransaction(submit);

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
                    dto.setCryptoAmount(dto.getCryptoAmount());
                    dto.setFee(coinCode.getTxFee());

                    userCoin.setReservedBalance(userCoin.getReservedBalance().subtract(dto.getCryptoAmount().add(txFee)));
                    userCoinRep.save(userCoin);

                    return Response.ok("txId", txId);
                } else {
                    return Response.error(3, "Error create transaction");
                }
            } else {
                return Response.error(2, "Insufficient server wallet balance");
            }
        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError();
        }
    }

    public void createStake(Long userId, CoinService.CoinEnum coin, String txId, BigDecimal amount) {
        try {
            TransactionRecordWallet record = new TransactionRecordWallet();
            record.setIdentity(userService.findByUserId(userId));
            record.setCoin(coin.getCoinEntity());
            record.setType(TransactionType.CREATE_STAKE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setAmount(amount);
            record.setTxId(txId);

            walletRep.save(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelStake(Long userId, CoinService.CoinEnum coin, String txId, BigDecimal amount) {
        try {
            TransactionRecordWallet record = new TransactionRecordWallet();
            Identity identity = userService.findByUserId(userId);

            record.setIdentity(identity);
            record.setCoin(coin.getCoinEntity());
            record.setType(TransactionType.CANCEL_STAKE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setAmount(amount);
            record.setTxId(txId);

            walletRep.save(record);

            Optional<TransactionRecordWallet> createStakeRecOpt = walletRep.findFirstByIdentityAndCoinAndTypeAndStatusOrderByCreateDateDesc(identity, coin.getCoinEntity(), TransactionType.CREATE_STAKE.getValue(), TransactionStatus.COMPLETE.getValue());

            if (createStakeRecOpt.isPresent()) {
                TransactionRecordWallet createStakeRec = createStakeRecOpt.get();
                createStakeRec.setRefTxId(txId);
                walletRep.save(createStakeRec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void withdrawStake(Long userId, CoinService.CoinEnum coin, String txId, BigDecimal amount) {
        try {
            TransactionRecordWallet record = new TransactionRecordWallet();
            Identity identity = userService.findByUserId(userId);

            record.setIdentity(identity);
            record.setCoin(coin.getCoinEntity());
            record.setType(TransactionType.WITHDRAW_STAKE.getValue());
            record.setStatus(TransactionStatus.PENDING.getValue());
            record.setAmount(amount);
            record.setTxId(txId);

            walletRep.save(record);

            Optional<TransactionRecordWallet> cancelStakeRecOpt = walletRep.findFirstByIdentityAndCoinAndTypeAndStatusOrderByCreateDateDesc(identity, coin.getCoinEntity(), TransactionType.CANCEL_STAKE.getValue(), TransactionStatus.COMPLETE.getValue());

            if (cancelStakeRecOpt.isPresent()) {
                TransactionRecordWallet cancelStakeRec = cancelStakeRecOpt.get();
                cancelStakeRec.setRefTxId(txId);
                walletRep.save(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StakingDetailsDTO getStakeDetails(Long userId, CoinService.CoinEnum coin) {
        StakingDetailsDTO dto = new StakingDetailsDTO();
        dto.setStatus(StakeStatus.NOT_EXIST);
        dto.setRewardAnnualPercent(stakeAnnualPercent);
        dto.setHoldPeriod(stakeHoldPeriod / stakeBasePeriod);

        try {
            Identity identity = userService.findByUserId(userId);
            Optional<TransactionRecordWallet> createStakeRecOpt = walletRep.findFirstByIdentityAndCoinAndTypeOrderByCreateDateDesc(identity, coin.getCoinEntity(), TransactionType.CREATE_STAKE.getValue());

            if (createStakeRecOpt.isPresent()) {
                TransactionRecordWallet createStakeRec = createStakeRecOpt.get();

                dto.setStatus(StakeStatus.convert(TransactionType.valueOf(createStakeRec.getType()), TransactionStatus.valueOf(createStakeRec.getStatus())));
                dto.setAmount(createStakeRec.getAmount().stripTrailingZeros());
                dto.setCreateDate(createStakeRec.getCreateDate());

                if (StringUtils.isBlank(createStakeRec.getRefTxId())) {
                    int days = Seconds.secondsBetween(new DateTime(createStakeRec.getCreateDate()), DateTime.now()).getSeconds() / stakeBasePeriod;
                    BigDecimal percent = calculateStakeRewardPercent(days);

                    dto.setDuration(days);
                    dto.setRewardPercent(percent);
                    dto.setRewardAmount(createStakeRec.getAmount().multiply(Util.convertPercentToDecimal(percent)).stripTrailingZeros());
                    dto.setRewardAnnualAmount(createStakeRec.getAmount().multiply(Util.convertPercentToDecimal(BigDecimal.valueOf(stakeAnnualPercent))).stripTrailingZeros());

                    return dto;
                } else {
                    TransactionRecordWallet cancelStakeRec = walletRep.findFirstByTxId(createStakeRec.getRefTxId()).get();

                    int days = Seconds.secondsBetween(new DateTime(createStakeRec.getCreateDate()), new DateTime(cancelStakeRec.getCreateDate())).getSeconds() / stakeBasePeriod;
                    int holdDays = Seconds.secondsBetween(new DateTime(cancelStakeRec.getCreateDate()), DateTime.now()).getSeconds() / stakeBasePeriod;
                    BigDecimal percent = calculateStakeRewardPercent(days);

                    dto.setDuration(days);
                    dto.setRewardPercent(percent);
                    dto.setStatus(StakeStatus.convert(TransactionType.valueOf(cancelStakeRec.getType()), TransactionStatus.valueOf(cancelStakeRec.getStatus())));
                    dto.setCancelDate(cancelStakeRec.getCreateDate());
                    dto.setRewardAmount(createStakeRec.getAmount().multiply(Util.convertPercentToDecimal(percent)).stripTrailingZeros());
                    dto.setRewardAnnualAmount(createStakeRec.getAmount().multiply(Util.convertPercentToDecimal(BigDecimal.valueOf(stakeAnnualPercent))).stripTrailingZeros());
                    dto.setUntilWithdraw(Math.max(0, stakeHoldPeriod / stakeBasePeriod - holdDays));

                    if (StringUtils.isNotBlank(cancelStakeRec.getRefTxId())) {
                        TransactionRecordWallet withdrawStakeRec = walletRep.findFirstByTxId(cancelStakeRec.getRefTxId()).get();
                        dto.setStatus(StakeStatus.convert(TransactionType.valueOf(withdrawStakeRec.getType()), TransactionStatus.valueOf(withdrawStakeRec.getStatus())));
                    }

                    return dto;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void processCronTasks() {
        completePendingRecords();

        deliverReservedGifts();
        deliverSwaps();
    }

    private void completePendingRecords() {
        try {
            List<TransactionRecordWallet> pendingRecords = walletRep.findAllByProcessedAndStatus(ProcessedType.SUCCESS.getValue(), TransactionStatus.PENDING.getValue(), page);
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
            List<TransactionRecordWallet> list = walletRep.findAllByProcessedAndTypeAndStatusAndRefTxIdNull(ProcessedType.SUCCESS.getValue(), TransactionType.SWAP_SEND.getValue(), TransactionStatus.COMPLETE.getValue(), page);

            list.stream().forEach(t -> {
                try {
                    CoinService.CoinEnum coinCode = CoinService.CoinEnum.valueOf(t.getRefCoin().getCode());
                    BigDecimal txFee = coinCode == CoinService.CoinEnum.CATM || coinCode == CoinService.CoinEnum.USDT ? walletService.convertToFee(coinCode) : coinCode.getTxFee();
                    BigDecimal withdrawAmount = t.getRefAmount().subtract(txFee);

                    if (walletService.isEnoughBalance(coinCode, t.getRefAmount())) {
                        Identity identity = t.getIdentity();
                        String fromAddress = coinCode.getWalletAddress();
                        String toAddress = userService.getUserCoin(identity.getUser().getId(), coinCode.name()).getAddress();
                        String hex = coinCode.sign(fromAddress, toAddress, withdrawAmount);

                        SubmitTransactionDTO submit = new SubmitTransactionDTO();
                        submit.setHex(hex);
                        submit.setFromAddress(fromAddress);
                        submit.setToAddress(toAddress);
                        submit.setCryptoAmount(withdrawAmount);

                        String txId = coinCode.submitTransaction(submit);

                        if (StringUtils.isNotBlank(txId)) {
                            TransactionRecordWallet rec = new TransactionRecordWallet();
                            rec.setTxId(txId);
                            rec.setIdentity(identity);
                            rec.setCoin(coinCode.getCoinEntity());
                            rec.setAmount(withdrawAmount);
                            rec.setType(TransactionType.SWAP_RECEIVE.getValue());
                            rec.setStatus(TransactionStatus.PENDING.getValue());
                            rec.setProfitPercent(t.getProfitPercent());
                            rec.setRefCoin(t.getCoin());
                            rec.setRefAmount(t.getAmount());
                            rec.setRefTxId(t.getTxId());

                            walletRep.save(rec);

                            t.setRefTxId(txId);
                        } else {
                            t.setProcessed(ProcessedType.ERROR_CREATE_TRANSACTION.getValue());
                        }
                    } else {
                        t.setProcessed(ProcessedType.INSUFFICIENT_WALLET_BALANCE.getValue());
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

    private void deliverReservedGifts() {
        try {
            List<TransactionRecordWallet> list = walletRep.findAllByProcessedAndTypeAndStatusAndReceiverStatus(ProcessedType.SUCCESS.getValue(),
                    TransactionType.TRANSFER_SEND.getValue(),
                    TransactionStatus.COMPLETE.getValue(),
                    TransactionRecordWallet.RECEIVER_NOT_EXIST, page);

            list.stream().forEach(t -> {
                try {
                    Optional<User> receiverOpt = userService.findByPhone(t.getToPhone());

                    if (receiverOpt.isPresent()) {
                        CoinService.CoinEnum coinCode = CoinService.CoinEnum.valueOf(t.getCoin().getCode());
                        BigDecimal txFee = coinCode == CoinService.CoinEnum.CATM || coinCode == CoinService.CoinEnum.USDT ? walletService.convertToFee(coinCode) : coinCode.getTxFee();
                        BigDecimal withdrawAmount = t.getAmount().subtract(txFee);

                        if (walletService.isEnoughBalance(coinCode, t.getAmount())) {
                            String fromAddress = coinCode.getWalletAddress();
                            String toAddress = userService.getUserCoin(receiverOpt.get().getId(), t.getCoin().getCode()).getAddress();
                            String hex = coinCode.sign(fromAddress, toAddress, withdrawAmount);

                            SubmitTransactionDTO dto = new SubmitTransactionDTO();
                            dto.setHex(hex);
                            dto.setCryptoAmount(withdrawAmount);
                            dto.setRefTxId(t.getTxId());
                            dto.setType(TransactionType.TRANSFER_SEND.getValue());
                            dto.setPhone(t.getToPhone());
                            dto.setImageId(t.getImageId());
                            dto.setMessage(t.getMessage());
                            dto.setFromServerWallet(true);

                            String txId = coinCode.submitTransaction(dto);

                            if (StringUtils.isNotBlank(txId)) {
                                saveGift(t.getIdentity().getUser().getId(), coinCode, txId, dto);

                                t.setReceiverStatus(TransactionRecordWallet.RECEIVER_EXIST);
                            } else {
                                t.setProcessed(ProcessedType.ERROR_CREATE_TRANSACTION.getValue());
                            }
                        } else {
                            t.setProcessed(ProcessedType.INSUFFICIENT_WALLET_BALANCE.getValue());
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

    private BigDecimal calculateStakeRewardPercent(int days) {
        return new BigDecimal(days * stakeAnnualPercent).divide(new BigDecimal(stakeAnnualPeriod / stakeBasePeriod), 2, RoundingMode.HALF_DOWN).stripTrailingZeros();
    }
}