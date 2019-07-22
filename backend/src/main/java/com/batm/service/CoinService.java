package com.batm.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import com.batm.dto.CoinBalanceDTO;
import com.batm.dto.Price;
import com.batm.rest.vm.CoinBalanceVM;
import com.batm.util.Util;
import com.binance.api.client.BinanceApiRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.batm.entity.Coin;
import com.batm.entity.User;
import com.batm.entity.UserCoin;
import com.batm.repository.CoinRepository;
import com.batm.repository.UserCoinRepository;
import com.batm.repository.UserRepository;
import com.batm.rest.vm.CoinVM;
import org.springframework.web.client.RestTemplate;

@Service
public class CoinService {

    @Autowired
    private UserCoinRepository userCoinRepository;

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private static BinanceApiRestClient binance;

    @Autowired
    private static RestTemplate rest;

    public enum CoinId {
        BTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return null;
            }
        }, ETH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("ETHUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return null;
            }
        }, BCH {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BCHABCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return null;
            }
        }, LTC {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("LTCUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return null;
            }
        }, BNB {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("BNBUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return null;
            }
        }, XRP {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("XRPUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return null;
            }
        }, TRX {
            @Override
            public BigDecimal getPrice() {
                return Util.convert(binance.getPrice("TRXUSDT").getPrice());
            }

            @Override
            public BigDecimal getBalance(String address) {
                return null;
            }
        };

        public abstract BigDecimal getPrice();

        public abstract BigDecimal getBalance(String address);
    }

    public CoinBalanceVM getCoinsBalance(Long userId, List<String> coins) {
        List<CoinBalanceDTO> balances = new ArrayList<>();
        List<UserCoin> userCoins = userCoinRepository.findByUserUserId(userId);

        List<CompletableFuture<CoinBalanceDTO>> futures = userCoins.stream()
                        .filter(it -> coins.contains(it.getCoin().getId()))
                        .map(dto -> getToDoAsync(dto))
                        .collect(Collectors.toList());

        List<CoinBalanceDTO> result = futures.stream()
                        .map(CompletableFuture::join)
                        .sorted(Comparator.comparing(CoinBalanceDTO::getOrderIndex))
                        .collect(Collectors.toList());

        BigDecimal totalBalance = result.stream()
                .map(it-> it.getPrice().getUsd().add(it.getBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CoinBalanceVM(userId, balances, new Price(totalBalance));
    }

    private CompletableFuture<CoinBalanceDTO> getToDoAsync(UserCoin userCoin) {
        return CompletableFuture.supplyAsync(() -> {
            CoinId coinId = CoinId.valueOf(userCoin.getCoin().getId());

            BigDecimal coinPrice = coinId.getPrice();
            BigDecimal coinBalance = coinId.getBalance(userCoin.getPublicKey());

            return new CoinBalanceDTO(userCoin.getCoin().getId(), userCoin.getPublicKey(), coinBalance, new Price(coinPrice), userCoin.getCoin().getOrderIndex());
        });
    }

    public void save(CoinVM coinVM, Long userId) {
        User user = userRepository.getOne(userId);

        coinVM.getCoins().stream().forEach(coinDTO -> {
            Coin code = coinRepository.findById(coinDTO.getCoinCode());
            UserCoin userCoin = new UserCoin(user, code, coinDTO.getPublicKey());
            userCoinRepository.save(userCoin);
        });
    }

    public List<UserCoin> getCoinByUserId(Long userId) {
        return userCoinRepository.findByUserUserId(userId);
    }

    public UserCoin getCoinWithUserIdAndCoinCode(Long userId, String coinCode) {
        return userCoinRepository.findByUserUserIdAndCoinId(userId, coinCode);
    }
}
