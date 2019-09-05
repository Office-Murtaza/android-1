package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
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

    private static final long serialVersionUID = 9027143155135946959L;

    private String txid;
    private Integer index;
    private BigDecimal value;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionStatus status;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private TransactionType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date;

    public enum TransactionStatus {
        UNKNOWN(0),
        PENDING(1),
        COMPLETE(2),
        FAIL(3);

        private int value;

        private TransactionStatus(int value) {
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }
    }

    public enum TransactionType {
        DEPOSIT(1),
        WITHDRAW(2),
        SEND_GIFT(3),
        RECEIVE_GIFT(4),
        BUY(5),
        SELL(6);

        private int value;

        private TransactionType(int value) {
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }
    }
}
