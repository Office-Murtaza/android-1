package com.batm.rest;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.Valid;
import com.batm.dto.AuthenticationDTO;
import com.batm.dto.TokenDTO;
import com.batm.service.MessageService;
import com.batm.util.Util;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
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
import com.batm.model.Error;
import com.batm.model.Response;
import com.batm.entity.Token;
import com.batm.entity.Unlink;
import com.batm.entity.UpdatePhone;
import com.batm.entity.User;
import com.batm.repository.TokenRepository;
import com.batm.dto.ChangePasswordDTO;
import com.batm.dto.CheckPasswordDTO;
import com.batm.dto.PhoneDTO;
import com.batm.dto.RefreshDTO;
import com.batm.dto.ValidateDTO;
import com.batm.security.TokenProvider;
import com.batm.service.UserService;
import com.batm.util.Constant;

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
    private TokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageService messageService;

    @PostMapping("/register")
    public Response register(@RequestBody AuthenticationDTO dto) {
        try {
            Pattern phonePattern = Pattern.compile(Constant.REGEX_PHONE);

            if (!phonePattern.matcher(dto.getPhone()).matches()) {
                return Response.serverError(2, "Invalid phone number");
            }

            if (!checkPasswordLength(dto.getPassword())) {
                return Response.serverError(3, "Password length should be from 6 to 15");
            }

            Optional<User> existingUser = userService.findByPhone(dto.getPhone());
            if (existingUser.isPresent()) {
                return Response.serverError(4, "Phone is already registered");
            }

            User user = userService.register(dto.getPhone(), dto.getPassword());
            messageService.sendVerificationCode(user);
            TokenDTO jwt = getJwt(user.getId(), user.getIdentity().getId(), dto.getPhone(), dto.getPassword());

            Token token = new Token();
            token.setRefreshToken(jwt.getRefreshToken());
            token.setAccessToken(jwt.getAccessToken());
            token.setUser(user);

            refreshTokenRepository.save(token);

            return Response.ok(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/recover")
    public Response recover(@RequestBody AuthenticationDTO dto) {
        try {
            Pattern phonePattern = Pattern.compile(Constant.REGEX_PHONE);

            if (!phonePattern.matcher(dto.getPhone()).matches()) {
                return Response.serverError(2, "Invalid phone number");
            }

            if (!checkPasswordLength(dto.getPassword())) {
                return Response.serverError(3, "Password length should be from 6 to 15");
            }

            Optional<User> existingUser = userService.findByPhone(dto.getPhone());
            if (!existingUser.isPresent()) {
                return Response.error(new Error(2, "Phone not found"));
            }

            User user = existingUser.get();

            boolean passwordMatch = passwordEncoder.matches(dto.getPassword(), user.getPassword());
            if (!passwordMatch) {
                return Response.error(new Error(3, "Wrong password"));
            }

            TokenDTO jwt = getJwt(user.getId(), user.getIdentity().getId(), dto.getPhone(), dto.getPassword());

            messageService.sendVerificationCode(user);

            Token token = refreshTokenRepository.findByUserId(user.getId());
            token.setRefreshToken(jwt.getRefreshToken());
            token.setAccessToken(jwt.getAccessToken());
            refreshTokenRepository.save(token);

            return Response.ok(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/refresh")
    public Response refresh(@Valid @RequestBody RefreshDTO refreshDTO) {
        try {
            Token refreshToken = refreshTokenRepository.findByRefreshToken(refreshDTO.getRefreshToken());

            if (refreshToken != null) {
                User user = userService.findById(refreshToken.getUser().getId());

                TokenDTO jwt = getJwt(user);

                Token token = refreshTokenRepository.findByUserId(user.getId());
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

    @GetMapping("/user/{userId}/code/send")
    public Response sendCode(@PathVariable Long userId) {
        try {
            messageService.sendVerificationCode(userService.findById(userId));

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/code/verify")
    public Response verify(@RequestBody ValidateDTO validateOtpVM, @PathVariable Long userId) {
        try {
            CodeVerification codeVerification = userService.getCodeByUserId(userId);
            Long timestamp = System.currentTimeMillis() - verificationCodeValidity;

            if (!StringUtils.isEmpty(codeVerification.getCode())
                    && codeVerification.getUpdateDate().getTime() < timestamp) {
                return Response.error(new Error(2, "Code is expired"));
            }

            if (codeVerification.getStatus() == 1) {
                return Response.error(new Error(3, "Code is already used"));
            }

            if (!StringUtils.equals(validateOtpVM.getCode(), codeVerification.getCode())) {
                return Response.error(new Error(4, "Wrong code"));
            }

            codeVerification.setStatus(1);
            userService.save(codeVerification);

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

            JSONObject res = new JSONObject();
            res.put("phone", user.getPhone());

            return Response.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/phone/update")
    public Response updatePhone(@RequestBody PhoneDTO phoneRequest, @PathVariable Long userId) {
        try {
            Boolean isPhoneExist = userService.isPhoneExist(phoneRequest.getPhone(), userId);

            if (isPhoneExist) {
                return Response.error(new Error(2, "Phone is already registered"));
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
            UpdatePhone updatePhone = userService.getUpdatePhone(userId);
            updatePhone = (UpdatePhone) Hibernate.unproxy(updatePhone);

            if (updatePhone.getStatus() == null || updatePhone.getStatus().intValue() == 1) {
                return Response.error(new Error(2, "Invalid request"));
            }

            CodeVerification codeVerification = userService.getCodeByUserId(userId);

            if (codeVerification.getStatus() == 1) {
                return Response.error(new Error(3, "Verification code is already used"));
            }

            if (!StringUtils.equals(validateOtpVM.getCode(), codeVerification.getCode())) {
                return Response.error(new Error(2, "Wrong verification code"));
            }

            userService.updatePhone(updatePhone.getPhone(), userId);
            updatePhone.setStatus(1);
            userService.save(updatePhone);

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
                return Response.error(new Error(2, "Old password does not match."));
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

    private static boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) && password.length() >= Constant.PASSWORD_MIN_LENGTH
                && password.length() <= Constant.PASSWORD_MAX_LENGTH;
    }
}