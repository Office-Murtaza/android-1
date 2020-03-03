package com.batm.entity;

import lombok.*;
import javax.persistence.*;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "identitypiecepersonalinfo")
public class IdentityPiecePersonalInfo extends BaseEntity {

    @ManyToOne(optional = false)
    private Identity identity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "identitypiece_id")
    private IdentityPiece identityPiece;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "contactaddress")
    private String address;

    @Column(name = "contactcountry")
    private String country;

    @Column(name = "contactprovince")
    private String province;

    @Column(name = "contactcity")
    private String city;

    @Column(name = "contactzip")
    private String zip;

    @Column(name = "idcardnumber")
    private String idCardNumber;

    @Column(name = "ssn")
    private String ssn;

    @Column(name = "created")
    private Date created;
}