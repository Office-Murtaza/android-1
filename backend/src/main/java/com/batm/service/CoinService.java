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
import com.batm.util.*;
import com.binance.dex.api.client.domain.TransactionPage;
import com.binance.dex.api.client.domain.TransactionType;
import com.binance.dex.api.client.domain.request.TransactionsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
    private Environment environment;

    @Autowired
    private UserCoinRepository userCoinRepository;

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private UserRepository userRepository;

    private static BinanceApiRestClient binance;
    private static BinanceDexApiRestClient binanceDex;
    private static RestTemplate rest;

    private static String btcUrl;
    private static String ethUrl;
    private static String bchUrl;
    private static String ltcUrl;
    private static String trxUrl;
    private static String xrpUrl;

    public CoinService(@Autowired final BinanceApiRestClient binance,
                       @Autowired final BinanceDexApiRestClient binanceDex,
                       @Autowired final RestTemplate rest,
                       @Value("${btc.url}") final String btcUrl,
                       @Value("${eth.url}") final String ethUrl,
                       @Value("${bch.url}") final String bchUrl,
                       @Value("${ltc.url}") final String ltcUrl,
                       @Value("${trx.url}") final String trxUrl,
                       @Value("${xrp.url}") final String xrpUrl) {

        CoinService.binance = binance;
        CoinService.binanceDex = binanceDex;
        CoinService.rest = rest;
        CoinService.btcUrl = btcUrl;
        CoinService.ethUrl = ethUrl;
        CoinService.bchUrl = bchUrl;
        CoinService.ltcUrl = ltcUrl;
        CoinService.trxUrl = trxUrl;
        CoinService.xrpUrl = xrpUrl;
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
            public BlockbookTxDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBlockbookTransactions2(btcUrl, address, Constant.BTC_DIVIDER, startIndex, limit);
            }

            @Override
            public SendTransactionDTO sendTx(String hex) {
                return sendBlockbookTransaction(btcUrl, hex);
            }

            @Override
            public List<JSONObject> getUTXO(String publicKey) {
                return getBlockbookUTXO(btcUrl, publicKey);
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
            public BlockbookTxDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBlockbookTransactions2(ethUrl, address, Constant.ETH_DIVIDER, startIndex, limit);
            }

            @Override
            public SendTransactionDTO sendTx(String hex) {
                return sendBlockbookTransaction(ethUrl, hex);
            }

            @Override
            public List<JSONObject> getUTXO(String publicKey) {
                return getBlockbookUTXO(ethUrl, publicKey);
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
            public BlockbookTxDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBlockbookTransactions2(bchUrl, address, Constant.BCH_DIVIDER, startIndex, limit);
            }

            @Override
            public SendTransactionDTO sendTx(String hex) {
                return sendBlockbookTransaction(bchUrl, hex);
            }

            @Override
            public List<JSONObject> getUTXO(String publicKey) {
                return getBlockbookUTXO(bchUrl, publicKey);
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
            public BlockbookTxDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBlockbookTransactions2(ltcUrl, address, Constant.LTC_DIVIDER, startIndex, limit);
            }

            @Override
            public SendTransactionDTO sendTx(String hex) {
                return sendBlockbookTransaction(ltcUrl, hex);
            }

            @Override
            public List<JSONObject> getUTXO(String publicKey) {
                return getBlockbookUTXO(ltcUrl, publicKey);
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
            public BlockbookTxDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getBinanceTransactions2(address, startIndex, limit);
            }

            @Override
            public SendTransactionDTO sendTx(String hex) {
                return null;
            }

            @Override
            public List<JSONObject> getUTXO(String publicKey) {
                return null;
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
            public BlockbookTxDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getRippledTransactions2(address, Constant.XRP_DIVIDER, startIndex, limit);
            }

            @Override
            public SendTransactionDTO sendTx(String hex) {
                return null;
            }

            @Override
            public List<JSONObject> getUTXO(String publicKey) {
                return null;
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
            public BlockbookTxDTO getTransactions(String address, Integer startIndex, Integer limit) {
                return getTrongridTransactions2(address, Constant.TRX_DIVIDER, startIndex, limit);
            }

            @Override
            public SendTransactionDTO sendTx(String hex) {
                return null;
            }

            @Override
            public List<JSONObject> getUTXO(String publicKey) {
                return null;
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);

        public abstract TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount);

        public abstract BlockbookTxDTO getTransactions(String address, Integer startIndex, Integer limit);

        public abstract SendTransactionDTO sendTx(String hex);

        public abstract List<JSONObject> getUTXO(String publicKey);
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

    public BlockbookTxDTO getTransactions(Long userId, CoinEnum coin, Integer startIndex) {
        String address = getCoinAddressByUserIdAndCoin(userId, coin.name());
        return coin.getTransactions(address, startIndex, Constant.TRANSACTION_LIMIT);
    }

    public static BlockbookTxDTO getBlockbookTransactions2(String url, String address, Long divider, Integer startIndex, Integer limit) {
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=1", JSONObject.class);
            JSONArray transactionsArray = res.optJSONArray("transactions");

            return TransactionUtil.composeBlockbook(res.optInt("txs"), transactionsArray, address, divider, startIndex, limit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BlockbookTxDTO();
    }

    public static BlockbookTxDTO getTrongridTransactions2(String address, Long divider, Integer startIndex, Integer limit) {
        try {
            JSONObject res = rest.getForObject(trxUrl + "/v1/accounts/" + address + "/transactions?limit=200", JSONObject.class);
            JSONArray transactionsArray = res.optJSONArray("data");

            return TransactionUtil.composeTrongrid(transactionsArray, address, divider, startIndex, limit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BlockbookTxDTO();
    }

    private static BlockbookTxDTO getRippledTransactions2(String address, Long divider, Integer startIndex, Integer limit) {
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

        return new BlockbookTxDTO();
    }

    private static BlockbookTxDTO getBinanceTransactions2(String address, Integer startIndex, Integer limit) {
        BlockbookTxDTO result = new BlockbookTxDTO();

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
                return Response.error(new com.batm.entity.Error(3, "Coin does not exist"));
            }

            String dbPublicKey = userCoins.get(index).getPublicKey();
            if (userCoin.getPublicKey() == null
                    || !userCoin.getPublicKey().equalsIgnoreCase(dbPublicKey)) {
                return Response.error(new com.batm.entity.Error(3, "Public keys not match"));
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

    private static SendTransactionDTO sendBlockbookTransaction(String url, String hex) {
        SendTransactionDTO result = new SendTransactionDTO();

        try {
            JSONObject res = rest.getForObject(url + "/api/v2/sendtx/" + hex, JSONObject.class);
            String txId = res.optString("result");

            if (txId != null) {
                result.setTxId(txId).setSuccess(true);
            } else {
                result.setErrorMessage(res.optJSONObject("error").optString("message"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static List<JSONObject> getBlockbookUTXO(String url, String publicKey) {
        try {
            JSONArray res = rest.getForObject(url + "/api/v2/utxo/" + publicKey, JSONArray.class);

            return Util.jsonArrayToList(res);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getCoinAddressByUserIdAndCoin(Long userId, String coin) {
        return userCoinRepository.findByUserUserIdAndCoinId(userId, coin).getPublicKey();
    }

    public String getCoinAddressByUserPhoneAndCoin(String phone, String coin) {
        return userRepository.findOneByPhoneIgnoreCase(phone).map(user -> {
            UserCoin userCoin = userCoinRepository.findByUserUserIdAndCoinId(user.getUserId(), coin);
            if (userCoin != null) {
                return userCoin.getPublicKey();
            }
            return null;
        }).orElse(null);
    }

    public String getDefaultPublicKeyByCoin(CoinEnum coin) {
        return environment.getProperty(String.format("%s.publicKey", coin.name().toLowerCase()));
    }
}