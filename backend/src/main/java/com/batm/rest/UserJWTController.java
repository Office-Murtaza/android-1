package com.batm.rest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.Valid;
import com.batm.util.Constant;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.batm.entity.CodeVerification;
import com.batm.entity.Error;
import com.batm.entity.RefreshToken;
import com.batm.entity.Response;
import com.batm.entity.User;
import com.batm.repository.RefreshTokenRepository;
import com.batm.rest.vm.LoginVM;
import com.batm.rest.vm.RefreshVM;
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

@RestController
@RequestMapping("/api/v1")
public class UserJWTController {

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

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${security.jwt.access-token-duration}")
    private Long expiryTime;
    
    @Value("${security.verification.code-validity}")
    private Long verificationCodeValidity;
   

    @PostMapping("/user/register")
    public Response registerAccount(@Valid @RequestBody RegisterVM register) {
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

        this.refreshTokenRepository.save(new RefreshToken(jwt.getRefreshToken(), user));
        return Response.ok(jwt);
    }

    @PostMapping("/user/recover")
    public Response recoverAccount(@Valid @RequestBody LoginVM loginVM) {
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

        RefreshToken token = this.refreshTokenRepository.findByUserUserId(user.getUserId());
        token.setToken(jwt.getRefreshToken());
        this.refreshTokenRepository.save(token);
        return Response.ok(jwt);
    }

    @PostMapping("/user/verify")
    public Response validateVerficationCode(@RequestBody ValidateOTPVM validateOtpVM) {
        CodeVerification codeVerification = codeVerificationService.getCodeByUserId(validateOtpVM.getUserId());
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

        return Response.ok(new ValidateOTPResponse(validateOtpVM.getUserId(), true));
    }

    @PostMapping("/user/refresh")
    public Response refresh(@Valid @RequestBody RefreshVM refreshVM) {

        RefreshToken refreshToken = this.refreshTokenRepository.findByTokenAndUserUserId(refreshVM.getRefreshToken(), refreshVM.getUserId());
        if (refreshToken != null) {
            User user = userService.findById(refreshVM.getUserId());

            JWTToken jwt = getJwt(user);

            RefreshToken token = this.refreshTokenRepository.findByUserUserId(user.getUserId());
            token.setToken(jwt.getRefreshToken());
            this.refreshTokenRepository.save(token);
            return Response.ok(jwt);

        } else {
            throw new AccessDeniedException("Refresh token not exist");
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
    	 UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getPhone(),
                 new String(Base64.decodeBase64(user.getPassword())));

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