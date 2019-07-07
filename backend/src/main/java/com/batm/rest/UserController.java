package com.batm.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1")
public class UserController {

	@Autowired
	private UserCoinService userCoinService;

	@PostMapping("/user/{userId}/coins-compare")
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
	
	@PostMapping("/user/{userId}/coins-balance")
	public Response compareCoins(@PathVariable Long userId) {
		List<CoinBalanceResponseDTO> balances = new ArrayList<>();
		try {
			List<UserCoin> userCoins = this.userCoinService.getCoinByUserId(userId);
			userCoins.stream().forEach(userCoin ->{
				balances.add(new CoinBalanceResponseDTO(userCoin.getCoin().getId(), userCoin.getPublicKey(), 1.0, new Price(2.0)));
			});
			
			return Response.ok(new CoinBalanceVM(userId, balances));
		} catch (Exception e) {
			return Response.error(new com.batm.entity.Error(1, "Something has been wrong."));
		}

	}

}
