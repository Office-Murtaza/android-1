package com.belco.server.dto;

import com.belco.server.model.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KycDetailsDTO {

    private VerificationStatus status;
    private BigDecimal txLimit;
    private BigDecimal dailyLimit;
    private String message;
}