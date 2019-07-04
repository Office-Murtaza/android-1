package com.batm.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.batm.dto.UserCoinDTO;
import com.batm.entity.Response;
import com.batm.entity.UserCoin;
import com.batm.rest.vm.CoinVM;
import com.batm.service.UserCoinService;

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

}
