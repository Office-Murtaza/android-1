package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "staking")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StakingDetailsDTO {

    @Id
    private String id;

    @Indexed
    private String coin;

    @Indexed
    private Long userId;

    private Integer status;
    private BigDecimal cryptoAmount;
    private Integer basePeriod;
    private Integer annualPeriod;
    private Integer holdPeriod;
    private Integer annualPercent;

    private String createTxId;
    private Long createTimestamp;
    private String cancelTxId;
    private Long cancelTimestamp;
    private String withdrawTxId;
    private Long withdrawTimestamp;
}