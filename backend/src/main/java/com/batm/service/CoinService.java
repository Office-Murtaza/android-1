package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.CashStatus;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.repository.CoinRep;
import com.batm.util.Constant;
import com.batm.util.Util;
import com.binance.api.client.BinanceApiRestClient;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.core.jni.CoinType;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CoinService {

    private static List<Coin> coinList;
    private static Map<String, Coin> coinMap;

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

        CoinService.binanceRest = binanceRest;
        CoinService.messageService = messageService;
        CoinService.walletService = walletService;
        CoinService.userService = userService;
        CoinService.transactionService = transactionService;

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
            public String sign(String toAddress, BigDecimal amount) {
                try {

                    List<JSONObject> utxos = blockbook.getUTXO(btcNodeUrl, walletService.getXPUB(CoinType.BITCOIN)).getUtxos();
                    String hex = blockbook.signBTC(toAddress, amount, coinMap.get(name()).getFee(), utxos);

                    System.out.println(name() + " hex:" + hex);
                    return blockbook.submitTransaction(btcNodeUrl, hex);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                try {
                    String txId = blockbook.submitTransaction(btcNodeUrl, transaction.getHex());
                    saveGift(userId, this, txId, transaction);

                    return txId;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        },
        ETH {
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
            public String sign(String toAddress, BigDecimal amount) {
                try {
                    NonceDTO nonceDTO = blockbook.getNonce(ethNodeUrl, walletService.getAddressETH());
                    String hex = blockbook.signETH(toAddress, amount, nonceDTO.getNonce());

                    return blockbook.submitTransaction(ethNodeUrl, hex);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                try {
                    String txId = blockbook.submitTransaction(ethNodeUrl, transaction.getHex());
                    saveGift(userId, this, txId, transaction);

                    return txId;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        },
        BCH {
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
            public String sign(String toAddress, BigDecimal amount) {
                try {
                    List<JSONObject> utxos = blockbook.getUTXO(bchNodeUrl, walletService.getXPUB(CoinType.BITCOINCASH)).getUtxos();
                    String hex = blockbook.signBCH(toAddress, amount, coinMap.get(name()).getFee(), utxos);

                    System.out.println(name() + " hex:" + hex);
                    return blockbook.submitTransaction(bchNodeUrl, hex);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                try {
                    String txId = blockbook.submitTransaction(bchNodeUrl, transaction.getHex());
                    saveGift(userId, this, txId, transaction);

                    return txId;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        },
        LTC {
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
            public String sign(String toAddress, BigDecimal amount) {
                try {
                    List<JSONObject> utxos = blockbook.getUTXO(ltcNodeUrl, walletService.getXPUB(CoinType.LITECOIN)).getUtxos();
                    String hex = blockbook.signLTC(toAddress, amount, coinMap.get(name()).getFee(), utxos);

                    System.out.println(name() + " hex:" + hex);
                    return blockbook.submitTransaction(ltcNodeUrl, hex);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                try {
                    String txId = blockbook.submitTransaction(ltcNodeUrl, transaction.getHex());
                    saveGift(userId, this, txId, transaction);

                    return txId;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
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
            public String sign(String toAddress, BigDecimal amount) {
                try {
                    CurrentAccountDTO currentDTO = binance.getCurrentAccount(walletService.getAddressBNB());
                    String hex = binance.sign(toAddress, amount, currentDTO.getAccountNumber(), currentDTO.getSequence(), currentDTO.getChainId());

                    return binance.submitTransaction(hex);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                try {
                    String txId = binance.submitTransaction(transaction.getHex());
                    saveGift(userId, this, txId, transaction);

                    return txId;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
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
            public String sign(String toAddress, BigDecimal amount) {
                try {
                    CurrentAccountDTO accountDTO = rippled.getCurrentAccount(walletService.getAddressXRP());
                    String hex = rippled.sign(toAddress, amount, coinMap.get(name()).getFee(), accountDTO.getSequence());

                    return rippled.submitTransaction(hex);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                try {
                    String txId = rippled.submitTransaction(transaction.getHex());
                    saveGift(userId, this, txId, transaction);

                    return txId;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
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
            public String sign(String toAddress, BigDecimal amount) {
                try {
                    CurrentBlockDTO currentBlockDTO = trongrid.getCurrentBlock();
                    JSONObject json = trongrid.sign(toAddress, amount, coinMap.get(name()).getFee(), currentBlockDTO.getBlockHeader().optJSONObject("raw_data"));

                    return trongrid.submitTransaction(json);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                try {
                    String txId = trongrid.submitTransaction(JSONObject.fromObject(transaction.getHex()));
                    saveGift(userId, this, txId, transaction);

                    return txId;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
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

        public abstract String sign(String toAddress, BigDecimal amount);

        public abstract String submitTransaction(Long userId, SubmitTransactionDTO transaction);
    }

    public TransactionDTO getTransaction(Long userId, CoinEnum coin, String txId) {
        User user = userService.findById(userId);

        TransactionDTO dto = new TransactionDTO();
        TransactionRecord txRecord;

        if (StringUtils.isNumeric(txId)) {  /** consider as txDbId */
            txRecord = user.getIdentity().getTxRecordByDbId(Long.valueOf(txId), coin.name());
        } else {                            /** consider as txId */
            String address = user.getCoinAddress(coin.name());
            dto = coin.getTransaction(txId, address);
            txRecord = user.getIdentity().getTxRecordByCryptoId(txId, coin.name());
        }

        TransactionRecordGift txGift = user.getIdentity().getTxGift(txId, coin.name());

        if (txGift != null) {
            dto.setPhone(txGift.getPhone());
            dto.setImageId(txGift.getImageId());
            dto.setMessage(txGift.getMessage());
            dto.setType(TransactionType.getGiftType(dto.getType()));
        } else if (txRecord != null) {

            // to return either txId or txDbId, not both
            if (StringUtils.isBlank(dto.getTxId())) {
                if (StringUtils.isNotBlank(txRecord.getDetail())) {
                    dto.setTxId(txRecord.getDetail());
                } else {
                    dto.setTxDbId(txRecord.getId().toString());
                }
            }

            dto.setType(txRecord.getTransactionType());
            dto.setStatus(txRecord.getTransactionStatus(dto.getType()));
            dto.setCryptoAmount(txRecord.getCryptoAmount().stripTrailingZeros());
            dto.setFiatAmount(txRecord.getCashAmount().setScale(0));
            dto.setToAddress(txRecord.getCryptoAddress());
            dto.setDate2(txRecord.getServerTime());

            if (dto.getType() == TransactionType.SELL) {
                dto.setCashStatus(CashStatus.getCashStatus(txRecord.getCanBeCashedOut(), txRecord.getWithdrawn()));
                dto.setSellInfo(coin.getName() + ":" + txRecord.getCryptoAddress() + "?amount=" + txRecord.getCryptoAmount() + "&label=" + txRecord.getRemoteTransactionId() + "&uuid=" + txRecord.getUuid());
            }
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
        coinVM.getCoins().stream().forEach(coinDTO -> {
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

    public static void saveGift(Long userId, CoinEnum coinId, String txId, SubmitTransactionDTO dto) {
        if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == dto.getType()) {
            User user = userService.findById(userId);

            Optional<User> receiver = userService.findByPhone(dto.getPhone());
            messageService.sendGiftMessage(coinId, dto, receiver.isPresent());

            transactionService.saveGift(user.getIdentity(), txId, user.getCoin(coinId.name()), dto, receiver.isPresent());
        }
    }
}