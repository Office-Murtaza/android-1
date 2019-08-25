package com.batm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.batm.repository.CoinRepository;
import com.batm.repository.UserCoinRepository;
import com.batm.repository.UserRepository;
import com.batm.rest.vm.CoinBalanceVM;
import com.batm.rest.vm.CoinVM;
import com.batm.util.Util;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.dex.api.client.BinanceDexApiRestClient;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class CoinService {

    @Autowired
    private UserCoinRepository userCoinRepository;

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static BinanceApiRestClient binance;
    private static BinanceDexApiRestClient binanceDex;
    private static RestTemplate rest;

    private static String btcUrl;
    private static String ethUrl;
    private static String bchUrl;
    private static String ltcUrl;
    private static String trxUrl;
    private static String xrpUrl;

    private static String chainalysisUrl;
    private static String chainalysisApiKey;
    private static Integer chainalysisRowsLimit;

    public CoinService(@Autowired final BinanceApiRestClient binance,
                       @Autowired final BinanceDexApiRestClient binanceDex,
                       @Autowired final RestTemplate rest,
                       @Value("${btc.url}") final String btcUrl,
                       @Value("${eth.url}") final String ethUrl,
                       @Value("${bch.url}") final String bchUrl,
                       @Value("${ltc.url}") final String ltcUrl,
                       @Value("${trx.url}") final String trxUrl,
                       @Value("${xrp.url}") final String xrpUrl,
                       @Value("${chainalysis.url}") final String chainalysisUrl,
                       @Value("${chainalysis.api-key}") final String chainalysisApiKey,
                       @Value("${chainalysis.rows-limit}") final Integer chainalysisRowsLimit) {

        this.binance = binance;
        this.binanceDex = binanceDex;
        this.rest = rest;
        this.btcUrl = btcUrl;
        this.ethUrl = ethUrl;
        this.bchUrl = bchUrl;
        this.ltcUrl = ltcUrl;
        this.trxUrl = trxUrl;
        this.xrpUrl = xrpUrl;
        this.chainalysisUrl = chainalysisUrl;
        this.chainalysisApiKey = chainalysisApiKey;
        this.chainalysisRowsLimit = chainalysisRowsLimit;
    }

    public enum CoinEnum {
        BTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "1F1tAaz5x1HUXrCNLbtMDqcw6o5GNn4xqX";

                return getBlockbookBalance(btcUrl, address, 100000000L);
            }

            @Override
            public String getTransactionId(String address, BigDecimal amount, Integer transactionType) {
                return getBlockbookTransactionId(btcUrl, address, amount, transactionType, 100000000L);
            }
        }, ETH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("ETHUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "0x5eD8Cee6b63b1c6AFce3AD7c92f4fD7E1B8fAd9F";

                return getBlockbookBalance(ethUrl, address, 1000000000000000000L);
            }

            @Override
            public String getTransactionId(String address, BigDecimal amount, Integer transactionType) {
                return null;
            }
        }, BCH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BCHABCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "bitcoincash:pp8skudq3x5hzw8ew7vzsw8tn4k8wxsqsv0lt0mf3g";

                return getBlockbookBalance(bchUrl, address, 100000000L);
            }

            @Override
            public String getTransactionId(String address, BigDecimal amount, Integer transactionType) {
                return null;
            }
        }, LTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("LTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "MH1RcrnDDttNBmz6XLRK4vJbUGp36nmThv";

                return getBlockbookBalance(ltcUrl, address, 100000000L);
            }

            @Override
            public String getTransactionId(String address, BigDecimal amount, Integer transactionType) {
                return getBlockbookTransactionId(ltcUrl, address, amount, transactionType, 100000000L);
            }
        }, BNB {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BNBUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "bnb1pv5vycd4xe3nu0msrewgf9tmfvam8yr3x6j97q";

                return getBinanceDEXBalance(address);
            }

            @Override
            public String getTransactionId(String address, BigDecimal amount, Integer transactionType) {
                return null;
            }
        }, XRP {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("XRPUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "r3kmLJN5D28dHuH8vZNUZpMC43pEHpaocV";

                return getRippledBalance(address, 1000000L);
            }

            @Override
            public String getTransactionId(String address, BigDecimal amount, Integer transactionType) {
                return null;
            }
        }, TRX {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("TRXUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "TFKCrg61s7Q9oWZAM9Fy4ua6ZvSAom8j7V";

                return getTrongridBalance(trxUrl, address, 1000000L);
            }

            @Override
            public String getTransactionId(String address, BigDecimal amount, Integer transactionType) {
                return null;
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);

        public abstract String getTransactionId(String address, BigDecimal amount, Integer transactionType);
    }

    @Scheduled(fixedDelay = 600_000)
    public void scheduleFixedDelayTask() {
        Set<CoinEnum> coins = new HashSet<>(Arrays.asList(CoinEnum.BTC, CoinEnum.LTC));
        List<Transaction> untrackedTransactionList = getUntrackedTransactions(coins, chainalysisRowsLimit);

        List<CompletableFuture<ChainalysisResponseDTO>> futures = untrackedTransactionList.stream()
                .map(CoinService::callAsyncChainalysisValidation)
                .collect(Collectors.toList());

        List<Transaction> analyzedTransactions = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .map(ChainalysisResponseDTO::getTransaction)
                .collect(Collectors.toList());

        saveTransactions(analyzedTransactions);
    }

    public List<Transaction> getUntrackedTransactions(Set<CoinEnum> coins, Integer limit) {
        Pageable page = PageRequest.of(0, limit);
        Set<String> currency = coins.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return transactionRepository.findUnTrackedClosedTransactions(currency, page);
    }

    @Transactional
    public List<Transaction> saveTransactions(List<Transaction> transactions) {
        return transactionRepository.saveAll(transactions);
    }

    private static CompletableFuture<ChainalysisResponseDTO> callAsyncChainalysisValidation(Transaction transaction) {
        return CompletableFuture.supplyAsync(() -> {
            if (!Pattern.matches("^[a-fA-F0-9]{64}$", String.valueOf(transaction.getDetail()))) {
                CoinEnum coinEnum = CoinEnum.valueOf(transaction.getCryptoCurrency());
                transaction.setDetail(coinEnum.getTransactionId(transaction.getCryptoAddress(), transaction.getCryptoAmount(), transaction.getType()));
            }
            return validateChainalysisTransfer(transaction);
        });
    }

    private static ChainalysisResponseDTO validateChainalysisTransfer(Transaction transaction) {
        ChainalysisResponseDTO result = new ChainalysisResponseDTO();

        if (transaction.getDetail() == null) {
            transaction.setTracked(true);
            result.setTransaction(transaction);
            return result;
        }

        String requestType = transaction.getType() == 0 ? "received" : "sent";
        String requestTransferReference = transaction.getType() == 0
                ? String.format("%s:%s", transaction.getDetail(), transaction.getCryptoAddress())
                : String.format("%s:%d", transaction.getDetail(), 0);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("asset", transaction.getCryptoCurrency());
        jsonObject.put("transferReference", requestTransferReference);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", chainalysisApiKey);

        HttpEntity<JSONArray> request = new HttpEntity<>(jsonArray, headers);

        String url = chainalysisUrl + "/api/kyt/v1/users/" + transaction.getIdentity().getPublicId() + "/transfers/" + requestType;

        try {
            ResponseEntity<JSONArray> responseEntity = rest.exchange(url, HttpMethod.POST, request, JSONArray.class);
            transaction.setTracked(true);
            result.setTransaction(transaction);

            if (responseEntity.getBody() != null) {
                JSONObject checkResult = responseEntity.getBody().getJSONObject(0);
                result.setTransferReference(checkResult.getString("transferReference"));
                result.setAsset(checkResult.getString("asset"));
                result.setRating(checkResult.getString("rating"));

                JSONObject cluster = checkResult.getJSONObject("cluster");
                if (cluster != null) {
                    result.setClusterName(cluster.getString("name"));
                    result.setClusterCategory(cluster.getString("category"));
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getBlockbookTransactionId(String url, String address, BigDecimal amount, Integer transactionType, long divider) {
        String type = transactionType == 0 ? "vout" : "vin";
        try {
            JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs", JSONObject.class);
            for (Object jsonTransactions : res.getJSONArray("transactions")) {
                for (Object operationObject : ((JSONObject) jsonTransactions).getJSONArray(type)) {
                    if (operationObject instanceof JSONObject) {
                        String value = ((JSONObject) operationObject).getString("value");
                        BigDecimal bigValue = new BigDecimal(value).divide(BigDecimal.valueOf(divider)).stripTrailingZeros();
                        if (bigValue.equals(amount.stripTrailingZeros())) {
                            return ((JSONObject) jsonTransactions).getString("txid");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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

            return new BigDecimal(res.getString("balance")).divide(BigDecimal.valueOf(divider)).setScale(2, RoundingMode.DOWN);
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
                return new BigDecimal(data.getJSONObject(0).getString("balance")).divide(BigDecimal.valueOf(divider)).setScale(2, RoundingMode.DOWN);
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
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
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
}
