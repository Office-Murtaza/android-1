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
    @NotNull(message = "{userVerification.field.required}")
    @Min(value = 1, message = "{userVerification.tierId.minMax}")
    @Max(value = 2, message = "{userVerification.tierId.minMax}")
    private Integer tierId;

    @NotNull(message = "{userVerification.field.required}")
    private MultipartFile file;

    @NotBlank(groups = BasicVerificationValidator.class, message = "{userVerification.field.required}")
    @Pattern(groups = BasicVerificationValidator.class, regexp = "\\d{9}")
    private String idNumber;

    @NotBlank(groups = VIPVerificationValidator.class, message = "{userVerification.field.required}")
    @Pattern(groups = VIPVerificationValidator.class, regexp = "\\d{9}")
    private String ssn;

    @NotBlank(groups = BasicVerificationValidator.class, message = "{userVerification.field.required}")
    @Pattern(groups = BasicVerificationValidator.class, regexp = "[a-zA-Z]{2,255}")
    private String firstName;

    @NotBlank(groups = BasicVerificationValidator.class, message = "{userVerification.field.required}")
    @Pattern(groups = BasicVerificationValidator.class, regexp = "[a-zA-Z]{2,255}")
    private String lastName;

    @NotBlank(groups = BasicVerificationValidator.class, message = "{userVerification.field.required}")
    @Pattern(groups = BasicVerificationValidator.class, regexp = "[A-Za-z0-9'.\\-\\s,]{2,255}")
    private String address;

    @NotBlank(groups = BasicVerificationValidator.class, message = "{userVerification.field.required}")
    @Pattern(groups = BasicVerificationValidator.class, regexp = "[a-zA-Z]{2,255}")
    private String country;

    @NotBlank(groups = BasicVerificationValidator.class, message = "{userVerification.field.required}")
    @Pattern(groups = BasicVerificationValidator.class, regexp = "[a-zA-Z]{2,255}")
    private String province;

    @NotBlank(groups = BasicVerificationValidator.class, message = "{userVerification.field.required}")
    @Pattern(groups = BasicVerificationValidator.class, regexp = "[a-zA-Z]{2,255}")
    private String city;

    @NotBlank(groups = BasicVerificationValidator.class, message = "{userVerification.field.required}")
    @Pattern(groups = BasicVerificationValidator.class, regexp = "\\d{5}")
    private String zipCode;
}
