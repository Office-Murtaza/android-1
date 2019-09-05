package com.batm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.batm.dto.*;
import com.batm.dto.mapper.TransactionMapper;
import com.batm.entity.*;
import com.batm.repository.TransactionRepository;
import com.batm.util.Constant;
import com.binance.dex.api.client.domain.TransactionPage;
import com.binance.dex.api.client.domain.TransactionType;
import com.binance.dex.api.client.domain.request.TransactionsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
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
    private Environment environment;

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

        CoinService.binance = binance;
        CoinService.binanceDex = binanceDex;
        CoinService.rest = rest;
        CoinService.btcUrl = btcUrl;
        CoinService.ethUrl = ethUrl;
        CoinService.bchUrl = bchUrl;
        CoinService.ltcUrl = ltcUrl;
        CoinService.trxUrl = trxUrl;
        CoinService.xrpUrl = xrpUrl;
        CoinService.chainalysisUrl = chainalysisUrl;
        CoinService.chainalysisApiKey = chainalysisApiKey;
        CoinService.chainalysisRowsLimit = chainalysisRowsLimit;
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

                return getBlockbookBalance(btcUrl, address, Constant.BTC_BLOCKBOOK_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return getBlockbookTransactionNumber(btcUrl, address, amount, Constant.BTC_BLOCKBOOK_DIVIDER);
            }

            @Override
            public TransactionResponseDTO<TransactionDTO> getTransactions(String address, Integer startIndex, Integer limit) {
                TransactionResponseDTO<BlockbookTransactionDTO> transactionDTOTransactionResponseDTO = getBlockbookTransactions(btcUrl, address, Constant.BTC_BLOCKBOOK_DIVIDER, startIndex, limit);
                return TransactionMapper.toTransactionResponseDTO(transactionDTOTransactionResponseDTO);
            }
        }, ETH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("ETHUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "0x5eD8Cee6b63b1c6AFce3AD7c92f4fD7E1B8fAd9F";

                return getBlockbookBalance(ethUrl, address, Constant.ETH_BLOCKBOOK_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO<TransactionDTO> getTransactions(String address, Integer startIndex, Integer limit) {
                TransactionResponseDTO<BlockbookTransactionDTO> transactionDTOTransactionResponseDTO = getBlockbookTransactions(ethUrl, address, Constant.ETH_BLOCKBOOK_DIVIDER, startIndex, limit);
                return TransactionMapper.toTransactionResponseDTO(transactionDTOTransactionResponseDTO);
            }
        }, BCH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BCHABCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "bitcoincash:pp8skudq3x5hzw8ew7vzsw8tn4k8wxsqsv0lt0mf3g";

                return getBlockbookBalance(bchUrl, address, Constant.BCH_BLOCKBOOK_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO<TransactionDTO> getTransactions(String address, Integer startIndex, Integer limit) {
                TransactionResponseDTO<BlockbookTransactionDTO> transactionDTOTransactionResponseDTO = getBlockbookTransactions(bchUrl, address, Constant.BCH_BLOCKBOOK_DIVIDER, startIndex, limit);
                return TransactionMapper.toTransactionResponseDTO(transactionDTOTransactionResponseDTO);
            }
        }, LTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("LTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "MH1RcrnDDttNBmz6XLRK4vJbUGp36nmThv";

                return getBlockbookBalance(ltcUrl, address, Constant.LTC_BLOCKBOOK_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return getBlockbookTransactionNumber(ltcUrl, address, amount, Constant.LTC_BLOCKBOOK_DIVIDER);
            }

            @Override
            public TransactionResponseDTO<TransactionDTO> getTransactions(String address, Integer startIndex, Integer limit) {
                TransactionResponseDTO<BlockbookTransactionDTO> transactionDTOTransactionResponseDTO = getBlockbookTransactions(ltcUrl, address, Constant.LTC_BLOCKBOOK_DIVIDER, startIndex, limit);
                return TransactionMapper.toTransactionResponseDTO(transactionDTOTransactionResponseDTO);
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
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO<TransactionDTO> getTransactions(String address, Integer startIndex, Integer limit) {
                TransactionPage page = getBinanceDEXTransactions(address);
                return TransactionMapper.toTransactionResponseDTO(page, address);
            }
        }, XRP {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("XRPUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "r3kmLJN5D28dHuH8vZNUZpMC43pEHpaocV";

                return getRippledBalance(address, Constant.XRP_BLOCKBOOK_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO<TransactionDTO> getTransactions(String address, Integer startIndex, Integer limit) {
                List<RippledTransactionDTO> rippledTransactionDTOS = getRippledTransactions(address, Constant.XRP_BLOCKBOOK_DIVIDER);
                return TransactionMapper.toTransactionResponseDTO(rippledTransactionDTOS, address);
            }
        }, TRX {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("TRXUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                //address = "TFKCrg61s7Q9oWZAM9Fy4ua6ZvSAom8j7V";

                return getTrongridBalance(trxUrl, address, Constant.TRX_BLOCKBOOK_DIVIDER);
            }

            @Override
            public TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount) {
                return null;
            }

            @Override
            public TransactionResponseDTO<TransactionDTO> getTransactions(String address, Integer startIndex, Integer limit) {
                List<TrongridTransactionDTO> rippledTransactionDTOS = getTrongridTransactions(address, startIndex, Constant.TRX_BLOCKBOOK_DIVIDER);
//                return TransactionMapper.toTransactionResponseDTO(rippledTransactionDTOS, address); // todo
                return null;
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);

        public abstract TransactionNumberDTO getTransactionNumber(String address, BigDecimal amount);

        public abstract TransactionResponseDTO<TransactionDTO> getTransactions(String address, Integer startIndex, Integer limit);
    }

    @Scheduled(fixedDelay = 600_000)
    public void scheduleChainalysisTransactionRegistrationDelayTask() {
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
            transaction.setCryptoAddress(transaction.getCryptoAddress().split(":")[0]);

            CoinEnum coinEnum = CoinEnum.valueOf(transaction.getCryptoCurrency());
            TransactionNumberDTO trxNumberDTO = coinEnum.getTransactionNumber(transaction.getCryptoAddress(), transaction.getCryptoAmount());
            transaction.setDetail(trxNumberDTO.getTransactionId());
            transaction.setN(trxNumberDTO.getN());

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
                : String.format("%s:%d", transaction.getDetail(), transaction.getN());

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
        } catch (HttpClientErrorException he) {
            System.out.println("-------------------------------------- url:\n");
            System.out.println(url);

            System.out.println("-------------------------------------- request:\n");
            System.out.println(request);

            he.printStackTrace();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }

        return result;
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

    public TransactionResponseDTO<TransactionDTO> getTransactions(Long userId, CoinEnum coin, Integer startIndex) {
        startIndex = startIndex == null || startIndex <= 0 ? 1 : startIndex;
        TransactionResponseDTO<TransactionDTO> result = new TransactionResponseDTO<>();
        int defaultLimit = 10;

        try {
            String address = getCoinAddressByUserIdAndCoin(userId, coin.name());
            result = coin.getTransactions(address, startIndex, defaultLimit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static TransactionResponseDTO<BlockbookTransactionDTO> getBlockbookTransactions(String url, String address, Long divider, Integer fromIndex, Integer limit) {
        TransactionResponseDTO<BlockbookTransactionDTO> result = new TransactionResponseDTO<>();
        List<BlockbookTransactionDTO> blockbookTransactionDTOList = new ArrayList<>();

        double pageSize = 1000;

        try {
            for (int i = 1; i <= Math.ceil((fromIndex + limit) / pageSize); i++) {
                JSONObject res = rest.getForObject(url + "/api/v2/address/" + address + "?details=txs&pageSize=1000&page=" + i, JSONObject.class);
                JSONArray transactionsArray = res.optJSONArray("transactions");

                if (transactionsArray != null) {
                    transactionsArray.forEach(t -> blockbookTransactionDTOList.add(TransactionMapper.toBlockbookTransactionDTO((JSONObject) t, address, divider)));
                }

                result.setTotalPages(res.getInt("totalPages"));
                result.setItemsOnPage(res.optInt("itemsOnPage"));
                result.setAddress(res.optString("address"));
                result.setTxs(res.optLong("txs"));
                result.setTransactions(blockbookTransactionDTOList);

                if (res.getInt("page") == res.getInt("totalPages")) {
                    break;
                }
            }

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

    private static TransactionPage getBinanceDEXTransactions(String address) {
        try {
            TransactionsRequest transactionsRequest = new TransactionsRequest();
            transactionsRequest.setStartTime(new SimpleDateFormat("yyyy").parse("2010").getTime());
            transactionsRequest.setEndTime(new Date().getTime() + TimeUnit.DAYS.toMillis(1));
            transactionsRequest.setTxType(TransactionType.TRANSFER);
            transactionsRequest.setAddress(address);
            transactionsRequest.setTxAsset("BNB");
            transactionsRequest.setLimit(1000);

            return binanceDex.getTransactions(transactionsRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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

    private static List<RippledTransactionDTO> getRippledTransactions(String address, long divider) {
        List<RippledTransactionDTO> result = new ArrayList<>();
        try {
            JSONObject param = new JSONObject();
            param.put("account", address);
            param.put("binary", false);
            param.put("forward", false);
            param.put("ledger_index_max", -1);
            param.put("ledger_index_min", -1);
            param.put("limit", 1000);

            JSONArray params = new JSONArray();
            params.add(param);

            JSONObject req = new JSONObject();
            req.put("method", "account_tx");
            req.put("params", params);

            JSONObject res = rest.postForObject(xrpUrl, req, JSONObject.class);
            JSONObject jsonResult = res.optJSONObject("result");
            JSONArray transactionsJsonArr = jsonResult.optJSONArray("transactions");

            if (transactionsJsonArr != null) {
                transactionsJsonArr.forEach(t -> result.add(TransactionMapper.toRippledTransactionDTO((JSONObject) t, divider)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // todo complete
    private static List<TrongridTransactionDTO> getTrongridTransactions(String address, Integer startIndex, long divider) {
        List<TrongridTransactionDTO> result = new ArrayList<>();
        String nextPageUrl = trxUrl;

        try {
            do {
                JSONObject res = rest.getForObject(nextPageUrl, JSONObject.class);
                JSONObject meta = res.optJSONObject("meta");
                JSONObject links = meta.optJSONObject("links");

                nextPageUrl = links.optString("next");

                JSONArray data = res.optJSONArray("data");
                if (data != null) {
                    data.forEach(t -> result.add(TransactionMapper.toTrongridTransactionDTO((JSONObject) t, divider)));
                }

                if (nextPageUrl == null || nextPageUrl.equals("")) {
                    break;
                }

            } while (result.size() < startIndex + 10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
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
