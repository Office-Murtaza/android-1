package com.belco.server.dto;

import com.belco.server.model.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationDetailsDTO {

    private VerificationStatus status;
    private BigDecimal txLimit;
    private BigDecimal dailyLimit;
    private String message;
}