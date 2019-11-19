package com.batm.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import com.batm.dto.*;
import com.batm.entity.*;
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

    private static List<Coin> coinList;

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

    private static String btcExplorerUrl;
    private static String ethExplorerUrl;
    private static String bchExplorerUrl;
    private static String ltcExplorerUrl;

    public CoinService(@Autowired final BinanceApiRestClient binanceRest,
                       @Autowired final MessageService messageService,
                       @Autowired final WalletService walletService,
                       @Autowired final UserService userService,
                       @Autowired final TransactionService transactionService,

                       @Autowired final CoinRepository coinRepository,

                       @Autowired final BlockbookService blockbook,
                       @Autowired final BinanceService binance,
                       @Autowired final RippledService rippled,
                       @Autowired final TrongridService trongrid,

                       @Value("${btc.node.url}") final String btcNodeUrl,
                       @Value("${eth.node.url}") final String ethNodeUrl,
                       @Value("${bch.node.url}") final String bchNodeUrl,
                       @Value("${ltc.node.url}") final String ltcNodeUrl,

                       @Value("${btc.explorer.url}") final String btcExplorerUrl,
                       @Value("${eth.explorer.url}") final String ethExplorerUrl,
                       @Value("${bch.explorer.url}") final String bchExplorerUrl,
                       @Value("${ltc.explorer.url}") final String ltcExplorerUrl) {

        CoinService.binanceRest = binanceRest;
        CoinService.messageService = messageService;
        CoinService.walletService = walletService;
        CoinService.userService = userService;
        CoinService.transactionService = transactionService;

        CoinService.coinList = coinRepository.findAll();

        CoinService.blockbook = blockbook;
        CoinService.binance = binance;
        CoinService.rippled = rippled;
        CoinService.trongrid = trongrid;

        CoinService.btcNodeUrl = btcNodeUrl;
        CoinService.ethNodeUrl = ethNodeUrl;
        CoinService.bchNodeUrl = bchNodeUrl;
        CoinService.ltcNodeUrl = ltcNodeUrl;

        CoinService.btcExplorerUrl = btcExplorerUrl;
        CoinService.ethExplorerUrl = ethExplorerUrl;
        CoinService.bchExplorerUrl = bchExplorerUrl;
        CoinService.ltcExplorerUrl = ltcExplorerUrl;
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
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return blockbook.getTransactionNumber(btcNodeUrl, address, amount, Constant.BTC_DIVIDER, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return blockbook.getTransactionStatus(btcNodeUrl, txId);
            }

            @Override
            public TransactionDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(btcNodeUrl, btcExplorerUrl, txId, address, Constant.BTC_DIVIDER);
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
            public NonceDTO getNonce(Long userId) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(Long userId) {
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
            public SubmitTransactionDTO sign(String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = blockbook.submitTransaction(btcNodeUrl, transaction);
                saveGift(userId, this, txId, transaction);

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
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return blockbook.getTransactionNumber(ethNodeUrl, address, amount, Constant.ETH_DIVIDER, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return blockbook.getTransactionStatus(ethNodeUrl, txId);
            }

            @Override
            public TransactionDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(ethNodeUrl, ethExplorerUrl, txId, address, Constant.ETH_DIVIDER);
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
            public NonceDTO getNonce(Long userId) {
                User user = userService.findById(userId);
                String address = user.getCoinAddress(this.getName());

                return blockbook.getNonce(ethNodeUrl, address);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(Long userId) {
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
            public SubmitTransactionDTO sign(String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = blockbook.submitTransaction(ethNodeUrl, transaction);
                saveGift(userId, this, txId, transaction);

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
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return blockbook.getTransactionNumber(bchNodeUrl, address, amount, Constant.BCH_DIVIDER, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return blockbook.getTransactionStatus(bchNodeUrl, txId);
            }

            @Override
            public TransactionDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(bchNodeUrl, bchExplorerUrl, txId, address, Constant.BCH_DIVIDER);
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
            public NonceDTO getNonce(Long userId) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(Long userId) {
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
            public SubmitTransactionDTO sign(String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = blockbook.submitTransaction(bchNodeUrl, transaction);
                saveGift(userId, this, txId, transaction);

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
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return blockbook.getTransactionNumber(ltcNodeUrl, address, amount, Constant.LTC_DIVIDER, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return blockbook.getTransactionStatus(ltcNodeUrl, txId);
            }

            @Override
            public TransactionDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(ltcNodeUrl, ltcExplorerUrl, txId, address, Constant.LTC_DIVIDER);
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
            public NonceDTO getNonce(Long userId) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(Long userId) {
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
            public SubmitTransactionDTO sign(String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = blockbook.submitTransaction(ltcNodeUrl, transaction);
                saveGift(userId, this, txId, transaction);

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
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
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
            public NonceDTO getNonce(Long userId) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(Long userId) {
                User user = userService.findById(userId);
                String address = user.getCoinAddress(this.getName());

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
            public SubmitTransactionDTO sign(String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = binance.submitTransaction(transaction);
                saveGift(userId, this, txId, transaction);

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
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
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
            public NonceDTO getNonce(Long userId) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(Long userId) {
                User user = userService.findById(userId);
                String address = user.getCoinAddress(this.getName());

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
            public SubmitTransactionDTO sign(String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = rippled.submitTransaction(transaction);
                saveGift(userId, this, txId, transaction);

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
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
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
            public NonceDTO getNonce(Long userId) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(Long userId) {
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
            public SubmitTransactionDTO sign(String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = trongrid.submitTransaction(transaction);
                saveGift(userId, this, txId, transaction);

                return txId;
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);

        public abstract String getName();

        public abstract TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type);

        public abstract TransactionStatus getTransactionStatus(String txId);

        public abstract TransactionDTO getTransaction(String txId, String address);

        public abstract TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, List<TransactionRecordGift> gifts, List<TransactionRecord> txs);

        public abstract UtxoDTO getUTXO(String xpub);

        public abstract NonceDTO getNonce(Long userId);

        public abstract CurrentAccountDTO getCurrentAccount(Long userId);

        public abstract CurrentBlockDTO getCurrentBlock();

        public abstract String getWalletAddress();

        public abstract SubmitTransactionDTO sign(String toAddress, BigDecimal amount);

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
            dto.setImageId(txGift.getImageId());
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
        List<UserCoin> userCoins = userService.getUserCoins(userId);

        List<CompletableFuture<CoinBalanceDTO>> futures = userCoins.stream()
                .filter(it -> coins.contains(it.getCoin().getCode()))
                .map(dto -> callAsync(dto))
                .collect(Collectors.toList());

        List<CoinBalanceDTO> balances = futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(CoinBalanceDTO::getId))
                .collect(Collectors.toList());

        BigDecimal totalBalance = Util.format2(balances.stream()
                .map(it -> it.getPrice().getUsd().multiply(it.getBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return new BalanceDTO(userId, new AmountDTO(totalBalance), balances);
    }

    public FeeDTO getCoinsFee(List<String> coins) {
        List<CoinFeeDTO> feeList = new ArrayList<>();

        coinList.forEach(e -> {
            if (coins.contains(e.getCode())) {
                if (CoinEnum.ETH.name().equalsIgnoreCase(e.getCode())) {
                    feeList.add(new CoinFeeDTO(e.getCode(), null, Constant.GAS_PRICE, Constant.GAS_LIMIT));
                } else {
                    feeList.add(new CoinFeeDTO(e.getCode(), e.getFee().stripTrailingZeros(), null, null));
                }
            }
        });

        return new FeeDTO(feeList);
    }

    private CompletableFuture<CoinBalanceDTO> callAsync(UserCoin userCoin) {
        return CompletableFuture.supplyAsync(() -> {
            CoinEnum coinEnum = CoinEnum.valueOf(userCoin.getCoin().getCode());

            BigDecimal coinPrice = coinEnum.getPrice();
            BigDecimal coinBalance = coinEnum.getBalance(userCoin.getAddress());

            return new CoinBalanceDTO(userCoin.getCoin().getId(), userCoin.getCoin().getCode(), userCoin.getAddress(), coinBalance, new AmountDTO(coinPrice));
        });
    }

    private Coin getCoin(List<Coin> coins, String coinCode) {
        return coins.stream().filter(e -> e.getCode().equalsIgnoreCase(coinCode)).findFirst().get();
    }

    public void save(CoinDTO coinVM, Long userId) {
        User user = userService.findById(userId);
        List<UserCoin> userCoins = userService.getUserCoins(userId);

        List<UserCoin> newCoins = new ArrayList<>();
        coinVM.getCoinList().stream().forEach(coinDTO -> {
            Coin coin = getCoin(coinList, coinDTO.getCode());

            if (coin != null) {
                UserCoin userCoin = new UserCoin(user, coin, coinDTO.getAddress());

                if (userCoins.indexOf(userCoin) < 0) {
                    newCoins.add(userCoin);
                }
            }
        });

        userService.save(newCoins);
    }

    public boolean compareCoins(CoinDTO coinDTO, Long userId) {
        List<UserCoin> userCoins = userService.getUserCoins(userId);

        for (UserCoinDTO reqCoin : coinDTO.getCoinList()) {
            for (UserCoin userCoin : userCoins) {
                if (reqCoin.getCode().equalsIgnoreCase(userCoin.getCoin().getCode())) {
                    if (!reqCoin.getAddress().equalsIgnoreCase(userCoin.getAddress())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static void saveGift(Long userId, CoinEnum coinId, String txId, SubmitTransactionDTO dto) {
        if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == dto.getType()) {
            User user = userService.findById(userId);

            Optional<User> receiver = userService.findByPhone(dto.getPhone());
            messageService.sendGiftMessage(coinId, dto, receiver.isPresent());

            transactionService.saveGift(user.getIdentity(), txId, user.getCoin(coinId.name()), dto, receiver.isPresent());
        }
    }
}