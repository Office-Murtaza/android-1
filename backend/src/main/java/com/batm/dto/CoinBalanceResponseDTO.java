package com.batm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoinBalanceResponseDTO {

	private String coinId;

	private String publicKey;
	
	private Double balance;
	
	private Price price;
	
	private transient Integer orderIndex;
	
}
