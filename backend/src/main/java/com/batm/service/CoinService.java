package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.repository.CoinRep;
import com.batm.util.Constant;
import com.batm.util.Util;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.core.jni.CoinType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CoinService {

    private static List<Coin> coinList;
    private static Map<String, Coin> coinMap;

    private static WalletService walletService;
    private static UserService userService;

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

    public CoinService(@Autowired final WalletService walletService,
                       @Autowired final UserService userService,

                       @Autowired final CoinRep coinRep,

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

        CoinService.walletService = walletService;
        CoinService.userService = userService;

        CoinService.coinList = coinRep.findAll();
        CoinService.coinMap = CoinService.coinList.stream().collect(Collectors.toMap(Coin::getCode, Function.identity()));

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

    private static BigDecimal getBinancePriceBySymbol(String symbol) {
        return binance.getBinancePriceBySymbol(symbol);
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

    private CompletableFuture<CoinBalanceDTO> callAsync(UserCoin userCoin) {
        return CompletableFuture.supplyAsync(() -> {
            CoinEnum coinEnum = CoinEnum.valueOf(userCoin.getCoin().getCode());

            Integer scale = coinEnum.getCoinEntity().getScale();
            BigDecimal coinPrice = coinEnum.getPrice();
            BigDecimal coinBalance = coinEnum.getBalance(userCoin.getAddress()).setScale(scale, BigDecimal.ROUND_DOWN).stripTrailingZeros();

            return new CoinBalanceDTO(userCoin.getCoin().getId(), userCoin.getCoin().getCode(), userCoin.getAddress(), coinBalance, new AmountDTO(coinPrice));
        });
    }

    public Coin getCoin(String coinCode) {
        return coinList.stream().filter(e -> e.getCode().equalsIgnoreCase(coinCode)).findFirst().get();
    }

    public void save(CoinDTO coinVM, Long userId) {
        User user = userService.findById(userId);
        List<UserCoin> userCoins = userService.getUserCoins(userId);

        List<UserCoin> newCoins = new ArrayList<>();
        coinVM.getCoins().stream().forEach(coinDTO -> {
            Coin coin = getCoin(coinDTO.getCode());

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

        for (UserCoinDTO reqCoin : coinDTO.getCoins()) {
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

    public FeeDTO getCoinsFee() {
        List<CoinFeeDTO> feeList = new ArrayList<>();

        coinList.forEach(e -> {
            if (CoinEnum.ETH.name().equalsIgnoreCase(e.getCode())) {
                feeList.add(new CoinFeeDTO(e.getCode(), null, Constant.GAS_PRICE, Constant.GAS_LIMIT));
            } else {
                feeList.add(new CoinFeeDTO(e.getCode(), e.getFee().stripTrailingZeros(), null, null));
            }
        });

        return new FeeDTO(feeList);
    }

    public enum CoinEnum {
        BTC {
            @Override
            public BigDecimal getPrice() {
                return getBinancePriceBySymbol("BTCUSDT");
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
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(btcNodeUrl, btcExplorerUrl, txId, address, Constant.BTC_DIVIDER);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbook.getTransactionList(btcNodeUrl, address, Constant.BTC_DIVIDER, startIndex, limit, txDTO);
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(CoinType.BITCOIN)).getUtxos();

                return blockbook.signBTCForks(getCoinType(), fromAddress, toAddress, amount, getCoinEntity().getFee(), Constant.BTC_DIVIDER, utxos);
            }

            @Override
            public String submitTransaction(String hex) {
                return blockbook.submitTransaction(btcNodeUrl, hex);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BITCOIN;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbook.getNodeTransactions(btcNodeUrl, address, Constant.BTC_DIVIDER);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public CoinSettingsDTO getCoinSettings() {
                CoinSettingsDTO dto = new CoinSettingsDTO();
                dto.setProfitC2C(getCoinEntity().getProfitC2C().stripTrailingZeros());
                dto.setByteFee(getCoinEntity().getFee());
                dto.setTxFee(getCoinEntity().getFee().multiply(BigDecimal.valueOf(1000)).stripTrailingZeros());
                dto.setServerWalletAddress(getWalletAddress());

                return dto;
            }

            @Override
            public String getExplorerUrl() {
                return btcExplorerUrl;
            }
        },
        ETH {
            @Override
            public BigDecimal getPrice() {
                return getBinancePriceBySymbol("ETHUSDT");
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
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(ethNodeUrl, ethExplorerUrl, txId, address, Constant.ETH_DIVIDER);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbook.getTransactionList(ethNodeUrl, address, Constant.ETH_DIVIDER, startIndex, limit, txDTO);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(Long userId) {
                User user = userService.findById(userId);
                String address = user.getCoinAddress(name());

                NonceDTO nonceDTO = blockbook.getNonce(ethNodeUrl, address);
                nonceDTO.setNonce(nonceDTO.getNonce());

                return nonceDTO;
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return blockbook.signETH(ethNodeUrl, toAddress, amount, null);
            }

            @Override
            public String submitTransaction(String hex) {
                return blockbook.submitTransaction(ethNodeUrl, hex);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.ETHEREUM;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbook.getNodeTransactions(ethNodeUrl, address, Constant.ETH_DIVIDER);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public CoinSettingsDTO getCoinSettings() {
                CoinSettingsDTO dto = new CoinSettingsDTO();
                dto.setProfitC2C(getCoinEntity().getProfitC2C().stripTrailingZeros());
                dto.setGasPrice(Constant.GAS_PRICE);
                dto.setGasLimit(Constant.GAS_LIMIT);
                dto.setTxFee(BigDecimal.valueOf(Constant.GAS_PRICE).multiply(BigDecimal.valueOf(Constant.GAS_LIMIT)).divide(Constant.ETH_DIVIDER).stripTrailingZeros());
                dto.setServerWalletAddress(getWalletAddress());

                return dto;
            }

            @Override
            public String getExplorerUrl() {
                return ethExplorerUrl;
            }
        },
        BCH {
            @Override
            public BigDecimal getPrice() {
                return getBinancePriceBySymbol("BCHUSDT");
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
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(bchNodeUrl, bchExplorerUrl, txId, address, Constant.BCH_DIVIDER);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbook.getTransactionList(bchNodeUrl, address, Constant.BCH_DIVIDER, startIndex, limit, txDTO);
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(CoinType.BITCOINCASH)).getUtxos();

                return blockbook.signBTCForks(getCoinType(), fromAddress, toAddress, amount, getCoinEntity().getFee(), Constant.BCH_DIVIDER, utxos);
            }

            @Override
            public String submitTransaction(String hex) {
                return blockbook.submitTransaction(bchNodeUrl, hex);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.BITCOINCASH;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbook.getNodeTransactions(bchNodeUrl, address, Constant.BCH_DIVIDER);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public CoinSettingsDTO getCoinSettings() {
                CoinSettingsDTO dto = new CoinSettingsDTO();
                dto.setProfitC2C(getCoinEntity().getProfitC2C().stripTrailingZeros());
                dto.setByteFee(getCoinEntity().getFee());
                dto.setTxFee(getCoinEntity().getFee().multiply(BigDecimal.valueOf(1000)).stripTrailingZeros());
                dto.setServerWalletAddress(getWalletAddress());

                return dto;
            }

            @Override
            public String getExplorerUrl() {
                return bchExplorerUrl;
            }
        },
        LTC {
            @Override
            public BigDecimal getPrice() {
                return getBinancePriceBySymbol("LTCUSDT");
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
                return getTransaction(txId, StringUtils.EMPTY).getStatus();
            }

            @Override
            public TransactionDetailsDTO getTransaction(String txId, String address) {
                return blockbook.getTransaction(ltcNodeUrl, ltcExplorerUrl, txId, address, Constant.LTC_DIVIDER);
            }

            @Override
            public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
                return blockbook.getTransactionList(ltcNodeUrl, address, Constant.LTC_DIVIDER, startIndex, limit, txDTO);
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                List<JSONObject> utxos = getUTXO(walletService.getXPUB(CoinType.LITECOIN)).getUtxos();

                return blockbook.signBTCForks(getCoinType(), fromAddress, toAddress, amount, getCoinEntity().getFee(), Constant.LTC_DIVIDER, utxos);
            }

            @Override
            public String submitTransaction(String hex) {
                return blockbook.submitTransaction(ltcNodeUrl, hex);
            }

            @Override
            public CoinType getCoinType() {
                return CoinType.LITECOIN;
            }

            @Override
            public NodeTransactionsDTO getNodeTransactions(String address) {
                return blockbook.getNodeTransactions(ltcNodeUrl, address, Constant.LTC_DIVIDER);
            }

            @Override
            public Coin getCoinEntity() {
                return coinMap.get(name());
            }

            @Override
            public CoinSettingsDTO getCoinSettings() {
                CoinSettingsDTO dto = new CoinSettingsDTO();
                dto.setProfitC2C(getCoinEntity().getProfitC2C().stripTrailingZeros());
                dto.setByteFee(getCoinEntity().getFee());
                dto.setTxFee(getCoinEntity().getFee().multiply(BigDecimal.valueOf(1000)).stripTrailingZeros());
                dto.setServerWalletAddress(getWalletAddress());

                return dto;
            }

            @Override
            public String getExplorerUrl() {
                return ltcExplorerUrl;
            }
        },
        BNB {
            @Override
            public BigDecimal getPrice() {
                return getBinancePriceBySymbol("BNBUSDT");
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
            public NonceDTO getNonce(Long userId) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(Long userId) {
                User user = userService.findById(userId);
                String address = user.getCoinAddress(name());

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
            public String submitTransaction(String hex) {
                return binance.submitTransaction(hex);
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
            public CoinSettingsDTO getCoinSettings() {
                CoinSettingsDTO dto = new CoinSettingsDTO();
                dto.setProfitC2C(getCoinEntity().getProfitC2C().stripTrailingZeros());
                dto.setTxFee(getCoinEntity().getFee().stripTrailingZeros());
                dto.setServerWalletAddress(getWalletAddress());

                return dto;
            }

            @Override
            public String getExplorerUrl() {
                return binance.getExplorerUrl();
            }
        },
        XRP {
            @Override
            public BigDecimal getPrice() {
                return getBinancePriceBySymbol("XRPUSDT");
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
            public NonceDTO getNonce(Long userId) {
                return null;
            }

            @Override
            public CurrentAccountDTO getCurrentAccount(Long userId) {
                User user = userService.findById(userId);
                String address = user.getCoinAddress(name());

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
            public String submitTransaction(String hex) {
                return rippled.submitTransaction(hex);
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
            public CoinSettingsDTO getCoinSettings() {
                CoinSettingsDTO dto = new CoinSettingsDTO();
                dto.setProfitC2C(getCoinEntity().getProfitC2C().stripTrailingZeros());
                dto.setTxFee(getCoinEntity().getFee().stripTrailingZeros());
                dto.setServerWalletAddress(getWalletAddress());

                return dto;
            }

            @Override
            public String getExplorerUrl() {
                return rippled.getExplorerUrl();
            }
        },
        TRX {
            @Override
            public BigDecimal getPrice() {
                return getBinancePriceBySymbol("TRXUSDT");
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
            public String sign(String fromAddress, String toAddress, BigDecimal amount) {
                return trongrid.sign(fromAddress, toAddress, amount, getCoinEntity().getFee());
            }

            @Override
            public String submitTransaction(String hex) {
                return trongrid.submitTransaction(hex);
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
            public CoinSettingsDTO getCoinSettings() {
                CoinSettingsDTO dto = new CoinSettingsDTO();
                dto.setProfitC2C(getCoinEntity().getProfitC2C().stripTrailingZeros());
                dto.setTxFee(getCoinEntity().getFee().stripTrailingZeros());
                dto.setServerWalletAddress(getWalletAddress());

                return dto;
            }

            @Override
            public String getExplorerUrl() {
                return trongrid.getExplorerUrl();
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);

        public abstract String getName();

        public abstract TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount, TransactionType type);

        public abstract TransactionStatus getTransactionStatus(String txId);

        public abstract TransactionDetailsDTO getTransaction(String txId, String address);

        public abstract TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO);

        public abstract UtxoDTO getUTXO(String xpub);

        public abstract NonceDTO getNonce(Long userId);

        public abstract CurrentAccountDTO getCurrentAccount(Long userId);

        public abstract CurrentBlockDTO getCurrentBlock();

        public abstract String getWalletAddress();

        public abstract String sign(String fromAddress, String toAddress, BigDecimal amount);

        public abstract String submitTransaction(String hex);

        public abstract CoinType getCoinType();

        public abstract NodeTransactionsDTO getNodeTransactions(String address);

        public abstract Coin getCoinEntity();

        public abstract CoinSettingsDTO getCoinSettings();

        public abstract String getExplorerUrl();
    }
}