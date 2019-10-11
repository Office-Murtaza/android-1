package com.batm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transactionrecord")
//@NamedEntityGraphs(value = {
//        @NamedEntityGraph(
//                name = Transaction.IDENTITY,
//                attributeNodes = @NamedAttributeNode("identity")
//        )
//})
public class Transaction implements Serializable {

    private static final long serialVersionUID = -1712969322089989538L;

    public static final String IDENTITY = "Transaction[identity]";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long transactionId;

    private Integer type;

    @Transient
    private Integer n;

    private String detail;
    private Integer status;
    private Boolean tracked;

    @Column(name = "cryptocurrency")
    private String cryptoCurrency;

    @Column(name = "cryptoaddress")
    private String cryptoAddress;

    @Column(name = "cryptoamount")
    private BigDecimal cryptoAmount;

//    @JsonBackReference
//    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private Identity identity;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Transaction other = (Transaction) obj;
        if (transactionId == null) {
            if (other.transactionId != null)
                return false;
        } else if (!transactionId.equals(other.transactionId))
            return false;
        return true;
    }
}