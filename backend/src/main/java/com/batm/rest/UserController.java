package com.batm.rest;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.Response;
import com.batm.repository.TokenRep;
import com.batm.security.TokenProvider;
import com.batm.service.CoinService;
import com.batm.service.TwilioService;
import com.batm.service.TransactionService;
import com.batm.service.UserService;
import com.batm.util.Constant;
import com.batm.util.Util;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Value("${security.jwt.access-token-duration}")
    private Long tokenDuration;

    @Value("${security.verification.code-validity}")
    private Long verificationCodeValidity;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TokenRep refreshTokenRep;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private CoinService coinService;

    @PostMapping("/check")
    public Response check(@RequestBody CheckDTO req) {
        try {
            CheckResponseDTO res = new CheckResponseDTO();
            User user = userService.findByPhone(req.getPhone());

            res.setPhoneExist(user != null);
            res.setPasswordMatch(user != null && passwordEncoder.matches(req.getPassword(), user.getPassword()));

            return Response.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/verify")
    public Response verify(@RequestBody VerificationDTO req) {
        try {
            VerificationResponseDTO res = new VerificationResponseDTO();
            String code = twilioService.sendVerificationCode(req.getPhone());

            if (StringUtils.isBlank(code)) {
                return Response.defaultError("Phone country is not supported");
            }

            res.setCode(code);

            return Response.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/register")
    public Response register(@RequestBody AuthenticationDTO req) {
        try {
            if (req.getCoins().isEmpty()) {
                return Response.defaultError("Empty coin list");
            }

            User existingUser = userService.findByPhone(req.getPhone());

            if (existingUser != null) {
                return Response.error(3, "Phone is already registered");
            }

            User user = userService.register(req.getPhone(), req.getPassword(), req.getPlatform(), req.getCoins());

            TokenDTO jwt = getJwt(user.getId(), user.getIdentity().getId(), req.getPhone(), req.getPassword());

            Token token = new Token();
            token.setRefreshToken(jwt.getRefreshToken());
            token.setAccessToken(jwt.getAccessToken());
            token.setUser(user);

            refreshTokenRep.save(token);

            List<String> coins = req.getCoins().stream().map(e -> e.getCode()).collect(Collectors.toList());

            jwt.setBalance(coinService.getCoinsBalance(user.getId(), coins));

            return Response.ok(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/recover")
    public Response recover(@RequestBody AuthenticationDTO req) {
        try {
            if (req.getCoins().isEmpty()) {
                return Response.defaultError("Empty coin list");
            }

            User user = userService.findByPhone(req.getPhone());
            if (user == null) {
                return Response.error(3, "Phone doesn't exist");
            }

            boolean isPasswordMatch = passwordEncoder.matches(req.getPassword(), user.getPassword());
            if (!isPasswordMatch) {
                return Response.error(4, "Incorrect password");
            }

            boolean isCoinAddressesMatch = coinService.isCoinsAddressMatch(user, req.getCoins());
            if (!isCoinAddressesMatch) {
                return Response.error(5, "Coins address doesn't match");
            }

            TokenDTO jwt = getJwt(user.getId(), user.getIdentity().getId(), req.getPhone(), req.getPassword());

            Token token = refreshTokenRep.findByUserId(user.getId());
            token.setRefreshToken(jwt.getRefreshToken());
            token.setAccessToken(jwt.getAccessToken());
            refreshTokenRep.save(token);

            user.setPlatform(req.getPlatform());
            userService.save(user);

            List<String> coins = req.getCoins().stream().map(e -> e.getCode()).collect(Collectors.toList());

            jwt.setBalance(coinService.getCoinsBalance(user.getId(), coins));

            return Response.ok(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/refresh")
    public Response refresh(@RequestBody RefreshDTO dto) {
        try {
            Token refreshToken = refreshTokenRep.findByRefreshToken(dto.getRefreshToken());

            if (refreshToken != null) {
                TokenDTO jwt = getJwt(refreshToken.getUser());

                Token token = refreshTokenRep.findByUserId(refreshToken.getUser().getId());
                token.setRefreshToken(jwt.getRefreshToken());
                token.setAccessToken(jwt.getAccessToken());
                refreshTokenRep.save(token);

                return Response.ok(jwt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }

        throw new AccessDeniedException("Refresh token not exist");
    }

    @GetMapping("/user/{userId}/unlink")
    public Response unlink(@PathVariable Long userId) {
        try {
            Unlink unlink = userService.unlinkUser(userId);

            return Response.ok(unlink != null);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/code/send")
    public Response sendCode(@PathVariable Long userId) {
        try {
            twilioService.sendVerificationCode(userService.findById(userId));

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/code/verify")
    public Response verify(@RequestBody ValidateDTO validateOtpVM, @PathVariable Long userId) {
        try {
            CodeVerify codeVerify = userService.getCodeByUserId(userId);
            Long timestamp = System.currentTimeMillis() - verificationCodeValidity;

            if (!StringUtils.isEmpty(codeVerify.getCode())
                    && codeVerify.getUpdateDate().getTime() < timestamp) {
                return Response.defaultError("Code is expired");
            }

            if (codeVerify.getStatus() == 1) {
                return Response.error(3, "Code is already used");
            }

            if (!StringUtils.equals(validateOtpVM.getCode(), codeVerify.getCode())) {
                return Response.error(4, "Wrong code");
            }

            codeVerify.setStatus(1);
            userService.save(codeVerify);

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/phone")
    public Response getPhone(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);

            return Response.ok("phone", user.getPhone());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/phone/update")
    public Response updatePhone(@PathVariable Long userId, @RequestBody PhoneDTO phoneRequest) {
        try {
            Boolean isPhoneExist = userService.isPhoneExist(phoneRequest.getPhone(), userId);

            if (isPhoneExist) {
                return Response.defaultError("Phone is already registered");
            }

            userService.updatePhone(phoneRequest, userId);

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/phone/verify")
    public Response confirmPhone(@RequestBody ValidateDTO validateOtpVM, @PathVariable Long userId) {
        try {
            PhoneChange phoneChange = userService.getUpdatePhone(userId);
            phoneChange = (PhoneChange) Hibernate.unproxy(phoneChange);

            if (phoneChange.getStatus() == null || phoneChange.getStatus().intValue() == 1) {
                return Response.defaultError("Invalid request");
            }

            CodeVerify codeVerify = userService.getCodeByUserId(userId);

            if (codeVerify.getStatus() == 1) {
                return Response.error(3, "Verification code is already used");
            }

            if (!StringUtils.equals(validateOtpVM.getCode(), codeVerify.getCode())) {
                return Response.error(4, "Wrong verification code");
            }

            userService.updatePhone(phoneChange.getPhone(), userId);

            phoneChange.setStatus(1);
            userService.save(phoneChange);

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/password/update")
    public Response updatePassword(@RequestBody ChangePasswordDTO changePasswordRequest, @PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            Boolean match = passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword());

            if (!match) {
                return Response.defaultError("Old password does not match");
            }

            String encodedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
            userService.updatePassword(encodedPassword, userId);

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/password/verify")
    public Response checkPassword(@RequestBody CheckPasswordDTO checkPasswordRequest, @PathVariable Long userId) {
        try {
            Boolean match = Boolean.FALSE;
            User user = userService.findById(userId);

            if (user != null) {
                match = passwordEncoder.matches(checkPasswordRequest.getPassword(), user.getPassword());
            }

            return Response.ok(match);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/kyc")
    public Response getKycState(@PathVariable Long userId) {
        try {
            VerificationStateDTO verificationStateDTO = userService.getVerificationState(userId);
            return Response.ok(verificationStateDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/kyc")
    public Response submitKyc(@PathVariable Long userId, @Valid @RequestBody @ModelAttribute UserVerificationDTO verificationData) {
        try {
            userService.submitVerification(userId, verificationData);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/location")
    public Response updateLocation(@PathVariable Long userId, @RequestBody LocationDTO dto) {
        try {
            return Response.ok(userService.updateLocation(userId, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/limits")
    public Response getLimits(@PathVariable Long userId) {
        try {
            return Response.ok(transactionService.getLimits(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    private TokenDTO getJwt(Long userId, Long identityId, String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication);
        String refreshToken = Util.createRefreshToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(Constant.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new TokenDTO(userId, identityId, jwt, System.currentTimeMillis() + tokenDuration, refreshToken,
                authentication.getAuthorities().stream().map(role -> role.getAuthority()).collect(Collectors.toList()));
    }

    private TokenDTO getJwt(User user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getPhone(), new String(Base64.decodeBase64(user.getPassword())));

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
        String jwt = tokenProvider.createToken(authentication);
        String refreshToken = Util.createRefreshToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(Constant.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new TokenDTO(user.getId(), user.getIdentity().getId(), jwt, System.currentTimeMillis() + tokenDuration, refreshToken,
                authentication.getAuthorities().stream().map(role -> role.getAuthority()).collect(Collectors.toList()));
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BindException.class)
    public Response handleValidationExceptions(BindException ex) {
        FieldError fieldError = (FieldError) ex.getBindingResult().getAllErrors().get(0);
        String errorMessage = fieldError.getDefaultMessage();

        return Response.defaultError(errorMessage);
    }
}