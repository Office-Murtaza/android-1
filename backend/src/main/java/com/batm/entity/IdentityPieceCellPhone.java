package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "identitypiececellphone")
public class IdentityPieceCellPhone extends BaseEntity {

    @ManyToOne(optional = false)
    private Identity identity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "identitypiece_id")
    private IdentityPiece identityPiece;

    @Column(name = "created")
    private Date created;

    @Column(name = "phonenumber")
    private String phoneNumber;

    @Column(name = "phonelinetype")
    private Integer phoneLineType;

    @Column(name = "typeupdated")
    private Date typeUpdated;
}