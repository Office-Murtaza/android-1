package com.batm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.Error;
import com.batm.model.Response;
import com.batm.util.*;
import com.binance.dex.api.client.domain.Account;
import com.binance.dex.api.client.domain.TransactionPage;
import com.binance.dex.api.client.domain.TransactionType;
import com.binance.dex.api.client.domain.request.TransactionsRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.batm.repository.CoinRepository;
import com.batm.repository.UserCoinRepository;
import com.batm.repository.UserRepository;
import com.batm.rest.vm.CoinBalanceVM;
import com.batm.rest.vm.CoinVM;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.dex.api.client.BinanceDexApiRestClient;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
@Slf4j
public class CoinService {

    @Autowired
    private UserCoinRepository userCoinRepository;

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private UserRepository userRepository;

    private static BinanceApiRestClient binance;
    private static BinanceDexApiRestClient binanceDex;
    private static RestTemplate rest;
    private static MessageService messageService;
    private static WalletService walletService;

    private static String btcUrl;
    private static String ethUrl;
    private static String bchUrl;
    private static String ltcUrl;
    private static String trxUrl;
    private static String xrpUrl;
    private static String bnbUrl;

    public CoinService(@Autowired final BinanceApiRestClient binance,
                       @Autowired final BinanceDexApiRestClient binanceDex,
                       @Autowired final RestTemplate rest,
                       @Autowired final MessageService messageService,
                       @Autowired final WalletService walletService,
                       @Value("${btc.url}") final String btcUrl,
                       @Value("${eth.url}") final String ethUrl,
                       @Value("${bch.url}") final String bchUrl,
                       @Value("${ltc.url}") final String ltcUrl,
                       @Value("${trx.url}") final String trxUrl,
                       @Value("${xrp.url}") final String xrpUrl,
                       @Value("${bnb.url}") final String bnbUrl) {

        CoinService.binance = binance;
        CoinService.binanceDex = binanceDex;
        CoinService.rest = rest;
        CoinService.messageService = messageService;
        CoinService.walletService = walletService;

        CoinService.btcUrl = btcUrl;
        CoinService.ethUrl = ethUrl;
        CoinService.bchUrl = bchUrl;
        CoinService.ltcUrl = ltcUrl;
        CoinService.trxUrl = trxUrl;
        CoinService.xrpUrl = xrpUrl;
        CoinService.bnbUrl = bnbUrl;
    }

    public enum CoinEnum {
        BTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return getBlockbookBalance(btcUrl, address, Constant.BTC_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return getBlockbookTransactionNumber(btcUrl, address, amount, Constant.BTC_DIVIDER);
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBlockbookTransactions(btcUrl, address, Constant.BTC_DIVIDER, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return getBlockbookUTXO(btcUrl, xpub);
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
                String txId = submitBlockbookTransaction(btcUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    messageService.sendGiftMessage(this, transaction);
                }

                return txId;
            }
        }, ETH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("ETHUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return getBlockbookBalance(ethUrl, address, Constant.ETH_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBlockbookTransactions(ethUrl, address, Constant.ETH_DIVIDER, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return null;
            }

            @Override
            public NonceDTO getNonce(String address) {
                return getBlockbookNonce(ethUrl, address);
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
                String txId = submitBlockbookTransaction(ethUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    messageService.sendGiftMessage(this, transaction);
                }

                return txId;
            }
        }, BCH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BCHABCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return getBlockbookBalance(bchUrl, address, Constant.BCH_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBlockbookTransactions(bchUrl, address, Constant.BCH_DIVIDER, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return getBlockbookUTXO(bchUrl, xpub);
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
                String txId = submitBlockbookTransaction(bchUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    messageService.sendGiftMessage(this, transaction);
                }

                return txId;
            }
        }, LTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("LTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return getBlockbookBalance(ltcUrl, address, Constant.LTC_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return getBlockbookTransactionNumber(ltcUrl, address, amount, Constant.LTC_DIVIDER);
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBlockbookTransactions(ltcUrl, address, Constant.LTC_DIVIDER, startIndex, limit);
            }

            @Override
            public UtxoDTO getUTXO(String xpub) {
                return getBlockbookUTXO(ltcUrl, xpub);
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
                String txId = submitBlockbookTransaction(ltcUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    messageService.sendGiftMessage(this, transaction);
                }

                return txId;
            }
        }, BNB {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BNBUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return getBinanceDEXBalance(address);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBinanceTransactions(address, startIndex, limit);
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
                return getBinanceCurrentAccount(address);
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
                String txId = submitBinanceTransaction(bnbUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    messageService.sendGiftMessage(this, transaction);
                }

                return txId;
            }
        }, XRP {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("XRPUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return getRippledBalance(address, Constant.XRP_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getRippledTransactions(address, Constant.XRP_DIVIDER, startIndex, limit);
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
                return walletService.getAddressXRP();
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = submitRippledTransaction(bnbUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    messageService.sendGiftMessage(this, transaction);
                }

                return txId;
            }
        }, TRX {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("TRXUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return getTrongridBalance(trxUrl, address, Constant.TRX_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getTrongridTransactions(address, Constant.TRX_DIVIDER, startIndex, limit);
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
                return getTrongridCurrentBlock();
            }

            @Override
            public String getWalletAddress() {
                return walletService.getAddressTRX();
            }

            @Override
            public String submitTransaction(Long userId, SubmitTransactionDTO transaction) {
                String txId = submitTrongridTransaction(bnbUrl, transaction);

                if (StringUtils.isNotEmpty(txId) && com.batm.model.TransactionType.SEND_GIFT.getValue() == transaction.getType()) {
                    messageService.sendGiftMessage(this, transaction);
                }

                return txId;
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);

        public abstract TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount);

        public abstract TransactionResponseDTO getTransactions(String address, Integer startIndex, Integer limit);

        public abstract UtxoDTO getUTXO(String xpub);

        public abstract NonceDTO getNonce(String address);

        public abstract CurrentAccountDTO getCurrentAccount(String address);

        public abstract CurrentBlockDTO getCurrentBlock();

        public abstract String getWalletAddress();

        public abstract String submitTransaction(Long userId, SubmitTransactionDTO transaction);
    }

    private static TransactionNumberDTO getBlockbookTransactionNumber(String url, String address, BigDecimal amount, long divider) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs", JSONObject.class);
            for (Object jsonTransactions : res.getJSONArray("transactions")) {
                for (Object operationObject : ((JSONObject) jsonTransactions).getJSONArray("vout")) {
                    if (operationObject instanceof JSONObject) {
                        JSONObject operationJson = ((JSONObject) operationObject);
                        String value = operationJson.getString("value");
                        BigDecimal bigValue = new BigDecimal(value).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
                        int n = operationJson.getInt("n");

                        if (bigValue.equals(amount.stripTrailingZeros())) {
                            String transactionId = ((JSONObject) jsonTransactions).getString("txid");
                            return new TransactionNumberDTO(transactionId, n);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransactionResponseDTO getTransactions(Long userId, CoinEnum coin, Integer startIndex) {
        String address = getCoinAddressByUserIdAndCoin(userId, coin.name());
        return coin.getTransactions(address, startIndex, Constant.TRANSACTION_LIMIT);
    }

    private static NonceDTO getBlockbookNonce(String url, String address) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
            JSONArray transactionsArray = res.optJSONArray("transactions");

            if (transactionsArray != null) {
                return new NonceDTO(transactionsArray.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new NonceDTO();
    }

    private static TransactionResponseDTO getBlockbookTransactions(String url, String address, Long divider, Integer startIndex, Integer limit) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
            JSONArray transactionsArray = res.optJSONArray("transactions");

            return TransactionUtil.composeBlockbook(res.optInt("txs"), transactionsArray, address, divider, startIndex, limit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionResponseDTO();
    }

    private static TransactionResponseDTO getTrongridTransactions(String address, Long divider, Integer startIndex, Integer limit) {
        try {
            JSONObject res = rest.getForObject(trxUrl + "/v1/accounts/" + address + "/transactions?limit=200", JSONObject.class);
            JSONArray transactionsArray = res.optJSONArray("data");

            return TransactionUtil.composeTrongrid(transactionsArray, address, divider, startIndex, limit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionResponseDTO();
    }

    private static CurrentBlockDTO getTrongridCurrentBlock() {
        try {
            String resStr = rest.getForObject(trxUrl + "/wallet/getnowblock", String.class);
            JSONObject res = JSONObject.fromObject(resStr);

            return new CurrentBlockDTO(res.optJSONObject("block_header"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CurrentBlockDTO();
    }

    private static TransactionResponseDTO getRippledTransactions(String address, Long divider, Integer startIndex, Integer limit) {
        try {
            JSONObject param = new JSONObject();
            param.put("account", address);
            param.put("limit", 1000);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "account_tx");
            req.put("params", params);

            JSONObject res = rest.postForObject(xrpUrl, req, JSONObject.class);
            JSONObject jsonResult = res.optJSONObject("result");
            JSONArray transactionsArray = jsonResult.optJSONArray("transactions");

            return TransactionUtil.composeRippled(transactionsArray, address, divider, startIndex, limit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionResponseDTO();
    }

    private static TransactionResponseDTO getBinanceTransactions(String address, Integer startIndex, Integer limit) {
        TransactionResponseDTO result = new TransactionResponseDTO();

        try {
            TransactionsRequest request = new TransactionsRequest();
            request.setStartTime(new SimpleDateFormat("yyyy").parse("2010").getTime());
            request.setEndTime(new Date().getTime() + TimeUnit.DAYS.toMillis(1));
            request.setTxType(TransactionType.TRANSFER);
            request.setAddress(address);
            request.setTxAsset("BNB");
            request.setLimit(1000);

            TransactionPage page = binanceDex.getTransactions(request);

            return TransactionUtil.composeBinance(page, address, startIndex, limit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public CoinBalanceVM getCoinsBalance(Long userId, List<String> coins) {
        if (coins == null || coins.isEmpty()) {
            return new CoinBalanceVM(userId, new ArrayList<>(), new Price(BigDecimal.ZERO));
        }

        List<UserCoin> userCoins = userCoinRepository.findByUserUserId(userId);

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

        return new CoinBalanceVM(userId, balances, new Price(totalBalance));
    }

    private CompletableFuture<CoinBalanceDTO> callAsync(UserCoin userCoin) {
        return CompletableFuture.supplyAsync(() -> {
            CoinEnum coinEnum = CoinEnum.valueOf(userCoin.getCoin().getId());

            BigDecimal coinPrice = coinEnum.getPrice();
            BigDecimal coinBalance = coinEnum.getBalance(userCoin.getPublicKey());

            return new CoinBalanceDTO(userCoin.getCoin().getId(), userCoin.getPublicKey(), coinBalance, new Price(coinPrice), userCoin.getCoin().getOrderIndex());
        });
    }

    private Coin getCoin(List<Coin> coins, String coinCode) {
        Coin coin = null;
        int index = coins.indexOf(new Coin(coinCode));
        if (index >= 0) {
            coin = coins.get(index);
        }
        return coin;
    }

    public void save(CoinVM coinVM, Long userId) {
        User user = userRepository.findById(userId).get();
        List<Coin> coins = coinRepository.findAll();
        List<UserCoin> userCoins = userCoinRepository.findByUserUserId(userId);

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

        userCoinRepository.saveAll(newCoins);
    }

    public Response compareCoins(CoinVM coinVM, Long userId) {
        List<UserCoin> userCoins = this.userCoinRepository.findByUserUserId(userId);

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

    private static BigDecimal getBlockbookBalance(String url, String address, long divider) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=basic", JSONObject.class);

            return new BigDecimal(res.getString("balance")).divide(BigDecimal.valueOf(divider)).setScale(5, RoundingMode.DOWN);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    private static BigDecimal getTrongridBalance(String url, String address, long divider) {
        try {
            JSONObject res = rest.getForObject(url + "/v1/accounts/" + address, JSONObject.class);
            JSONArray data = res.getJSONArray("data");

            if (!data.isEmpty()) {
                return new BigDecimal(data.getJSONObject(0).getString("balance")).divide(BigDecimal.valueOf(divider)).setScale(5, RoundingMode.DOWN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    private static BigDecimal getBinanceDEXBalance(String address) {
        try {
            return binanceDex
                    .getAccount(address)
                    .getBalances()
                    .stream()
                    .filter(e -> "BNB".equals(e.getSymbol()))
                    .map(it -> new BigDecimal(it.getFree()).add(new BigDecimal(it.getLocked())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(5, RoundingMode.DOWN);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    private static CurrentAccountDTO getBinanceCurrentAccount(String address) {
        try {
            Account account = binanceDex.getAccount(address);

            return new CurrentAccountDTO(account.getAccountNumber(), account.getSequence());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CurrentAccountDTO();
    }

    private static BigDecimal getRippledBalance(String address, long divider) {
        try {
            JSONObject param = new JSONObject();
            param.put("account", address);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "account_info");
            req.put("params", params);

            JSONObject res = rest.postForObject(xrpUrl, req, JSONObject.class);
            String balance = res.getJSONObject("result").getJSONObject("account_data").getString("Balance");

            return new BigDecimal(balance).divide(BigDecimal.valueOf(divider)).setScale(2, RoundingMode.DOWN);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    private static String submitBlockbookTransaction(String url, SubmitTransactionDTO transaction) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/sendtx/" + transaction.getHex(), JSONObject.class);

            String txId = RandomStringUtils.randomAlphanumeric(50);

            return txId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String submitTrongridTransaction(String url, SubmitTransactionDTO transaction) {
        try {
            JSONObject res = rest.postForObject(url + "/wallet/broadcasttransaction", transaction.getTrx(), JSONObject.class);

            String txId = RandomStringUtils.randomAlphanumeric(50);

            return txId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String submitBinanceTransaction(String url, SubmitTransactionDTO transaction) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            JSONObject res = rest.postForObject(url + "api/v1/broadcast", transaction.getHex(), JSONObject.class);

            String txId = RandomStringUtils.randomAlphanumeric(50);

            return txId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String submitRippledTransaction(String url, SubmitTransactionDTO transaction) {
        try {
            JSONObject param = new JSONObject();
            param.put("tx_blob", transaction.getHex());

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "submit");
            req.put("params", params);

            JSONObject res = rest.postForObject(url, req, JSONObject.class);

            String txId = RandomStringUtils.randomAlphanumeric(50);

            return txId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static UtxoDTO getBlockbookUTXO(String url, String publicKey) {
        try {
            JSONArray res = rest.getForObject(url + "/api/v2/utxo/" + publicKey, JSONArray.class);

            return new UtxoDTO(res);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new UtxoDTO();
    }

    public String getCoinAddressByUserIdAndCoin(Long userId, String coin) {
        return userCoinRepository.findByUserUserIdAndCoinId(userId, coin).getPublicKey();
    }

    public GiftAddressDTO getUserGiftAddress(CoinService.CoinEnum coinId, String phone) {
        Optional<User> user = userRepository.findOneByPhoneIgnoreCase(phone);

        if (user.isPresent()) {
            String address = user.get().getUserCoins().stream()
                    .filter(k -> k.getCoinId().equalsIgnoreCase(coinId.name()))
                    .findFirst().get()
                    .getPublicKey();

            return new GiftAddressDTO(address, true);
        } else {
            return new GiftAddressDTO(coinId.getWalletAddress(), false);
        }
    }
}