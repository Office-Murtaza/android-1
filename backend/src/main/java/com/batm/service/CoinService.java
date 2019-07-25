package com.batm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.batm.dto.CoinBalanceDTO;
import com.batm.dto.Price;
import com.batm.dto.UserCoinDTO;
import com.batm.entity.Coin;
import com.batm.entity.Response;
import com.batm.entity.User;
import com.batm.entity.UserCoin;
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

        this.binance = binance;
        this.binanceDex = binanceDex;
        this.rest = rest;
        this.btcUrl = btcUrl;
        this.ethUrl = ethUrl;
        this.bchUrl = bchUrl;
        this.ltcUrl = ltcUrl;
        this.trxUrl = trxUrl;
        this.xrpUrl = xrpUrl;
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
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);
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
    	if(index >= 0) {
    		coin = coins.get(index);
    	}
    	return coin;
    }

    public void save(CoinVM coinVM, Long userId) {
        User user = userRepository.getOne(userId);
        List<Coin> coins = this.coinRepository.findAll();
        List<UserCoin> userCoins = this.userCoinRepository.findAll();
        
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
    	Response response = null; 
    	
         List<UserCoin> userCoins = this.userCoinRepository.findByUserUserId(userId);
         
    	for (UserCoinDTO userCoin : coinVM.getCoins()) {
             UserCoin tempUserCoin = new UserCoin(userCoin.getCoinCode());
             int index = userCoins.indexOf(tempUserCoin);
             if(index <0) {
            	 return Response.error(new com.batm.entity.Error(3, "Coin does not exist"));
             }
             
             String dbPublicKey = userCoins.get(index).getPublicKey();
             if (userCoin.getPublicKey() == null
                     || !userCoin.getPublicKey().equalsIgnoreCase(dbPublicKey)) {
                 return Response.error(new com.batm.entity.Error(3, "Public keys not match"));
             }
         }
    	
    	return response;
    }
    
    

    public UserCoin getCoinWithUserIdAndCoinCode(Long userId, String coinCode) {
        return userCoinRepository.findByUserUserIdAndCoinId(userId, coinCode);
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
