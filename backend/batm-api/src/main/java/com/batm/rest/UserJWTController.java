package com.batm.rest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.batm.entity.CodeVerification;
import com.batm.entity.Error;
import com.batm.entity.User;
import com.batm.rest.vm.LoginVM;
import com.batm.rest.vm.RegisterVM;
import com.batm.rest.vm.ValidateOTPResponse;
import com.batm.rest.vm.ValidateOTPVM;
import com.batm.security.jwt.JWTFilter;
import com.batm.security.jwt.TokenProvider;
import com.batm.service.CodeVerificationService;
import com.batm.service.UserService;
import com.batm.util.TwilioComponent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api/v1")
public class UserJWTController {

	public static final int PASSWORD_MIN_LENGTH = 6;

	public static final int PASSWORD_MAX_LENGTH = 15;

	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserService userService;

	@Autowired
	private TwilioComponent twilioComponent;

	@Autowired
	private CodeVerificationService codeVerificationService;

	@PostMapping("/login")
	public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) {

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				loginVM.getUsername(), loginVM.getPassword());

		Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		boolean rememberMe = (loginVM.isRememberMe() == null) ? false : loginVM.isRememberMe();
		String jwt = tokenProvider.createToken(authentication, rememberMe);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
		return new ResponseEntity<>(getJwt(0L, loginVM.getUsername(), loginVM.getPassword()), httpHeaders,
				HttpStatus.OK);

	}

	private JWTToken getJwt(Long userId, String username, String password) {
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
				password);

		Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		boolean rememberMe = false;
		String jwt = tokenProvider.createToken(authentication, rememberMe);
		String refreshToken = tokenProvider.createRefreshToken(authentication, rememberMe);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

		return new JWTToken(userId, jwt, System.currentTimeMillis() + 300000, refreshToken,
				authentication.getAuthorities().stream().map(role -> role.getAuthority()).collect(Collectors.toList()));
	}

	@PostMapping("/user/register")
	public ResponseEntity<?> registerAccount(@Valid @RequestBody RegisterVM register) {

		String regex = "^\\+(?:[0-9] ?){10,10}[0-9]$";

		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(register.getPhone());
		if (!matcher.matches()) {
			return new ResponseEntity<>(new Error(2, "Invalid phone number"), HttpStatus.OK);
		}

		Optional<User> findOneByPhoneIgnoreCase = userService.findOneByPhoneIgnoreCase(register.getPhone());
		if (findOneByPhoneIgnoreCase.isPresent()) {
			return new ResponseEntity<>(new Error(1, "Phone is already registered"), HttpStatus.OK);
		}

		if (!checkPasswordLength(register.getPassword())) {
			return new ResponseEntity<>(new Error(2, "Password length should be in 6 to 15"), HttpStatus.OK);
		}
		User user = userService.registerUser(register.getPhone(), register.getPassword());
		twilioComponent.sendOTP(user);
		return new ResponseEntity<>(getJwt(user.getUserId(), register.getPhone(), register.getPassword()),
				HttpStatus.OK);
	}

	@PostMapping("/user/verify")
	public ResponseEntity<?> validateBuyerOTP(@RequestBody ValidateOTPVM validateOtpVM) {
		CodeVerification codeVerification = codeVerificationService.getCodeByUserId(validateOtpVM.getUserId());
		Instant time10MinuteAge = Instant.now().minusSeconds(10 * 60);
		if (!StringUtils.isEmpty(codeVerification.getCode())
				&& codeVerification.getLastModifiedDate().isBefore(time10MinuteAge)) {
			return new ResponseEntity<>(new Error(2, "Verification code is expired"), HttpStatus.OK);
		}

		if (!StringUtils.equals(validateOtpVM.getCode(), codeVerification.getCode())) {
			return new ResponseEntity<>(new Error(2, "Wrong verification code"), HttpStatus.OK);
		}

		codeVerification.setCode("0");
		codeVerificationService.save(codeVerification);

		return new ResponseEntity<>(new ValidateOTPResponse(validateOtpVM.getUserId(), true), HttpStatus.OK);
	}

	private static boolean checkPasswordLength(String password) {
		return !StringUtils.isEmpty(password) && password.length() >= PASSWORD_MIN_LENGTH
				&& password.length() <= PASSWORD_MAX_LENGTH;
	}

	/**
	 * Object to return as body in JWT Authentication.
	 */
	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	static class JWTToken {

		private Long userId;

		private String accessToken;

		private Long expires;

		private String refreshToken;

		private List<String> roles;

	}
}
