package com.batm.rest.vm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * View Model object for storing a user's credentials.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginVM {

	private String phone;

	private String password;

}
