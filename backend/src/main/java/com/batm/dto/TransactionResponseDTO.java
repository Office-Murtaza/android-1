package com.batm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO<T> {

    private Integer totalPages;
    private Integer itemsOnPage;
    private String address;
    private Integer txs;

    private List<T> transactions;
}
