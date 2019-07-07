package com.batm.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.batm.entity.CodeVerification;
import com.batm.entity.Coin;
import com.batm.entity.User;
import com.batm.entity.UserCoin;
import com.batm.repository.CoinRepository;
import com.batm.repository.UserCoinRepository;
import com.batm.repository.UserRepository;
import com.batm.rest.vm.CoinVM;

@Service
public class UserCoinService {

	@Autowired
	private UserCoinRepository userCoinRepository;

	@Autowired
	private CoinRepository coinRepository;

	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	public void init() {

		Coin coin = coinRepository.findById("BTC");
		if (coin == null) {
			coin = new Coin("BTC", "Bitcoin");
			coinRepository.save(coin);
		}

		coin = coinRepository.findById("ETH");
		if (coin == null) {
			coin = new Coin("ETH", "Ethereum");
			coinRepository.save(coin);
		}

		coin = coinRepository.findById("LTC");
		if (coin == null) {
			coin = new Coin("LTC", "Litecoin");
			coinRepository.save(coin);
		}

		coin = coinRepository.findById("BNB");
		if (coin == null) {
			coin = new Coin("BNB", "Binance Coin");
			coinRepository.save(coin);
		}

		coin = coinRepository.findById("TRX");
		if (coin == null) {
			coin = new Coin("TRX", "Tron");
			coinRepository.save(coin);
		}

		coin = coinRepository.findById("XRP");
		if (coin == null) {
			coin = new Coin("XRP", "Ripple");
			coinRepository.save(coin);
		}
		
		coin = coinRepository.findById("BCH");
		if (coin == null) {
			coin = new Coin("BCH", "Bitcoin Cash");
			coinRepository.save(coin);
		}

	}

	public void save(CoinVM coinVM) {
		User user = userRepository.getOne(coinVM.getUserId());

		coinVM.getCoins().stream().forEach(coinDTO -> {
			Coin code = coinRepository.findById(coinDTO.getCoinCode());
			UserCoin userCoin = new UserCoin(user, code, coinDTO.getPublicKey());
			userCoinRepository.save(userCoin);
		});

	}
	
	public List<UserCoin> getCoinByUserId(Long userId) {
		return userCoinRepository.findByUserUserId(userId);
	}
	
	public UserCoin getCoinWithUserIdAndCoinCode(Long userId,String coinCode) {
		return userCoinRepository.findByUserUserIdAndCoinId(userId, coinCode);
	}


}
