package com.batm.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.Error;
import com.batm.model.Response;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.batm.repository.CoinRepository;
import com.batm.dto.BalanceDTO;
import com.batm.dto.CoinDTO;
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

    private static String btcNodeUrl;
    private static String ethNodeUrl;
    private static String bchNodeUrl;
    private static String ltcNodeUrl;

    public CoinService(@Autowired final BinanceApiRestClient binanceRest,
                       @Autowired final MessageService messageService,
                       @Autowired final WalletService walletService,
                       @Autowired final UserService userService,
                       @Autowired final TransactionService transactionService,
                       @Autowired final BlockbookService blockbook,
                       @Autowired final BinanceService binance,
                       @Autowired final RippledService rippled,
                       @Autowired final TrongridService trongrid,
                       @Value("${btc.node.url}") final String btcNodeUrl,
                       @Value("${eth.node.url}") final String ethNodeUrl,
                       @Value("${bch.node.url}") final String bchNodeUrl,
                       @Value("${ltc.node.url}") final String ltcNodeUrl) {

        CoinService.binanceRest = binanceRest;
        CoinService.messageService = messageService;
        CoinService.walletService = walletService;
        CoinService.userService = userService;
        CoinService.transactionService = transactionService;

        CoinService.blockbook = blockbook;
        CoinService.binance = binance;
        CoinService.rippled = rippled;
        CoinService.trongrid = trongrid;

        CoinService.btcNodeUrl = btcNodeUrl;
        CoinService.ethNodeUrl = ethNodeUrl;
        CoinService.bchNodeUrl = bchNodeUrl;
        CoinService.ltcNodeUrl = ltcNodeUrl;
    }

    public enum CoinEnum {
        BTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binanceRest.getPrice("BTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbook.getBalance(btcNodeUrl, address, Constant.BTC_DIVIDER);
            }

            @Override
            public String getName() {
                return "bitcoin";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return blockbook.getTransactionNumber(btcNodeUrl, address, amount, Constant.BTC_DIVIDER);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return null;
            }

            @Override
            public TransactionDTO getTransaction(String txId, String address) {
                return null;
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
                return blockbook.getTransactionList(btcNodeUrl, address, Constant.BTC_DIVIDER, startIndex, limit, gifts, txs);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbook.getUTXO(btcNodeUrl, xpub);
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
                String txId = blockbook.submitTransaction(btcNodeUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    saveGift(userId, this, txId, transaction);
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
                return blockbook.getBalance(ethNodeUrl, address, Constant.ETH_DIVIDER);
            }

            @Override
            public String getName() {
                return "ethereum";
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
            public TransactionDTO getTransaction(String txId, String address) {
                return null;
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
                return blockbook.getTransactionList(ethNodeUrl, address, Constant.ETH_DIVIDER, startIndex, limit, gifts, txs);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return blockbook.getNonce(ethNodeUrl, address);
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
                String txId = blockbook.submitTransaction(ethNodeUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    saveGift(userId, this, txId, transaction);
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
                return blockbook.getBalance(bchNodeUrl, address, Constant.BCH_DIVIDER);
            }

            @Override
            public String getName() {
                return "bitcoincash";
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
            public TransactionDTO getTransaction(String txId, String address) {
                return null;
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
                return blockbook.getTransactionList(bchNodeUrl, address, Constant.BCH_DIVIDER, startIndex, limit, gifts, txs);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbook.getUTXO(bchNodeUrl, xpub);
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
                String txId = blockbook.submitTransaction(bchNodeUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    saveGift(userId, this, txId, transaction);
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
                return blockbook.getBalance(ltcNodeUrl, address, Constant.LTC_DIVIDER);
            }

            @Override
            public String getName() {
                return "litecoin";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return blockbook.getTransactionNumber(ltcNodeUrl, address, amount, Constant.LTC_DIVIDER);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return null;
            }

            @Override
            public TransactionDTO getTransaction(String txId, String address) {
                return null;
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
                return blockbook.getTransactionList(ltcNodeUrl, address, Constant.LTC_DIVIDER, startIndex, limit, gifts, txs);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbook.getUTXO(ltcNodeUrl, xpub);
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
                String txId = blockbook.submitTransaction(ltcNodeUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    saveGift(userId, this, txId, transaction);
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
            public String getName() {
                return "binance";
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
            public TransactionDTO getTransaction(String txId, String address) {
                return binance.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
                return binance.getTransactionList(address, startIndex, limit, gifts, txs);
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
                    saveGift(userId, this, txId, transaction);
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
            public String getName() {
                return "ripple";
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
            public TransactionDTO getTransaction(String txId, String address) {
                return rippled.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
                return rippled.getTransactionList(address, startIndex, limit, gifts, txs);
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
                    saveGift(userId, this, txId, transaction);
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
            public String getName() {
                return "tron";
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
            public TransactionDTO getTransaction(String txId, String address) {
                return trongrid.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs) {
                return trongrid.getTransactionList(address, startIndex, limit, gifts, txs);
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
                    saveGift(userId, this, txId, transaction);
                }

                return txId;
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);

        public abstract String getName();

        public abstract TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount);

        public abstract TransactionStatus getTransactionStatus(String txId);

        public abstract TransactionDTO getTransaction(String txId, String address);

        public abstract TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs);

        public abstract UtxoDTO getUTXO(String xpub);

        public abstract NonceDTO getNonce(String address);

        public abstract CurrentAccountDTO getCurrentAccount(String address);

        public abstract CurrentBlockDTO getCurrentBlock();

        public abstract String getWalletAddress();

        public abstract String submitTransaction(Long userId, SubmitTransactionDTO transaction);
    }

    public TransactionDTO getTransaction(Long userId, CoinEnum coin, String txId) {
        User user = userService.findById(userId);
        String address = user.getCoinAddress(coin.name());
        TransactionRecord txRecord = user.getIdentity().getTxRecord(txId, coin.name());
        TransactionRecordGift txGift = user.getIdentity().getTxGift(txId, coin.name());

        TransactionDTO dto = coin.getTransaction(txId, address);

        if (txGift != null) {
            dto.setPhone(txGift.getPhone());
            dto.setImageId(txGift.getImage());
            dto.setMessage(txGift.getMessage());
            dto.setType(TransactionType.getGiftType(dto.getType()));
        } else if (txRecord != null) {
            dto.setSellInfo(coin.getName() + ":" + txRecord.getCryptoAddress() + "?" + txRecord.getCryptoAmount() + "&" + txRecord.getRemoteTransactionId() + "&" + txRecord.getUuid());
            dto.setType(TransactionType.getTxType(dto.getType()));
        }

        return dto;
    }

    public TransactionListDTO getTransactions(Long userId, CoinEnum coin, Integer startIndex) {
        User user = userService.findById(userId);
        String address = user.getCoinAddress(coin.name());
        List<TransactionRecordGift> gifts = user.getIdentity().getTxGiftList(coin.name());
        List<TransactionRecord> txs = user.getIdentity().getTxRecordList(coin.name());

        return coin.getTransactionList(address, startIndex, Constant.TRANSACTION_LIMIT, gifts, txs);
    }

    public BalanceDTO getCoinsBalance(Long userId, List<String> coins) {
        if (coins == null || coins.isEmpty()) {
            return new BalanceDTO(userId, new AmountDTO(BigDecimal.ZERO), new ArrayList<>());
        }

        List<UserCoin> userCoins = userService.getUserCoins(userId);

        List<CompletableFuture<com.batm.dto.CoinBalanceDTO>> futures = userCoins.stream()
                .filter(it -> coins.contains(it.getCoin().getId()))
                .map(dto -> callAsync(dto))
                .collect(Collectors.toList());

        List<com.batm.dto.CoinBalanceDTO> balances = futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(com.batm.dto.CoinBalanceDTO::getOrderIndex))
                .collect(Collectors.toList());

        BigDecimal totalBalance = Util.format2(balances.stream()
                .map(it -> it.getPrice().getUsd().multiply(it.getBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return new BalanceDTO(userId, new AmountDTO(totalBalance), balances);
    }

    private CompletableFuture<com.batm.dto.CoinBalanceDTO> callAsync(UserCoin userCoin) {
        return CompletableFuture.supplyAsync(() -> {
            CoinEnum coinEnum = CoinEnum.valueOf(userCoin.getCoin().getId());

            BigDecimal coinPrice = coinEnum.getPrice();
            BigDecimal coinBalance = coinEnum.getBalance(userCoin.getPublicKey());

            return new com.batm.dto.CoinBalanceDTO(userCoin.getCoin().getId(), userCoin.getPublicKey(), coinBalance, new AmountDTO(coinPrice), userCoin.getCoin().getOrderIndex());
        });
    }

    private Coin getCoin(List<Coin> coins, String coinId) {
        return coins.stream().filter(e -> e.getId().equalsIgnoreCase(coinId)).findFirst().get();
    }

    public void save(CoinDTO coinVM, Long userId) {
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

    public Response compareCoins(CoinDTO coinDTO, Long userId) {
        List<UserCoin> userCoins = userService.getUserCoins(userId);

        for (UserCoinDTO userCoin : coinDTO.getCoins()) {
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

    public static void saveGift(Long userId, CoinEnum coinId, String txId, SubmitTransactionDTO dto) {
        User user = userService.findById(userId);

        Optional<User> receiver = userService.findByPhone(dto.getPhone());
        messageService.sendGiftMessage(coinId, dto, receiver.isPresent());

        transactionService.saveGift(user.getIdentity(), txId, user.getCoin(coinId.name()), dto, receiver.isPresent());
    }
}