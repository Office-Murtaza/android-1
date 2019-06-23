package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Error {

    private String errorCode;
    private String errorMsg;
}