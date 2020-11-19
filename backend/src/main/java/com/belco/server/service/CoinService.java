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

    private static Map<String, Coin> coinMap;
    public static Map<String, Map<Long, List<String>>> wsMap = new ConcurrentHashMap<>();

    private static WalletService walletService;
    private static UserService userService;

    private static BlockbookService blockbook;
    private static GethService geth;
    private static BinanceService binance;
    private static RippledService rippled;
    private static TrongridService trongrid;
    private static CacheService cache;
    private static SimpMessagingTemplate simp;

    public CoinService(@Autowired final WalletService walletService,
                       @Autowired @Lazy final UserService userService,
                       @Autowired final CoinRep coinRep,
                       @Autowired final CacheService cache,
                       @Autowired final SimpMessagingTemplate simp,

                       @Autowired final BlockbookService blockbook,
                       @Autowired final GethService geth,
                       @Autowired final BinanceService binance,
                       @Autowired final RippledService rippled,
                       @Autowired final TrongridService trongrid) {

        CoinService.walletService = walletService;
        CoinService.userService = userService;

        CoinService.coinMap = coinRep.findAll().stream().collect(Collectors.toMap(Coin::getCode, Function.identity()));
        CoinService.cache = cache;
        CoinService.simp = simp;

        CoinService.blockbook = blockbook;
        CoinService.geth = geth;
        CoinService.binance = binance;
        CoinService.rippled = rippled;
        CoinService.trongrid = trongrid;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void wsStompBalance() {
        wsMap.forEach((k, v) -> sendStompBalance(k, (Long) v.keySet().toArray()[0], v.get((Long) v.keySet().toArray()[0])));
    }

    public void sendStompBalance(String phone, Long userId, List<String> coins) {
        simp.convertAndSendToUser(phone, "/queue/balance", getCoinsBalance(userId, coins));
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
                    geth.addAddressToJournal(e.getAddress());
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
        dto.setProfitExchange(coin.getCoinEntity().getProfitExchange());
        dto.setWalletAddress(coin.getWalletAddress());
        dto.setContractAddress(coin.getContractAddress());

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
        return cache.getPriceById(id);
    }

    public enum CoinEnum {
        BTC {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbook.getBalance(getCoinType(), address);
            }

            @Override
            public Long getByteFee() {
                return blockbook.getByteFee(getCoinType());
            }

            @Override
            public BigDecimal getTxFee() {
                return blockbook.getTxFee(getCoinType());
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
                return blockbook.getTransactionNumber(getCoinType(), address, amount, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(getCoinType(), txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbook.getTransactionList(getCoinType(), address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbook.getUTXO(getCoinType(), xpub);
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(getCoinType())).getUtxos();
                Long byteFee = blockbook.getByteFee(getCoinType());

                return blockbook.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return blockbook.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BITCOIN;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbook.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return blockbook.getBtcExplorerUrl();
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
                return geth.getEthBalance(address);
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return geth.getTxFee(geth.getEthGasLimit(getWalletAddress()), geth.getGasPrice());
            }

            @Override
            public Long getGasPrice() {
                return geth.getGasPrice();
            }

            @Override
            public Long getGasLimit() {
                return geth.getEthGasLimit(getWalletAddress());
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
                return geth.getTransactionStatus(txId);
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return geth.getEthTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return geth.getEthTransactionList(address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return new NonceDTO(geth.getNonce(address));
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return geth.ethSign(fromAddress, toAddress, amount, geth.getEthGasLimit(toAddress), geth.getGasPrice());
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return geth.submitTransaction(dto);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.ETHEREUM;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return geth.getEthNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return geth.getExplorerUrl();
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
                return geth.getTokenBalance(address);
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return geth.getTxFee(geth.getTokenGasLimit(), geth.getGasPrice());
            }

            @Override
            public Long getGasPrice() {
                return geth.getGasPrice();
            }

            @Override
            public Long getGasLimit() {
                return geth.getTokenGasLimit();
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
                return geth.getTransactionStatus(txId);
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return geth.getTokenTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return geth.getTokenTransactionList(address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return new NonceDTO(geth.getNonce(address));
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return geth.tokenSign(fromAddress, toAddress, amount, geth.getTokenGasLimit(), geth.getGasPrice());
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return geth.submitTokenTransaction(dto);
            }

            @Override
            public CoinType getCoinType() {
                return CoinEnum.ETH.getCoinType();
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return geth.getTokenNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return geth.getExplorerUrl();
            }

            @Override
            public String getContractAddress() {
                return geth.getContractAddress();
            }
        },
        BCH {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return blockbook.getBalance(getCoinType(), address);
            }

            @Override
            public Long getByteFee() {
                return blockbook.getByteFee(getCoinType());
            }

            @Override
            public BigDecimal getTxFee() {
                return blockbook.getTxFee(getCoinType());
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
                return blockbook.getTransactionNumber(getCoinType(), address, amount, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(getCoinType(), txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbook.getTransactionList(getCoinType(), address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbook.getUTXO(getCoinType(), xpub);
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(getCoinType())).getUtxos();
                Long byteFee = blockbook.getByteFee(getCoinType());

                return blockbook.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return blockbook.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BITCOINCASH;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbook.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return blockbook.getBchExplorerUrl();
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
                return blockbook.getBalance(getCoinType(), address);
            }

            @Override
            public Long getByteFee() {
                return blockbook.getByteFee(getCoinType());
            }

            @Override
            public BigDecimal getTxFee() {
                return blockbook.getTxFee(getCoinType());
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
                return blockbook.getTransactionNumber(getCoinType(), address, amount, type);
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(getCoinType(), txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbook.getTransactionList(getCoinType(), address, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return blockbook.getUTXO(getCoinType(), xpub);
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(getCoinType())).getUtxos();
                Long byteFee = blockbook.getByteFee(getCoinType());

                return blockbook.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return blockbook.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.LITECOIN;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbook.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return blockbook.getLtcExplorerUrl();
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        BNB {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return binance.getBalance(address);
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
                return binance.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return binance.getTransactionList(address, startIndex, limit, txDTO);
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return binance.sign(fromAddress, toAddress, amount);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return binance.submitTransaction(dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BINANCE;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return binance.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return binance.getExplorerUrl();
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
                return rippled.getBalance(address);
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
                return rippled.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return rippled.getTransactionList(address, startIndex, limit, txDTO);
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                BigDecimal balance = getBalance(fromAddress);
                BigDecimal fee = getCoinEntity().getFee();
                BigDecimal maxWithdrawAmount = balance.subtract(new BigDecimal(20).subtract(fee));

                if (maxWithdrawAmount.compareTo(amount) < 0) {
                    amount = maxWithdrawAmount;
                }

                return rippled.sign(fromAddress, toAddress, amount, fee);
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return rippled.submitTransaction(dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.XRP;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return rippled.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return rippled.getExplorerUrl();
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
                return trongrid.getBalance(address);
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
                return trongrid.getTransaction(txId, address);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return trongrid.getTransactionList(address, startIndex, limit, txDTO);
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return trongrid.sign(fromAddress, toAddress, amount, getCoinEntity().getFee());
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return trongrid.submitTransaction(dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.TRON;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return trongrid.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return trongrid.getExplorerUrl();
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
                return null;
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return null;
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
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return null;
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return null;
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return null;
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
                return null;
            }

            @Override
            public String getWalletAddress() {
                return null;
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return null;
            }

            @Override
            public CoinType getCoinType() {
                return null;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return null;
            }

            @Override
            public Coin getCoinEntity() {
                return null;
            }

            @Override
            public String getExplorerUrl() {
                return null;
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
                return null;
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return null;
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
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return null;
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return null;
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return null;
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
                return null;
            }

            @Override
            public String getWalletAddress() {
                return null;
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return null;
            }

            @Override
            public CoinType getCoinType() {
                return null;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return null;
            }

            @Override
            public Coin getCoinEntity() {
                return null;
            }

            @Override
            public String getExplorerUrl() {
                return null;
            }

            @Override
            public String getContractAddress() {
                return null;
            }
        },
        USDT {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return null;
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return null;
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
                return "tether";
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type) {
                return null;
            }

            @Override
            public TransactionStatus getTransactionStatus(String txId) {
                return null;
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return null;
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return null;
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
                return null;
            }

            @Override
            public String getWalletAddress() {
                return null;
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return null;
            }

            @Override
            public String submitTransaction(SubmitTransactionDTO dto) {
                return null;
            }

            @Override
            public CoinType getCoinType() {
                return null;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return null;
            }

            @Override
            public Coin getCoinEntity() {
                return null;
            }

            @Override
            public String getExplorerUrl() {
                return null;
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