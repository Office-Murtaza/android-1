package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationDTO {

    private String phone;
    private Integer tierId;
    private MultipartFile file;
    private String idNumber;
    private String ssn;
    private String firstName;
    private String lastName;
    private String address;
    private String country;
    private String province;
    private String city;
    private String zipCode;
}