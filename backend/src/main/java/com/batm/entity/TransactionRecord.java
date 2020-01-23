package com.batm.entity;

import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transactionrecord")
public class TransactionRecord extends BaseEntity {

    @Column(
            name = "type"
    )
    private Integer type = Integer.valueOf(0);

    @Column(
            name = "remotetid"
    )
    private String remoteTransactionId;

    @Column(
            name = "relatedremotetid"
    )
    private String relatedRemoteTransactionId;

    @Column(
            name = "localtid"
    )
    private String localTransactionId;

    @Column(
            name = "uuid"
    )
    private String uuid;

    @Column(
            name = "status"
    )
    private int status;

    @Column(
            name = "detail"
    )
    private String detail;

    private Integer n;

    @Column(
            name = "cashamount",
            precision = 20,
            scale = 10
    )
    private BigDecimal cashAmount;

    @Column(
            name = "cashcurrency"
    )
    private String cashCurrency;

    @Column(
            name = "cryptoamount",
            precision = 20,
            scale = 10
    )
    private BigDecimal cryptoAmount;

    @Column(
            name = "cryptocurrency"
    )
    private String cryptoCurrency;

    @Column(
            name = "cryptoaddress",
            columnDefinition = "TEXT"
    )
    private String cryptoAddress;

    @Column(
            name = "servertime"
    )
    private Date serverTime;

    @Column(
            name = "terminaltime"
    )
    private Date terminalTime;

    @Column(
            name = "exchangestrategyused"
    )
    private int exchangeStrategyUsed;

    @Column(
            name = "errorcode"
    )
    private int errorCode;

    @ManyToOne
    private Identity identity;

    @Column(
            name = "sold"
    )
    private Boolean sold;

    @Column(
            name = "purchased"
    )
    private Boolean purchased;

    @Column(
            name = "withdrawn"
    )
    private Boolean withdrawn;

    @Column(
            name = "canbecashedout"
    )
    private Boolean canBeCashedOut;

    @Column(
            name = "note",
            columnDefinition = "TEXT"
    )
    private String note;

    @Column(
            name = "cellphoneused"
    )
    private String cellPhoneUsed;

    @Column(
            name = "canbeallocatedforwithdrawal"
    )
    private Boolean canBeAllocatedForWithdrawal;

    @Column(
            name = "risk"
    )
    private Boolean risk;

    @Column(
            name = "autoexecuted"
    )
    private Boolean autoexecuted;

    @Column(
            name = "discountcode"
    )
    private String discountCode;

    @Column(
            name = "feediscount",
            precision = 20,
            scale = 10
    )
    private BigDecimal feeDiscount;

    @Column(
            name = "cryptodiscountamount",
            precision = 20,
            scale = 10
    )
    private BigDecimal cryptoDiscountAmount;

    @Column(
            name = "discountquotient",
            precision = 20,
            scale = 10
    )
    private BigDecimal discountQuotient;

    @Column(
            name = "fixedtransactionfee",
            precision = 20,
            scale = 10
    )
    private BigDecimal fixedTransactionFee;

    @Column(
            name = "expectedprofitsetting",
            precision = 20,
            scale = 10
    )
    private BigDecimal expectedProfitSetting;

    @Column(
            name = "expectedprofitvalue",
            precision = 20,
            scale = 10
    )
    private BigDecimal expectedProfitValue;

    private Integer tracked;
    private Integer notified;

    @Column(name = "ratesourceprice", precision = 20, scale = 10)
    private BigDecimal rateSourcePrice;

    @Column(name = "nameofcryptosettingused")
    private String nameOfCryptoSettingUsed;

    @Transient
    public TransactionType getTransactionType() {
        return getType() == 1 ? TransactionType.SELL : TransactionType.BUY;
    }

    @Transient
    public TransactionStatus getTransactionStatus(TransactionType type) {
        TransactionStatus status = TransactionStatus.PENDING; // just in case... use Pending as default
        if (getStatus() == 0) {
            status = TransactionStatus.PENDING;
        } else if (getStatus() == 2) {
            status = TransactionStatus.FAIL;
        }
        else if ((type == TransactionType.SELL && getStatus() == 3)
                || (type == TransactionType.BUY && getStatus() == 1)) {
            status = TransactionStatus.COMPLETE;
        }
        return status;
    }
}