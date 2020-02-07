package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.PublicKey;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignDTO {

    private String fromAddress;
    private PublicKey publicKey;
    private PrivateKey privateKey;
}