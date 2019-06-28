package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Error {

    private Integer errorCode;
    private String errorMsg;
}