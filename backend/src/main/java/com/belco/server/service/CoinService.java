package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.entity.Coin;
import com.belco.server.entity.TransactionRecord;
import com.belco.server.entity.User;
import com.belco.server.entity.UserCoin;
import com.belco.server.repository.CoinRep;
import com.belco.server.util.Util;
import com.fasterxml.jackson.annotation.JsonValue;
import net.sf.json.JSONObject;
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

    //TODO: replace with Redis
    public static Map<String, Map<Long, List<String>>> wsMap = new ConcurrentHashMap<>();

    private static Map<String, Coin> coinMap;
    private static WalletService walletService;
    private static UserService userService;
    private static CacheService cacheService;
    private static SocketService socketService;
    private static NodeService nodeService;
    private static BlockbookService blockbookService;
    private static GethService gethService;
    private static BinanceService binanceService;
    private static RippledService rippledService;
    private static TrongridService trongridService;
    private static SettingsService settingsService;

    public CoinService(WalletService walletService, UserService userService, CoinRep coinRep, CacheService cacheService, SocketService socketService, NodeService nodeService, BlockbookService blockbookService, GethService gethService, BinanceService binanceService, RippledService rippledService, TrongridService trongridService, SettingsService settingsService) {
        CoinService.walletService = walletService;
        CoinService.userService = userService;

        CoinService.coinMap = coinRep.findAll().stream().collect(Collectors.toMap(Coin::getCode, Function.identity()));
        CoinService.cacheService = cacheService;
        CoinService.socketService = socketService;
        CoinService.nodeService = nodeService;

        CoinService.blockbookService = blockbookService;
        CoinService.gethService = gethService;
        CoinService.binanceService = binanceService;
        CoinService.rippledService = rippledService;
        CoinService.trongridService = trongridService;
        CoinService.settingsService = settingsService;
    }

    private static BigDecimal getPriceById(String id) {
        return cacheService.getPriceById(id);
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void updateBalance() {
        wsMap.forEach((k, v) -> pushBalance(k, (Long) v.keySet().toArray()[0], v.get(v.keySet().toArray()[0])));
    }

    public void pushBalance(String phone, Long userId, List<String> coins) {
        BalanceDTO dto = getCoinsBalance(userId, coins);
        socketService.pushBalance(phone, dto);
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
                .reduce(BigDecimal.ZERO, BigDecimal::add), 3);

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
        dto.setScale(coin.getCoinEntity().getScale());
        dto.setPlatformSwapFee(settingsService.getPlatformSwapFee());
        dto.setPlatformTradeFee(settingsService.getPlatformTradeFee());
        dto.setWalletAddress(coin.getWalletAddress());
        dto.setContractAddress(coin.getContractAddress());

        if (coin == CoinEnum.ETH) {
            dto.setGasLimit(coin.getGasLimit(coin.getWalletAddress()));
        } else if (coin == CoinEnum.CATM) {
            dto.setGasLimit(coin.getGasLimit(GethService.ERC20.CATM.getContractAddress()));
            dto.setConvertedTxFee(walletService.convertToFee(coin));
        } else if (coin == CoinEnum.USDC) {
            dto.setGasLimit(coin.getGasLimit(GethService.ERC20.USDC.getContractAddress()));
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

            socketService.pushBalance(user.getPhone(), getCoinsBalance(userId, coins));

            return true;
        }

        return false;
    }

    private CompletableFuture<CoinBalanceDTO> callAsync(UserCoin userCoin) {
        return CompletableFuture.supplyAsync(() -> {
            CoinEnum coin = CoinEnum.valueOf(userCoin.getCoin().getCode());

            Integer scale = coin.getCoinEntity().getScale();
            BigDecimal coinPrice = coin.getPrice();
            BigDecimal coinBalance = coin.getBalance(userCoin.getAddress()).setScale(scale, BigDecimal.ROUND_DOWN).stripTrailingZeros();
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
            dto.setDetails(getCoinDetails(coin));

            return dto;
        });
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
            public Long getGasLimit(String toAddress) {
                return null;
            }

            @Override
            public String getName() {
                return "bitcoin";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return blockbookService.isTransactionSeenOnBlockchain(getCoinType(), txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return blockbookService.getTransactionDetails(getCoinType(), txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return blockbookService.getTransactionHistory(getCoinType(), address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = blockbookService.getUtxo(getCoinType(), walletService.getXpub(getCoinType()));
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BITCOIN;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
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
            public Long getGasLimit(String toAddress) {
                return null;
            }

            @Override
            public String getName() {
                return "bitcoin-cash";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return blockbookService.isTransactionSeenOnBlockchain(getCoinType(), txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return blockbookService.getTransactionDetails(getCoinType(), txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return blockbookService.getTransactionHistory(getCoinType(), address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = blockbookService.getUtxo(getCoinType(), walletService.getXpub(getCoinType()));
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BITCOINCASH;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
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
            public Long getGasLimit(String toAddress) {
                return null;
            }

            @Override
            public String getName() {
                return "litecoin";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return blockbookService.isTransactionSeenOnBlockchain(getCoinType(), txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return blockbookService.getTransactionDetails(getCoinType(), txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return blockbookService.getTransactionHistory(getCoinType(), address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = blockbookService.getUtxo(getCoinType(), walletService.getXpub(getCoinType()));
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.LITECOIN;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
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
            public Long getGasLimit(String toAddress) {
                return null;
            }

            @Override
            public String getName() {
                return "dash";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return blockbookService.isTransactionSeenOnBlockchain(getCoinType(), txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return blockbookService.getTransactionDetails(getCoinType(), txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return blockbookService.getTransactionHistory(getCoinType(), address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = blockbookService.getUtxo(getCoinType(), walletService.getXpub(getCoinType()));
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.DASH;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
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
            public Long getGasLimit(String toAddress) {
                return null;
            }

            @Override
            public String getName() {
                return "dogecoin";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return blockbookService.isTransactionSeenOnBlockchain(getCoinType(), txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return blockbookService.getTransactionDetails(getCoinType(), txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return blockbookService.getTransactionHistory(getCoinType(), address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = blockbookService.getUtxo(getCoinType(), walletService.getXpub(getCoinType()));
                Long byteFee = blockbookService.getByteFee(getCoinType());

                return blockbookService.signBTCForks(getCoinType(), fromAddress, toAddress, amount, byteFee, utxos);
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return blockbookService.submitTransaction(getCoinType(), dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.DOGECOIN;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return blockbookService.getNodeTransactions(getCoinType(), address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
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
                return gethService.getTxFee(getGasLimit(getWalletAddress()), getGasPrice());
            }

            @Override
            public Long getGasPrice() {
                return GethService.getAvgGasPrice();
            }

            @Override
            public Long getGasLimit(String toAddress) {
                return gethService.getGasLimit(toAddress);
            }

            @Override
            public String getName() {
                return "ethereum";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return gethService.isTransactionSeenOnBlockchain(txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return gethService.getTransactionDetails(txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return gethService.getTransactionHistory(address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(CoinType.ETHEREUM).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return gethService.sign(fromAddress, toAddress, amount, getGasLimit(toAddress), getGasPrice());
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return gethService.submitTransaction(dto);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.ETHEREUM;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return gethService.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
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
                return gethService.getTxFee(getGasLimit(GethService.ERC20.CATM.getContractAddress()), getGasPrice());
            }

            @Override
            public Long getGasPrice() {
                return GethService.getFastGasPrice();
            }

            @Override
            public Long getGasLimit(String toAddress) {
                return gethService.getGasLimit(toAddress);
            }

            @Override
            public String getName() {
                return "catm";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return gethService.isTransactionSeenOnBlockchain(txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return gethService.getTransactionDetails(GethService.ERC20.CATM, txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return gethService.getTransactionHistory(GethService.ERC20.CATM, address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(CoinType.ETHEREUM).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return gethService.sign(GethService.ERC20.CATM, fromAddress, toAddress, amount, getGasLimit(GethService.ERC20.CATM.getContractAddress()), getGasPrice());
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return GethService.submitTransaction(GethService.ERC20.CATM, dto);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.ETHEREUM;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return gethService.getNodeTransactions(GethService.ERC20.CATM, address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
            }

            @Override
            public String getContractAddress() {
                return GethService.ERC20.CATM.getContractAddress();
            }
        },
        USDC {
            @Override
            public BigDecimal getPrice() {
                return getPriceById(getName());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return GethService.ERC20.USDC.getBalance(address);
            }

            @Override
            public Long getByteFee() {
                return null;
            }

            @Override
            public BigDecimal getTxFee() {
                return gethService.getTxFee(getGasLimit(GethService.ERC20.USDC.getContractAddress()), getGasPrice());
            }

            @Override
            public Long getGasPrice() {
                return GethService.getFastGasPrice();
            }

            @Override
            public Long getGasLimit(String toAddress) {
                return gethService.getGasLimit(toAddress);
            }

            @Override
            public String getName() {
                return "tether";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return gethService.isTransactionSeenOnBlockchain(txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return gethService.getTransactionDetails(GethService.ERC20.USDC, txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return gethService.getTransactionHistory(GethService.ERC20.USDC, address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(CoinType.ETHEREUM).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return gethService.sign(GethService.ERC20.USDC, fromAddress, toAddress, amount, getGasLimit(GethService.ERC20.USDC.getContractAddress()), getGasPrice());
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return GethService.submitTransaction(GethService.ERC20.USDC, dto);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.ETHEREUM;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return gethService.getNodeTransactions(GethService.ERC20.USDC, address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
            }

            @Override
            public String getContractAddress() {
                return GethService.ERC20.USDC.getContractAddress();
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
                return binanceService.getTxFee();
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit(String toAddress) {
                return null;
            }

            @Override
            public String getName() {
                return "binancecoin";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return binanceService.isTransactionSeenOnBlockchain(txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return binanceService.getTransactionDetails(txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return binanceService.getTransactionHistory(address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return binanceService.getCurrentAccount(address);
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
            public String submitTransaction(TransactionDTO dto) {
                return binanceService.submitTransaction(dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BINANCE;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return binanceService.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
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
                return rippledService.getTxFee();
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit(String toAddress) {
                return null;
            }

            @Override
            public String getName() {
                return "ripple";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return rippledService.isTransactionSeenOnBlockchain(txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return rippledService.getTransactionDetails(txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return rippledService.getTransactionDetails(address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return rippledService.getCurrentAccount(address);
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                BigDecimal balance = getBalance(fromAddress);
                BigDecimal fee = getTxFee();
                BigDecimal maxWithdrawAmount = balance.subtract(new BigDecimal(20).subtract(fee));

                if (maxWithdrawAmount.compareTo(amount) < 0) {
                    amount = maxWithdrawAmount;
                }

                return rippledService.sign(fromAddress, toAddress, amount, fee);
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return rippledService.submitTransaction(dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.XRP;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return rippledService.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
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
                return BigDecimal.ZERO;
            }

            @Override
            public Long getGasPrice() {
                return null;
            }

            @Override
            public Long getGasLimit(String toAddress) {
                return null;
            }

            @Override
            public String getName() {
                return "tron";
            }

            @Override
            public boolean isTransactionSeenOnBlockchain(String txId) {
                return trongridService.isTransactionSeenOnBlockchain(txId);
            }

            @Override
            public TransactionDetailsDTO getTransactionDetails(String txId, String address) {
                return trongridService.getTransactionDetails(txId, address, getExplorerUrl());
            }

            @Override
            public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details) {
                return trongridService.getTransactionHistory(address, startIndex, limit, transactionRecords, details);
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(String address) {
                return null;
            }

            @Override
            public String getWalletAddress() {
                return walletService.getCoinsMap().get(getCoinType()).getAddress();
            }

            @Override
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return trongridService.sign(fromAddress, toAddress, amount);
            }

            @Override
            public String submitTransaction(TransactionDTO dto) {
                return trongridService.submitTransaction(dto.getHex());
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.TRON;
            }

            @Override
            public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
                return trongridService.getNodeTransactions(address);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public String getExplorerUrl() {
                return nodeService.getExplorerUrl(getCoinType());
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

        public abstract Long getGasLimit(String toAddress);

        public abstract String getName();

        public abstract boolean isTransactionSeenOnBlockchain(String txId);

        public abstract TransactionDetailsDTO getTransactionDetails(String txId, String address);

        public abstract TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionDetailsDTO> details);

        public abstract CurrentAccountDTO getCurrentAccount(String address);

        public abstract String getWalletAddress();

        public abstract String sign(String fromAddress, String toAddress, BigDecimal amount);

        public abstract String submitTransaction(TransactionDTO dto);

        public abstract CoinType getCoinType();

        public abstract Map<String, TransactionDetailsDTO> getNodeTransactions(String address);

        public abstract Coin getCoinEntity();

        public abstract String getExplorerUrl();

        public abstract String getContractAddress();

        @JsonValue
        public String getValue() {
            return name();
        }
    }
}