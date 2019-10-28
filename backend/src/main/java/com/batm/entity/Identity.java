package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "identity")
public class Identity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id"
    )
    private long id;

    @Column(
            name = "publicid"
    )
    private String publicId;

    @Column(
            name = "externalid"
    )
    private String externalId;

    @Column(
            name = "type"
    )
    private Integer type;

    @Column(
            name = "state"
    )
    private int state;

    @Column(
            name = "created"
    )
    private Date created;

    @Column(
            name = "registered"
    )
    private Date registered;

    @Column(
            name = "lastupdatedat"
    )
    private Date lastUpdatedAt;

    @Column(
            name = "configurationcashcurrency"
    )
    private String configurationCashCurrency;

    @OneToMany(
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "identity_limit_cptr",
            joinColumns = {@JoinColumn(
                    name = "identity_id",
                    referencedColumnName = "id"
            )},
            inverseJoinColumns = {@JoinColumn(
                    name = "limit_id",
                    referencedColumnName = "id"
            )}
    )
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerTransaction;

    @OneToMany(
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "identity_limit_cph",
            joinColumns = {@JoinColumn(
                    name = "identity_id",
                    referencedColumnName = "id"
            )},
            inverseJoinColumns = {@JoinColumn(
                    name = "limit_id",
                    referencedColumnName = "id"
            )}
    )
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerHour;

    @OneToMany(
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "identity_limit_cpd",
            joinColumns = {@JoinColumn(
                    name = "identity_id",
                    referencedColumnName = "id"
            )},
            inverseJoinColumns = {@JoinColumn(
                    name = "limit_id",
                    referencedColumnName = "id"
            )}
    )
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerDay;

    @OneToMany(
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "identity_limit_cpw",
            joinColumns = {@JoinColumn(
                    name = "identity_id",
                    referencedColumnName = "id"
            )},
            inverseJoinColumns = {@JoinColumn(
                    name = "limit_id",
                    referencedColumnName = "id"
            )}
    )
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerWeek;

    @OneToMany(
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "identity_limit_cpm",
            joinColumns = {@JoinColumn(
                    name = "identity_id",
                    referencedColumnName = "id"
            )},
            inverseJoinColumns = {@JoinColumn(
                    name = "limit_id",
                    referencedColumnName = "id"
            )}
    )
    @Fetch(FetchMode.SUBSELECT)
    private List<Limit> limitCashPerMonth;

    @Column(
            name = "watchlistlastscanat"
    )
    private Date watchListLastScanAt;

    @Column(
            name = "watchlistbanned"
    )
    private Boolean watchListBanned;

    @Column(
            name = "note",
            columnDefinition = "MEDIUMTEXT"
    )
    private String note;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "identity")
    private List<TransactionRecord> transactionRecords;

    @OneToMany(mappedBy = "identity")
    private List<TransactionRecordGift> transactionRecordGifts;

    @Transient
    public TransactionRecord getTxRecord(String txId, String coinId) {
        Optional<TransactionRecord> first = transactionRecords.stream().filter(e -> e.getDetail().equalsIgnoreCase(txId) && e.getCryptoCurrency().equalsIgnoreCase(coinId)).findFirst();

        return first.isPresent() ? first.get() : null;
    }

    @Transient
    public List<TransactionRecord> getTxRecordList(String coinId) {
        return transactionRecords.stream().filter(e -> e.getCryptoCurrency().equalsIgnoreCase(coinId)).collect(Collectors.toList());
    }

    @Transient
    public TransactionRecordGift getTxGift(String txId, String coinId) {
        Optional<TransactionRecordGift> first = transactionRecordGifts.stream().filter(e -> e.getTxId().equalsIgnoreCase(txId) && e.getCoin().getId().equalsIgnoreCase(coinId)).findFirst();

        return first.isPresent() ? first.get() : null;
    }

    @Transient
    public List<TransactionRecordGift> getTxGiftList(String coinId) {
        return transactionRecordGifts.stream().filter(e -> e.getCoin().getId().equalsIgnoreCase(coinId)).collect(Collectors.toList());
    }
}