package com.batm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class GethTx {

    @Id
    private String txId;

    @Indexed
    private String fromAddress;

    @Indexed
    private String toAddress;

    private BigDecimal amount;
    private BigDecimal fee;
    private int blockNumber;
    private long blockTime;
}