package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccessDenied {

	private Integer status;
	
	private String message;
	
}
