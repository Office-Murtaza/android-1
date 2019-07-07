package com.batm.rest.vm;

import java.util.List;

import com.batm.dto.CoinBalanceResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoinBalanceVM {

	private Long userId;

	private List<CoinBalanceResponseDTO> coins;
}
