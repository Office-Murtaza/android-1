package com.belco.server.service;

import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.repository.CoinRep;
import com.belco.server.dto.*;
import com.belco.server.entity.Coin;
import com.belco.server.entity.User;
import com.belco.server.entity.UserCoin;
import com.belco.server.util.Util;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wallet.core.jni.CoinType;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class CoinService {

    @Value("${swap.profit-percent}")
    private BigDecimal swapProfitPercent;

    private static Map<String, Coin> coinMap;
    public static Map<String, Map<Long, List<String>>> wsMap = new ConcurrentHashMap<>();

    private static WalletService walletService;
    private static UserService userService;

    private static BlockbookService blockbookService;
    private static GethService gethService;
    private static BinanceService binanceService;
    private static RippledService rippledService;
    private static TrongridService trongridService;
    private static CacheService cacheService;
    private static SimpMessagingTemplate simpMessagingTemplate;

    public CoinService(@Autowired final WalletService walletService,
                       @Autowired @Lazy final UserService userService,
                       @Autowired final CoinRep coinRep,
                       @Autowired final CacheService cacheService,
                       @Autowired final SimpMessagingTemplate simpMessagingTemplate,

                       @Autowired final BlockbookService blockbookService,
                       @Autowired final GethService gethService,
                       @Autowired final BinanceService binanceService,
                       @Autowired final RippledService rippledService,
                       @Autowired final TrongridService trongridService) {

        CoinService.walletService = walletService;
        CoinService.userService = userService;

        CoinService.coinMap = coinRep.findAll().stream().collect(Collectors.toMap(Coin::getCode, Function.identity()));
        CoinService.cacheService = cacheService;
        CoinService.simpMessagingTemplate = simpMessagingTemplate;

        CoinService.blockbookService = blockbookService;
        CoinService.gethService = gethService;
        CoinService.binanceService = binanceService;
        CoinService.rippledService = rippledService;
        CoinService.trongridService = trongridService;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void wsStompBalance() {
        wsMap.forEach((k, v) -> sendStompBalance(k, (Long) v.keySet().toArray()[0], v.get((Long) v.keySet().toArray()[0])));
    }

    public void sendStompBalance(String phone, Long userId, List<String> coins) {
        simpMessagingTemplate.convertAndSendToUser(phone, "/queue/balance", getCoinsBalance(userId, coins));
    }

    public BalanceDTO getCoinsBalance(Long userId, List<String> coins) {
        if (coins == null || coins.isEmpty()) {
            return new BalanceDTO(BigDecimal.ZERO, BigDecimal.ZERO.toString(), Collections.EMPTY_LIST);
        }

        List<UserCoin> userCoins = userService.getUserCoins(userId);

        List<CompletableFuture<CoinBalanceDTO>> futures = userCoins.stream()
                .filter(it -> coins.contains(it.getCoin().getCode()))
                .map(dto -> callAsync(dto))
                .collect(Collectors.toList());

        List<CoinBalanceDTO> balances = futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(CoinBalanceDTO::getIdx))
                .collect(Collectors.toList());

        BigDecimal totalBalance = Util.format(balances.stream()
                .map(it -> it.getPrice().multiply(it.getBalance().add(it.getReservedBalance())))
                .reduce(BigDecimal.ZERO, BigDecimal::add), 2);

        return new BalanceDTO(totalBalance, totalBalance.toString(), balances);
    }

    public void addUserCoins(User user, List<CoinDTO> coins) {
        List<UserCoin> userCoins = new ArrayList<>();

        coins.stream().forEach(e -> {
            boolean coinExist = user.getUserCoins().stream().anyMatch(t -> t.getCoin().getCode().equalsIgnoreCase(e.getCode()));

            if (!coinExist) {
                Coin coin = coinMap.get(e.getCode());

                userCoins.add(new UserCoin(user, coin, e.getAddress(), BigDecimal.ZERO));

                if (CoinEnum.valueOf(e.getCode()) == CoinEnum.ETH) {
                    gethService.addAddressToJournal(e.getAddress());
                }
            }
        });

        userService.save(userCoins);
    }

    public boolean isCoinsAddressMatch(User user, List<CoinDTO> coins) {
        for (CoinDTO reqCoin : coins) {
            for (UserCoin userCoin : user.getUserCoins()) {
                if (reqCoin.getCode().equalsIgnoreCase(userCoin.getCoin().getCode())) {
                    if (!reqCoin.getAddress().equalsIgnoreCase(userCoin.getAddress())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public CoinDetailsDTO getCoinDetails(CoinService.CoinEnum coin) {
        CoinDetailsDTO dto = new CoinDetailsDTO();
        dto.setCode(coin.name());
        dto.setByteFee(coin.getByteFee());
        dto.setTxFee(coin.getTxFee());
        dto.setGasPrice(coin.getGasPrice());
        dto.setGasLimit(coin.getGasLimit());
        dto.setScale(coin.getCoinEntity().getScale());
        dto.setSwapProfitPercent(swapProfitPercent);
        dto.setWalletAddress(coin.getWalletAddress());
        dto.setContractAddress(coin.getContractAddress());

        if (coin == CoinEnum.CATM || coin == CoinEnum.USDT) {
            dto.setConvertedTxFee(walletService.convertToFee(coin));
        }

        return dto;
    }

    public boolean enableCoin(Long userId, CoinEnum coin, boolean enabled) {
        User user = userService.findById(userId);

        if (wsMap.containsKey(user.getPhone())) {
            List<String> coins = wsMap.get(user.getPhone()).get(userId);

            if (!enabled && coins.contains(coin.name())) {
                coins.remove(coin.name());
            } else if (enabled && !coins.contains(coin.name())) {
                coins.add(coin.name());
            }

            sendStompBalance(user.getPhone(), userId, coins);

            return true;
        }

        return false;
    }

    private CompletableFuture<CoinBalanceDTO> callAsync(UserCoin userCoin) {
        return CompletableFuture.supplyAsync(() -> {
            CoinEnum coinEnum = CoinEnum.valueOf(userCoin.getCoin().getCode());

            Integer scale = coinEnum.getCoinEntity().getScale();
            BigDecimal coinPrice = coinEnum.getPrice();
            BigDecimal coinBalance = coinEnum.getBalance(userCoin.getAddress()).setScale(scale, BigDecimal.ROUND_DOWN).stripTrailingZeros();
            BigDecimal coinFiatBalance = Util.format(coinBalance.multiply(coinPrice), 3);
            BigDecimal reservedBalance = userCoin.getReservedBalance().stripTrailingZeros();
            BigDecimal reservedFiatBalance = Util.format(reservedBalance.multiply(coinPrice), 3);

            CoinBalanceDTO dto = new CoinBalanceDTO();
            dto.setId(userCoin.getCoin().getId());
            dto.setCode(userCoin.getCoin().getCode());
            dto.setIdx(userCoin.getCoin().getIdx());
            dto.setAddress(userCoin.getAddress());
            dto.setBalance(coinBalance);
            dto.setFiatBalance(coinFiatBalance);
            dto.setReservedBalance(reservedBalance);
            dto.setReservedFiatBalance(reservedFiatBalance);
            dto.setPrice(coinPrice);

            return dto;
        });
    }

    private static BigDecimal getPriceById(String id) {
        return cacheService.getPriceById(id);
    }

    public enum CoinEnum {
        BTC {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbookService.getBalance(getCoinType(), address);
            }

            @Override
            public Long getByteFee() {
                return blockbookService.getByteFee(getCoinType());
            }

            @Override
            public BigDecimal getTxFee() {
                return blockbookService.getTxFee(getCoinType());
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit() {
                return null;
            }

            @Override
            public String getName() {
                return "bitcoin";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return blockbookService.getTransactionNumber(getCoinType(), address, amount, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbookService.getTransaction(getCoinType(), txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbookService.getTransactionList(getCoinType(), address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbookService.getUTXO(getCoinType(), xpub);
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
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(getCoinType())).getUtxos();
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BITCOIN;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return blockbookService.getExplorerUrl(getCoinType());
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        BCH {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbookService.getBalance(getCoinType(), address);
            }

            @Override
            public Long getByteFee() {
                return blockbookService.getByteFee(getCoinType());
            }

            @Override
            public BigDecimal getTxFee() {
                return blockbookService.getTxFee(getCoinType());
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit() {
                return null;
            }

            @Override
            public String getName() {
                return "bitcoin-cash";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return blockbookService.getTransactionNumber(getCoinType(), address, amount, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbookService.getTransaction(getCoinType(), txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbookService.getTransactionList(getCoinType(), address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbookService.getUTXO(getCoinType(), xpub);
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
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(getCoinType())).getUtxos();
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BITCOINCASH;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return blockbookService.getExplorerUrl(getCoinType());
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        LTC {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbookService.getBalance(getCoinType(), address);
            }

            @Override
            public Long getByteFee() {
                return blockbookService.getByteFee(getCoinType());
            }

            @Override
            public BigDecimal getTxFee() {
                return blockbookService.getTxFee(getCoinType());
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit() {
                return null;
            }

            @Override
            public String getName() {
                return "litecoin";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return blockbookService.getTransactionNumber(getCoinType(), address, amount, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbookService.getTransaction(getCoinType(), txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbookService.getTransactionList(getCoinType(), address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbookService.getUTXO(getCoinType(), xpub);
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
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(getCoinType())).getUtxos();
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.LITECOIN;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return blockbookService.getExplorerUrl(getCoinType());
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        DASH {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbookService.getBalance(getCoinType(), address);
            }

            @Override
            public Long getByteFee() {
                return blockbookService.getByteFee(getCoinType());
            }

            @Override
            public BigDecimal getTxFee() {
                return blockbookService.getTxFee(getCoinType());
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit() {
                return null;
            }

            @Override
            public String getName() {
                return "dash";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return blockbookService.getTransactionNumber(getCoinType(), address, amount, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbookService.getTransaction(getCoinType(), txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbookService.getTransactionList(getCoinType(), address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbookService.getUTXO(getCoinType(), xpub);
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
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(getCoinType())).getUtxos();
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.DASH;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return blockbookService.getExplorerUrl(getCoinType());
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        DOGE {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbookService.getBalance(getCoinType(), address);
            }

            @Override
            public Long getByteFee() {
                return blockbookService.getByteFee(getCoinType());
            }

            @Override
            public BigDecimal getTxFee() {
                return blockbookService.getTxFee(getCoinType());
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit() {
                return null;
            }

            @Override
            public String getName() {
                return "dogecoin";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return blockbookService.getTransactionNumber(getCoinType(), address, amount, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbookService.getTransaction(getCoinType(), txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbookService.getTransactionList(getCoinType(), address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbookService.getUTXO(getCoinType(), xpub);
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
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(getCoinType())).getUtxos();
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.DOGECOIN;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return blockbookService.getExplorerUrl(getCoinType());
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        ETH {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return gethService.getBalance(address);
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return gethService.getTxFee(getGasLimit(), getGasPrice());
            }

            @Override
            public Long getGasPrice() {
                return gethService.getAvgGasPrice();
            }

            @Override
            public Long getGasLimit() {
                return gethService.getGasLimit(getWalletAddress());
            }

            @Override
            public String getName() {
                return "ethereum";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return gethService.getTransactionStatus(txId);
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return gethService.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return gethService.getTransactionList(address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return new NonceDTO(gethService.getNonce(address));
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
                return walletService.getCoinsMap().get(CoinType.ETHEREUM).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return gethService.sign(fromAddress, toAddress, amount, getGasLimit(), getGasPrice());
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return gethService.submitTransaction(dto);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.ETHEREUM;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return gethService.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return GethService.explorerUrl;
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        CATM {
            @Override
            public BigDecimal getPrice() {
                return new BigDecimal("0.1");
            }

            @Override
            public BigDecimal getBalance(String address) {
                return GethService.ERC20.CATM.getBalance(address);
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return gethService.getTxFee(getGasLimit(), getGasPrice());
            }

            @Override
            public Long getGasPrice() {
                return gethService.getFastGasPrice();
            }

            @Override
            public Long getGasLimit() {
                return gethService.getGasLimit(GethService.ERC20.CATM.getContractAddress());
            }

            @Override
            public String getName() {
                return "catm";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return gethService.getTransactionStatus(txId);
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return gethService.getTransaction(GethService.ERC20.CATM, txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return gethService.getTransactionList(GethService.ERC20.CATM, address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return new NonceDTO(gethService.getNonce(address));
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
                return walletService.getCoinsMap().get(CoinType.ETHEREUM).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return gethService.sign(GethService.ERC20.CATM, fromAddress, toAddress, amount, getGasLimit(), getGasPrice());
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return gethService.submitTransaction(GethService.ERC20.CATM, dto);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.ETHEREUM;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return gethService.getNodeTransactions(GethService.ERC20.CATM, address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return GethService.explorerUrl;
            }

            @Override
            public String getContractAddress() {
                return GethService.ERC20.CATM.getContractAddress();
            }
        },
        USDT {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return GethService.ERC20.USDT.getBalance(address);
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return gethService.getTxFee(getGasLimit(), getGasPrice());
            }

            @Override
            public Long getGasPrice() {
                return gethService.getFastGasPrice();
            }

            @Override
            public Long getGasLimit() {
                return gethService.getGasLimit(GethService.ERC20.USDT.getContractAddress());
            }

            @Override
            public String getName() {
                return "tether";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return gethService.getTransactionStatus(txId);
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return gethService.getTransaction(GethService.ERC20.USDT, txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return gethService.getTransactionList(GethService.ERC20.USDT, address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return new NonceDTO(gethService.getNonce(address));
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
                return walletService.getCoinsMap().get(CoinType.ETHEREUM).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return gethService.sign(GethService.ERC20.USDT, fromAddress, toAddress, amount, getGasLimit(), getGasPrice());
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return gethService.submitTransaction(GethService.ERC20.USDT, dto);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.ETHEREUM;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return gethService.getNodeTransactions(GethService.ERC20.USDT, address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return GethService.explorerUrl;
            }

            @Override
            public String getContractAddress() {
                return GethService.ERC20.USDT.getContractAddress();
            }
        },
        BNB {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return binanceService.getBalance(address);
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return getCoinEntity().getFee().stripTrailingZeros();
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit() {
                return null;
            }

            @Override
            public String getName() {
                return "binancecoin";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return binanceService.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return binanceService.getTransactionList(address, startIndex, limit, txDTO);
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
                return binanceService.getCurrentAccount(address);
            }

            @Override
            public CurrentBlockDTO getCurrentBlock() {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return binanceService.sign(fromAddress, toAddress, amount);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return binanceService.submitTransaction(dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BINANCE;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return binanceService.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return binanceService.getExplorerUrl();
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        XRP {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return rippledService.getBalance(address);
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return getCoinEntity().getFee().stripTrailingZeros();
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit() {
                return null;
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
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return rippledService.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return rippledService.getTransactionList(address, startIndex, limit, txDTO);
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
                return rippledService.getCurrentAccount(address);
            }

            @Override
            public CurrentBlockDTO getCurrentBlock() {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                BigDecimal balance = getBalance(fromAddress);
                BigDecimal fee = getCoinEntity().getFee();
                BigDecimal maxWithdrawAmount = balance.subtract(new BigDecimal(20).subtract(fee));

                if (maxWithdrawAmount.compareTo(amount) < 0) {
                    amount = maxWithdrawAmount;
                }

                return rippledService.sign(fromAddress, toAddress, amount, fee);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return rippledService.submitTransaction(dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.XRP;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return rippledService.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return rippledService.getExplorerUrl();
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        TRX {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return trongridService.getBalance(address);
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return getCoinEntity().getFee().stripTrailingZeros();
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit() {
                return null;
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
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return trongridService.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return trongridService.getTransactionList(address, startIndex, limit, txDTO);
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
                return trongridService.getCurrentBlock();
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return trongridService.sign(fromAddress, toAddress, amount, getCoinEntity().getFee());
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return trongridService.submitTransaction(dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.TRON;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return trongridService.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return trongridService.getExplorerUrl();
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);

        public abstract Long getByteFee();

        public abstract BigDecimal getTxFee();

        public abstract Long getGasPrice();

        public abstract Long getGasLimit();

        public abstract String getName();

        public abstract TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type);

        public abstract TransactionStatus getTransactionStatus(String txId);

        public abstract TransactionDetailsDTO getTransaction(String txId, String address);

        public abstract TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO);

        public abstract UtxoDTO getUTXO(String xpub);

        public abstract NonceDTO getNonce(String address);

        public abstract CurrentAccountDTO getCurrentAccount(String address);

        public abstract CurrentBlockDTO getCurrentBlock();

        public abstract String getWalletAddress();

        public abstract String sign(String fromAddress, String toAddress, BigDecimal amount);

        public abstract String submitTransaction(SubmitTransactionDTO dto);

        public abstract CoinType getCoinType();

        public abstract NodeTransactionsDTO getNodeTransactions(String address);

        public abstract Coin getCoinEntity();

        public abstract String getExplorerUrl();

        public abstract String getContractAddress();
    }
}