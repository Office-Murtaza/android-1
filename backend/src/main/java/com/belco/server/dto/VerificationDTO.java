package com.belco.server.dto;

import com.belco.server.model.VerificationStatus;
import com.belco.server.model.VerificationTier;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationDTO {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private VerificationStatus status;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private VerificationTier tier;

    private String firstName;
    private String lastName;
    private String address;
    private String country;
    private String province;
    private String city;
    private String zipCode;
    private String idCardNumber;
    private String idCardNumberFilename;
    private String ssn;
    private String ssnFilename;
    private String message;
    private BigDecimal txLimit;
    private BigDecimal dailyLimit;
    private boolean autoConfirm;
}