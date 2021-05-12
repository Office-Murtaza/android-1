package com.belco.server.dto;

import com.belco.server.model.VerificationStatus;
import com.belco.server.model.VerificationTier;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationDTO {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private VerificationStatus status;

    private Integer tierId;
    private String phone;
    private String firstName;
    private String lastName;
    private String address;
    private String country;
    private String province;
    private String city;
    private String zipCode;
    private String idNumber;
    private String ssn;
    private String file;
    private String message;

    @JsonIgnore
    public VerificationTier getVerificationTier() {
        return VerificationTier.valueOf(tierId);
    }
}