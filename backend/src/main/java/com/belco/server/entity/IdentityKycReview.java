package com.belco.server.entity;

import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_identitykycreview")
public class IdentityKycReview extends BaseEntity {

    @OneToOne(optional = false)
    private Identity identity;

    @Column(name = "tier_id")
    private Integer tierId;

    @Column(name = "review_status")
    private Integer reviewStatus;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String address;

    private String country;

    private String province;

    private String city;

    @Column(name = "zip_code")
    private String zip;

    @Column(name = "id_card_number")
    private String idCardNumber;

    @Column(name = "ssn")
    private String ssn;

    @Column(name = "ssn_file_name")
    private String ssnFileName;

    @Column(name = "ssn_file_mimetype")
    private String ssnFileMimeType;

    @Column(name = "id_card_file_name")
    private String idCardFileName;

    @Column(name = "id_card_file_mimetype")
    private String idCardFileMimeType;

    @Column(name = "rejected_message")
    private String rejectedMessage;
}