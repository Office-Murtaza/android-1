package com.batm.dto;

import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO implements Serializable {

    private Integer index;
    private String txid;

    private BigDecimal value;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionStatus status;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date;
}