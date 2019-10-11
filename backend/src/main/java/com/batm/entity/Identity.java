package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "identity")
public class Identity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long identityId;

    @Column(name = "publicid")
    private String publicId;

    private Integer state;

    @CreationTimestamp
    @Column(name = "created", updatable = false)
    private Instant createDate = Instant.now();

    @CreationTimestamp
    @Column(name = "registered", updatable = false)
    private Instant registerDate = Instant.now();

    @UpdateTimestamp
    @Column(name = "lastupdatedat")
    private Instant updateDate = Instant.now();

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

//    @OneToOne(mappedBy = "identity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private DailyLimit dailyLimit;

//    @JsonManagedReference
//    @OneToMany(mappedBy = "identity", fetch = FetchType.LAZY)
//    private Set<Transaction> transactions;
}