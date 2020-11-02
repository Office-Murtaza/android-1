package com.belco.server.dto;

import com.belco.server.entity.TransactionRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChainalysisResponseDTO {

    private TransactionRecord transactionRecord;
}