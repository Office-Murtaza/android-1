package com.belco.server.entity;

import com.belco.server.model.VerificationStatus;
import com.belco.server.model.VerificationTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "w_verificationreview")
public class VerificationReview extends BaseEntity {

    @OneToOne
    private Identity identity;

    private Integer tier;
    private Integer status;
    private String firstName;
    private String lastName;
    private String address;
    private String country;
    private String province;
    private String city;
    private String zipCode;
    private String idCardNumber;
    private String idCardNumberFilename;
    private String idCardNumberMimetype;
    private String ssn;
    private String ssnFilename;
    private String ssnMimetype;
    private String message;

    @Transient
    public VerificationTier getVerificationTier() {
        return VerificationTier.valueOf(tier);
    }

    @Transient
    public VerificationStatus getVerificationStatus() {
        return VerificationStatus.valueOf(status);
    }
}