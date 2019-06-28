package com.batm.rest.vm;

import java.util.List;

import com.batm.dto.UserCoinDTO;
import com.batm.entity.UserCoin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * View Model object for storing a user's credentials.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoinVM {
	
	private Long userId;

	private List<UserCoinDTO> coins;
}
