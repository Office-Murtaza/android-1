package com.batm.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.batm.rest.vm.CoinVM;
import com.batm.service.UserCoinService;

@RestController
@RequestMapping("/api/v1")
public class CoinController {

	@Autowired
	private UserCoinService userCoinService;
	
	@PostMapping("/user/add-coins")
	public ResponseEntity<?> addCoins(@RequestBody CoinVM coinVM) {
		try {
			userCoinService.save(coinVM);
		} catch (Exception e) {
			return new ResponseEntity<>(new com.batm.entity.Error(1, "Something has been wrong."), HttpStatus.OK);
		}
		Map<String, String> response = new HashMap<>();
		response.put("userId", coinVM.getUserId() + "");
		response.put("isCoinsAdded", true + "");
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
