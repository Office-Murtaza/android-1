package com.batm.rest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.batm.entity.CodeVerification;
import com.batm.entity.Error;
import com.batm.entity.Response;
import com.batm.entity.Token;
import com.batm.entity.Unlink;
import com.batm.entity.UpdatePhone;
import com.batm.entity.User;
import com.batm.repository.TokenRepository;
import com.batm.rest.vm.ChangePasswordRequestVM;
import com.batm.rest.vm.CheckPasswordRequestVM;
import com.batm.rest.vm.LoginVM;
import com.batm.rest.vm.PhoneRequestVM;
import com.batm.rest.vm.RefreshVM;
import com.batm.rest.vm.RegisterVM;
import com.batm.rest.vm.UpdatePasswordRequestVM;
import com.batm.rest.vm.ValidateOTPResponse;
import com.batm.rest.vm.ValidateOTPVM;
import com.batm.security.jwt.JWTFilter;
import com.batm.security.jwt.TokenProvider;
import com.batm.service.UnlinkService;
import com.batm.service.PhoneService;
import com.batm.service.UserService;
import com.batm.service.VerificationService;
import com.batm.util.Constant;
import com.batm.util.TwilioComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private TwilioComponent twilioComponent;

    @Autowired
    private VerificationService codeVerificationService;

    @Autowired
    private TokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UnlinkService unlinkService;

    @Autowired
    private PhoneService phoneService;

    @Value("${security.jwt.access-token-duration}")
    private Long expiryTime;

    @Value("${security.verification.code-validity}")
    private Long verificationCodeValidity;

    @PostMapping("/register")
    public Response register(@Valid @RequestBody RegisterVM register) {
        try {
            Pattern pattern = Pattern.compile(Constant.REGEX_PHONE);

            Matcher matcher = pattern.matcher(register.getPhone());
            if (!matcher.matches()) {
                return Response.error(new Error(2, "Invalid phone number"));
            }

            if (!checkPasswordLength(register.getPassword())) {
                return Response.error(new Error(2, "Password length should be in 6 to 15"));
            }

            Optional<User> findOneByPhoneIgnoreCase = userService.findOneByPhoneIgnoreCase(register.getPhone());
            if (findOneByPhoneIgnoreCase.isPresent()) {
                return Response.error(new Error(1, "Phone is already registered"));
            }

            User user = userService.registerUser(register.getPhone(), register.getPassword());
            twilioComponent.sendOTP(user);
            JWTToken jwt = getJwt(user.getUserId(), register.getPhone(), register.getPassword());

            refreshTokenRepository.save(new Token(jwt.getAccessToken(), jwt.getRefreshToken(), user));

            return Response.ok(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/recover")
    public Response recover(@Valid @RequestBody LoginVM loginVM) {
        try {
            Pattern pattern = Pattern.compile(Constant.REGEX_PHONE);

            Matcher matcher = pattern.matcher(loginVM.getPhone());
            if (!matcher.matches()) {
                return Response.error(new Error(2, "Invalid phone number"));
            }

            if (!checkPasswordLength(loginVM.getPassword())) {
                return Response.error(new Error(3, "Password length should be in 6 to 15"));
            }

            Optional<User> findOneByPhoneIgnoreCase = userService.findOneByPhoneIgnoreCase(loginVM.getPhone());
            if (!findOneByPhoneIgnoreCase.isPresent()) {
                return Response.error(new Error(2, "Phone is not registered"));
            }

            User user = findOneByPhoneIgnoreCase.get();

            boolean passwordMatch = passwordEncoder.matches(loginVM.getPassword(), user.getPassword());
            if (!passwordMatch) {
                return Response.error(new Error(3, "Wrong password"));
            }

            JWTToken jwt = getJwt(user.getUserId(), loginVM.getPhone(), loginVM.getPassword());

            twilioComponent.sendOTP(user);

            Token token = this.refreshTokenRepository.findByUserUserId(user.getUserId());
            token.setRefreshToken(jwt.getRefreshToken());
            token.setAccessToken(jwt.getAccessToken());
            refreshTokenRepository.save(token);

            return Response.ok(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/verify")
    public Response verify(@RequestBody ValidateOTPVM validateOtpVM, @PathVariable Long userId) {
        try {
            CodeVerification codeVerification = codeVerificationService.getCodeByUserId(userId);
            Instant time10MinuteAge = Instant.now().minusMillis(verificationCodeValidity);
            if (!StringUtils.isEmpty(codeVerification.getCode())
                    && codeVerification.getLastModifiedDate().isBefore(time10MinuteAge)) {
                return Response.error(new Error(2, "Verification code is expired"));
            }

            if (StringUtils.equals(codeVerification.getCodeStatus(), "1")) {
                return Response.error(new Error(2, "Verification code is already used"));
            }

            if (!StringUtils.equals(validateOtpVM.getCode(), codeVerification.getCode())) {
                return Response.error(new Error(2, "Wrong verification code"));
            }

            codeVerification.setCodeStatus("1");
            codeVerificationService.save(codeVerification);

            return Response.ok(new ValidateOTPResponse(userId, true));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/refresh")
    public Response refresh(@Valid @RequestBody RefreshVM refreshVM) {
        try {
            Token refreshToken = refreshTokenRepository.findByRefreshToken(refreshVM.getRefreshToken());

            if (refreshToken != null) {
                User user = userService.findById(refreshToken.getUser().getUserId());

                JWTToken jwt = getJwt(user);

                Token token = refreshTokenRepository.findByUserUserId(user.getUserId());
                token.setRefreshToken(jwt.getRefreshToken());
                token.setAccessToken(jwt.getAccessToken());
                refreshTokenRepository.save(token);

                return Response.ok(jwt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }

        throw new AccessDeniedException("Refresh token not exist");
    }

    @GetMapping("/user/{userId}/phone")
    public Response getPhone(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("phone", user.getPhone());

            return Response.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/phone")
    public Response updatePhone(@RequestBody PhoneRequestVM phoneRequest, @PathVariable Long userId) {
        try {
        	Boolean isPhoneExist = this.userService.isPhoneExist(phoneRequest.getPhone(), userId);
        	if(isPhoneExist) {
        		return Response.error(new Error(2, "Phone is already registered"));
        	}
            phoneService.updatePhone(phoneRequest, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("smsSent", true);

            return Response.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/phone/confirm")
    public Response confirmPhone(@RequestBody ValidateOTPVM validateOtpVM, @PathVariable Long userId) {
        try {
            UpdatePhone updatePhone = phoneService.getUpdatePhone(userId);
            updatePhone = (UpdatePhone) Hibernate.unproxy(updatePhone);
            if (updatePhone.getStatus() == null || updatePhone.getStatus().intValue() == 1) {
                return Response.error(new Error(2, "Invalid request"));
            }

            CodeVerification codeVerification = codeVerificationService.getCodeByUserId(userId);
            if (StringUtils.equals("1", codeVerification.getCodeStatus())) {
                return Response.error(new Error(3, "Verification code is already used"));
            }

            if (!StringUtils.equals(validateOtpVM.getCode(), codeVerification.getCode())) {
                return Response.error(new Error(2, "Wrong verification code"));
            }

            userService.updatePhone(updatePhone.getPhone(), userId);
            updatePhone.setStatus(1);
            phoneService.save(updatePhone);

            Map<String, Object> response = new HashMap<>();
            response.put("confirmed", true);

            return Response.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/check/password")
    public Response checkPassword(@RequestBody CheckPasswordRequestVM checkPasswordRequest, @PathVariable Long userId) {
        try {
            Boolean match = Boolean.FALSE;
            User user = this.userService.findById(userId);
            if (user != null) {
                match = passwordEncoder.matches(checkPasswordRequest.getPassword(), user.getPassword());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("match", match);

            return Response.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/password")
    public Response updatePassword(@RequestBody ChangePasswordRequestVM changePasswordRequest, @PathVariable Long userId) {
		try {

			User user = this.userService.findById(userId);
			Boolean match = passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword());
			if(!match) {
				  return Response.error(new Error(2, "Old password does not match."));
			}
			String encodedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
			userService.updatePassword(encodedPassword, userId);

			Map<String, Object> response = new HashMap<>();
			response.put("updated", true);

			return Response.ok(response);
		} catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/unlink")
    public Response unlink(@PathVariable Long userId) {
        try {
            Unlink unlink = unlinkService.unlinkUser(userId);
            Map<String, Object> response = new HashMap<>();
            if (unlink != null) {
                response.put("updated", true);
            } else {
                response.put("updated", false);
            }

            return Response.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/coins/{coinId}/transactions")
    public Response getTransactions(@PathVariable Long userId, @PathVariable Long coinId, @RequestParam Integer index) {
        JSONParser parser = new JSONParser();
        try {

            JSONObject jsonObject = (JSONObject) parser.parse(" {" +
                    "\"total\": 2," +
                    "\"transactions\": [" +
                    "{" +
                    "\"index\": 1," +
                    "\"txid\": \"b53d6f6614218a6d7a6b23cd89150908e8112d8717dc2ba2c7bf2997a8c16e09\"," +
                    "\"type\": \"withdraw\"," +
                    "\"value\": 0.01," +
                    "\"status\": \"confirmed\"," +
                    "\"date\": \"2019-08-17\"" +
                    "}," +
                    "{" +
                    "\"index\": 2," +
                    "\"txid\": \"5a919ae049ea60249570216b9916dd1381608287fb339f0b3ae068ce949fca29\"," +
                    "\"type\": \"deposit\"," +
                    "\"value\": 0.01," +
                    "\"status\": \"confirmed\"," +
                    "\"date\": \"2019-08-16\"" +
                    "}" +
                    "]" +
                    "}");
            return Response.ok(jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    private JWTToken getJwt(Long userId, String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new JWTToken(userId, jwt, System.currentTimeMillis() + expiryTime, refreshToken,
                authentication.getAuthorities().stream().map(role -> role.getAuthority()).collect(Collectors.toList()));
    }

    private JWTToken getJwt(User user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getPhone(), new String(Base64.decodeBase64(user.getPassword())));

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
        String jwt = tokenProvider.createToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new JWTToken(user.getUserId(), jwt, System.currentTimeMillis() + expiryTime, refreshToken,
                authentication.getAuthorities().stream().map(role -> role.getAuthority()).collect(Collectors.toList()));
    }

    private static boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) && password.length() >= Constant.PASSWORD_MIN_LENGTH
                && password.length() <= Constant.PASSWORD_MAX_LENGTH;
    }

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
