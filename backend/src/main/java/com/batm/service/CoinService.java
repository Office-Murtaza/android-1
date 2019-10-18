package com.batm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.Error;
import com.batm.model.Response;
import com.batm.model.TransactionStatus;
import com.batm.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.batm.repository.CoinRepository;
import com.batm.rest.vm.CoinBalanceVM;
import com.batm.rest.vm.CoinVM;
import com.binance.api.client.BinanceApiRestClient;

@Slf4j
@Service
public class CoinService {

    @Autowired
    private CoinRepository coinRepository;

    private static BinanceApiRestClient binanceRest;
    private static MessageService messageService;
    private static WalletService walletService;
    private static UserService userService;
    private static TransactionService transactionService;

    private static BlockbookService blockbook;
    private static BinanceService binance;
    private static RippledService rippled;
    private static TrongridService trongrid;

    private static String btcUrl;
    private static String ethUrl;
    private static String bchUrl;
    private static String ltcUrl;

    public CoinService(@Autowired final BinanceApiRestClient binanceRest,
                       @Autowired final MessageService messageService,
                       @Autowired final WalletService walletService,
                       @Autowired final UserService userService,
                       @Autowired final TransactionService transactionService,
                       @Autowired final BlockbookService blockbook,
                       @Autowired final BinanceService binance,
                       @Autowired final RippledService rippled,
                       @Autowired final TrongridService trongrid,
                       @Value("${btc.url}") final String btcUrl,
                       @Value("${eth.url}") final String ethUrl,
                       @Value("${bch.url}") final String bchUrl,
                       @Value("${ltc.url}") final String ltcUrl) {

        CoinService.binanceRest = binanceRest;
        CoinService.messageService = messageService;
        CoinService.walletService = walletService;
        CoinService.userService = userService;
        CoinService.transactionService = transactionService;

        CoinService.blockbook = blockbook;
        CoinService.binance = binance;
        CoinService.rippled = rippled;
        CoinService.trongrid = trongrid;

        CoinService.btcUrl = btcUrl;
        CoinService.ethUrl = ethUrl;
        CoinService.bchUrl = bchUrl;
        CoinService.ltcUrl = ltcUrl;
    }

    public enum CoinEnum {
        BTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binanceRest.getPrice("BTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbook.getBalance(btcUrl, address, Constant.BTC_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return blockbook.getTransactionNumber(btcUrl, address, amount, Constant.BTC_DIVIDER);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return null;
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return blockbook.getTransactions(btcUrl, address, Constant.BTC_DIVIDER, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbook.getUTXO(btcUrl, xpub);
            }

            @Override
            public NonceDTO getNonce(String address) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public CurrentBlockDTO getCurrentBlock() {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getAddressBTC();
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = blockbook.submitTransaction(btcUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    processGiftTransaction(userId, this, transaction, txId);
                }

                return txId;
            }
        }, ETH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binanceRest.getPrice("ETHUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbook.getBalance(ethUrl, address, Constant.ETH_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return null;
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return blockbook.getTransactions(ethUrl, address, Constant.ETH_DIVIDER, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return blockbook.getNonce(ethUrl, address);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public CurrentBlockDTO getCurrentBlock() {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getAddressETH();
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = blockbook.submitTransaction(ethUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    processGiftTransaction(userId, this, transaction, txId);
                }

                return txId;
            }
        }, BCH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binanceRest.getPrice("BCHABCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbook.getBalance(bchUrl, address, Constant.BCH_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return null;
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return blockbook.getTransactions(bchUrl, address, Constant.BCH_DIVIDER, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbook.getUTXO(bchUrl, xpub);
            }

            @Override
            public NonceDTO getNonce(String address) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public CurrentBlockDTO getCurrentBlock() {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getAddressBCH();
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = blockbook.submitTransaction(bchUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    processGiftTransaction(userId, this, transaction, txId);
                }

                return txId;
            }
        }, LTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binanceRest.getPrice("LTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbook.getBalance(ltcUrl, address, Constant.LTC_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return blockbook.getTransactionNumber(ltcUrl, address, amount, Constant.LTC_DIVIDER);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return null;
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return blockbook.getTransactions(ltcUrl, address, Constant.LTC_DIVIDER, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbook.getUTXO(ltcUrl, xpub);
            }

            @Override
            public NonceDTO getNonce(String address) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public CurrentBlockDTO getCurrentBlock() {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getAddressLTC();
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = blockbook.submitTransaction(ltcUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    processGiftTransaction(userId, this, transaction, txId);
                }

                return txId;
            }
        },
        BNB {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binanceRest.getPrice("BNBUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return binance.getBalance(address);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return binance.getTransactionStatus(txId);
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return binance.getTransactions(address, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return binance.getCurrentAccount(address);
            }

            @Override
            public CurrentBlockDTO getCurrentBlock() {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getAddressBNB();
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = binance.submitTransaction(transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    processGiftTransaction(userId, this, transaction, txId);
                }

                return txId;
            }
        },
        XRP {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binanceRest.getPrice("XRPUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return rippled.getBalance(address);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return rippled.getTransactionStatus(txId);
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return rippled.getTransactions(address, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return rippled.getCurrentAccount(address);
            }

            @Override
            public CurrentBlockDTO getCurrentBlock() {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getAddressXRP();
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = rippled.submitTransaction(transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    processGiftTransaction(userId, this, transaction, txId);
                }

                return txId;
            }
        },
        TRX {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binanceRest.getPrice("TRXUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return trongrid.getBalance(address);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return trongrid.getTransactionStatus(txId);
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return trongrid.getTransactions(address, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public CurrentBlockDTO getCurrentBlock() {
                return trongrid.getCurrentBlock();
            }

            @Override
            public String getWalletAddress() {
                return walletService.getAddressTRX();
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = trongrid.submitTransaction(transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    processGiftTransaction(userId, this, transaction, txId);
                }

                return txId;
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);

        public abstract TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount);

        public abstract TransactionStatus getTransactionStatus(String txId);

        public abstract TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit);

        public abstract UtxoDTO getUTXO(String xpub);

        public abstract NonceDTO getNonce(String address);

        public abstract CurrentAccountDTO getCurrentAccount(String address);

        public abstract CurrentBlockDTO getCurrentBlock();

        public abstract String getWalletAddress();

        public abstract String submitTransaction(Long userId, SubmitTransactionDTO transaction);
    }

    public TransactionResponseDTO getTransactions(Long userId, CoinEnum coin, Integer startIndex) {
        String address = userService.getUserCoin(userId, coin.name()).getPublicKey();
        return coin.getTransactions(address, startIndex, Constant.TRANSACTION_LIMIT);
    }

    public CoinBalanceVM getCoinsBalance(Long userId, List<String> coins) {
        if (coins == null || coins.isEmpty()) {
            return new CoinBalanceVM(userId, new ArrayList<>(), new AmountDTO(BigDecimal.ZERO));
        }

        List<UserCoin> userCoins = userService.getUserCoins(userId);

        List<CompletableFuture<CoinBalanceDTO>> futures = userCoins.stream()
                .filter(it -> coins.contains(it.getCoin().getId()))
                .map(dto -> callAsync(dto))
                .collect(Collectors.toList());

        List<CoinBalanceDTO> balances = futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(CoinBalanceDTO::getOrderIndex))
                .collect(Collectors.toList());

        BigDecimal totalBalance = balances.stream()
                .map(it -> it.getPrice().getUsd().multiply(it.getBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);

        return new CoinBalanceVM(userId, balances, new AmountDTO(totalBalance));
    }

    private CompletableFuture<CoinBalanceDTO> callAsync(UserCoin userCoin) {
        return CompletableFuture.supplyAsync(() -> {
            CoinEnum coinEnum = CoinEnum.valueOf(userCoin.getCoin().getId());

            BigDecimal coinPrice = coinEnum.getPrice();
            BigDecimal coinBalance = coinEnum.getBalance(userCoin.getPublicKey());

            return new CoinBalanceDTO(userCoin.getCoin().getId(), userCoin.getPublicKey(), coinBalance, new AmountDTO(coinPrice), userCoin.getCoin().getOrderIndex());
        });
    }

    private Coin getCoin(List<Coin> coins, String coinId) {
        return coins.stream().filter(e -> e.getId().equalsIgnoreCase(coinId)).findFirst().get();
    }

    public void save(CoinVM coinVM, Long userId) {
        User user = userService.findById(userId);
        List<Coin> coins = coinRepository.findAll();
        List<UserCoin> userCoins = userService.getUserCoins(userId);

        List<UserCoin> newCoins = new ArrayList<>();
        coinVM.getCoins().stream().forEach(coinDTO -> {
            Coin coin = getCoin(coins, coinDTO.getCoinCode());
            if (coin != null) {
                UserCoin userCoin = new UserCoin(user, coin, coinDTO.getPublicKey());
                if (userCoins.indexOf(userCoin) < 0) {
                    newCoins.add(userCoin);
                }
            }
        });

        userService.save(newCoins);
    }

    public Response compareCoins(CoinVM coinVM, Long userId) {
        List<UserCoin> userCoins = userService.getUserCoins(userId);

        for (UserCoinDTO userCoin : coinVM.getCoins()) {
            UserCoin tempUserCoin = new UserCoin(userCoin.getCoinCode());
            int index = userCoins.indexOf(tempUserCoin);
            if (index < 0) {
                return Response.error(new Error(3, "Coin does not exist"));
            }

            String dbPublicKey = userCoins.get(index).getPublicKey();
            if (userCoin.getPublicKey() == null
                    || !userCoin.getPublicKey().equalsIgnoreCase(dbPublicKey)) {
                return Response.error(new Error(3, "Public keys not match"));
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("isCoinsMatched", true);

        return Response.ok(response);
    }

    public static void processGiftTransaction(Long userId, CoinEnum coinId, SubmitTransactionDTO dto, String txId) {
        Identity sender = userService.findById(userId).getIdentity();
        Coin coin = userService.getUserCoin(userId, coinId.name()).getCoin();

        Optional<User> receiver = userService.findByPhone(dto.getPhone());
        messageService.sendGiftMessage(coinId, dto, receiver.isPresent());

        transactionService.saveGift(sender, txId, coin, dto, receiver.isPresent());
    }
}