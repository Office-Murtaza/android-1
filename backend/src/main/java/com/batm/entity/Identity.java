package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "identity")
public class Identity extends BaseEntity {

    public static final int STATE_REGISTERED = 1;

    @Column(name = "publicid")
    private String publicId;

    @Column(name = "externalid")
    private String externalId;

    @Column(name = "type")
    private Integer type;

    @Column(name = "state")
    private int state;

    private BigDecimal vipbuydiscount;
    private BigDecimal vipselldiscount;

    @Column(name = "created")
    private Date created;

    @Column(name = "registered")
    private Date registered;

    @Column(name = "lastupdatedat")
    private Date lastUpdatedAt;

    @Column(name = "configurationcashcurrency")
    private String configurationCashCurrency;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "identity_limit_cptr",
            joinColumns = {@JoinColumn(name = "identity_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "limit_id", referencedColumnName = "id")})
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerTransaction;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "identity_limit_cph",
            joinColumns = {@JoinColumn(name = "identity_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "limit_id", referencedColumnName = "id")})
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerHour;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "identity_limit_cpd",
            joinColumns = {@JoinColumn(name = "identity_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "limit_id", referencedColumnName = "id")})
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerDay;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "identity_limit_cpw",
            joinColumns = {@JoinColumn(name = "identity_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "limit_id", referencedColumnName = "id")})
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerWeek;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "identity_limit_cpm",
            joinColumns = {@JoinColumn(name = "identity_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "limit_id", referencedColumnName = "id")})
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerMonth;

    @Column(name = "watchlistlastscanat")
    private Date watchListLastScanAt;

    @Column(name = "watchlistbanned")
    private Boolean watchListBanned;

    @Column(name = "note", columnDefinition = "MEDIUMTEXT")
    private String note;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "identity")
    private List<TransactionRecord> transactionRecords;

    @OneToMany(mappedBy = "identity")
    private List<TransactionRecordGift> transactionRecordGifts;

    @OneToMany(mappedBy = "identity")
    private List<TransactionRecordC2C> transactionRecordC2C;
}