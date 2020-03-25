package com.batm.dto;

import com.batm.dto.validation.BasicVerificationValidator;
import com.batm.dto.validation.VIPVerificationValidator;
import com.batm.dto.validation.VerificationValidationSeqProvider;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;

@GroupSequenceProvider(VerificationValidationSeqProvider.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVerificationDTO {
    @NotNull(message = "{userVerification.tierId.required}")
    @Min(value = 1, message = "{userVerification.tierId.minMax}")
    @Max(value = 2, message = "{userVerification.tierId.minMax}")
    private Integer tierId;

    @NotNull(message = "{userVerification.file.required}")
    private MultipartFile file;

    @NotBlank(groups = BasicVerificationValidator.class,
            message = "{userVerification.idNumber.required}")
    @Pattern(groups = BasicVerificationValidator.class,
            regexp = "\\d{9}",
            message = "{userVerification.idNumber.pattern}")
    private String idNumber;

    @NotBlank(groups = VIPVerificationValidator.class,
            message = "{userVerification.ssn.required}")
    @Pattern(groups = VIPVerificationValidator.class,
            regexp = "\\d{9}",
            message = "{userVerification.ssn.pattern}")
    private String ssn;

    @NotBlank(groups = BasicVerificationValidator.class,
            message = "{userVerification.firstName.required}")
    @Pattern(groups = BasicVerificationValidator.class,
            regexp = "[a-zA-Z]{2,255}",
            message = "{userVerification.firstName.pattern}")
    private String firstName;

    @NotBlank(groups = BasicVerificationValidator.class,
            message = "{userVerification.lastName.required}")
    @Pattern(groups = BasicVerificationValidator.class,
            regexp = "[a-zA-Z]{2,255}",
            message = "{userVerification.lastName.pattern}")
    private String lastName;

    @NotBlank(groups = BasicVerificationValidator.class,
            message = "{userVerification.address.required}")
    @Pattern(groups = BasicVerificationValidator.class,
            regexp = "[A-Za-z0-9'.\\-\\s,]{2,255}",
            message = "{userVerification.address.pattern}")
    private String address;

    @NotBlank(groups = BasicVerificationValidator.class,
            message = "{userVerification.country.required}")
    @Pattern(groups = BasicVerificationValidator.class,
            regexp = "[a-zA-Z]{2,255}",
            message = "{userVerification.country.pattern}")
    private String country;

    @NotBlank(groups = BasicVerificationValidator.class,
            message = "{userVerification.province.required}")
    @Pattern(groups = BasicVerificationValidator.class,
            regexp = "[a-zA-Z]{2,255}",
            message = "{userVerification.province.pattern}")
    private String province;

    @NotBlank(groups = BasicVerificationValidator.class,
            message = "{userVerification.city.required}")
    @Pattern(groups = BasicVerificationValidator.class,
            regexp = "[a-zA-Z]{2,255}",
            message = "{userVerification.city.pattern}")
    private String city;

    @NotBlank(groups = BasicVerificationValidator.class,
            message = "{userVerification.zipCode.required}")
    @Pattern(groups = BasicVerificationValidator.class,
            regexp = "\\d{5}",
            message = "{userVerification.zipCode.pattern}")
    private String zipCode;
}
