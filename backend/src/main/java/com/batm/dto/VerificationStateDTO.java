package com.batm.dto;

import com.batm.model.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationStateDTO {
    private VerificationStatus status;
    private BigDecimal txLimit;
    private BigDecimal dailyLimit;
    private String message;
}
