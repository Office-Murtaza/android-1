package com.batm.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.batm.dto.CoinBalanceResponseDTO;
import com.batm.dto.Price;
import com.batm.dto.UserCoinDTO;
import com.batm.entity.Response;
import com.batm.entity.UserCoin;
import com.batm.rest.vm.CoinBalanceVM;
import com.batm.rest.vm.CoinVM;
import com.batm.service.UserCoinService;
import com.binance.api.client.BinanceApiRestClient;

@RestController
@RequestMapping("/api/v1")
public class CoinController {

	@Autowired
	private UserCoinService userCoinService;
	
	@Autowired
	private BinanceApiRestClient binanceApiRestClient;
	
	@PostMapping("/user/{userId}/coins/add")
	public Response addCoins(@RequestBody CoinVM coinVM, @PathVariable Long userId) {
		if(coinVM == null || coinVM.getCoins().isEmpty()) {
			return Response.error(new com.batm.entity.Error(1, "Empty coin list."));
		}
		try {
			userCoinService.save(coinVM,userId);
		} catch (Exception e) {
			return Response.error(new com.batm.entity.Error(1, "Something has been wrong."));
		}
		Map<String, String> response = new HashMap<>();
		response.put("userId", userId + "");
		response.put("isCoinsAdded", true + "");
		return Response.ok(response);

	}
	
	@PostMapping("/user/{userId}/coins/compare")
	public Response compareCoins(@RequestBody CoinVM coinVM, @PathVariable Long userId) {
		if (coinVM == null || coinVM.getCoins().isEmpty()) {
			return Response.error(new com.batm.entity.Error(1, "Empty coin list."));
		}
		try {
			for (UserCoinDTO userCoin : coinVM.getCoins()) {
				UserCoin coinWithUserIdAndCoinCode = this.userCoinService.getCoinWithUserIdAndCoinCode(userId,
						userCoin.getCoinCode());
				if (userCoin.getPublicKey() == null
						|| !userCoin.getPublicKey().equalsIgnoreCase(coinWithUserIdAndCoinCode.getPublicKey())) {
					return Response.error(new com.batm.entity.Error(1, "Public keys not match."));
				}
			}

		} catch (Exception e) {
			return Response.error(new com.batm.entity.Error(1, "Something has been wrong."));
		}
		Map<String, String> response = new HashMap<>();
		response.put("isCoinsMatched", true + "");
		return Response.ok(response);

	}

	@GetMapping("/user/{userId}/coins/balance")
	public Response compareCoins(@PathVariable Long userId) {
		List<CoinBalanceResponseDTO> balances = new ArrayList<>();
		try {
			BigDecimal totalBalance = new BigDecimal("0");
			List<UserCoin> userCoins = this.userCoinService.getCoinByUserId(userId);
			for (UserCoin userCoin : userCoins) {
				String coinCode = userCoin.getCoin().getId();
				if (coinCode.equalsIgnoreCase("BCH")) {
					coinCode = "BCHABC";
				}

				String prc = binanceApiRestClient.getPrice(coinCode + "USDT").getPrice();
				BigDecimal price = new BigDecimal(prc);
				totalBalance = totalBalance.add(price);
				balances.add(new CoinBalanceResponseDTO(userCoin.getCoin().getId(), userCoin.getPublicKey(),
						new BigDecimal("1"), new Price(price), userCoin.getCoin().getOrderIndex()));
			}

			Comparator<CoinBalanceResponseDTO> sortingByIndex = (CoinBalanceResponseDTO s1,
					CoinBalanceResponseDTO s2) -> s1.getOrderIndex().compareTo(s2.getOrderIndex());

			balances.sort(sortingByIndex);
			return Response.ok(new CoinBalanceVM(userId, balances, new Price(totalBalance)));
		} catch (Exception e) {
			return Response.error(new com.batm.entity.Error(1, "Something has been wrong."));
		}

	}

	
}
