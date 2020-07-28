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
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
    public Response check(@RequestBody CheckDTO dto) {
        try {
            CheckResponseDTO res = new CheckResponseDTO();
            User user = userService.findByPhone(dto.getPhone());

            res.setPhoneExist(user != null);
            res.setPasswordMatch(user != null && passwordEncoder.matches(dto.getPassword(), user.getPassword()));

            return Response.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/verify")
    public Response verify(@RequestBody VerificationDTO dto) {
        try {
            VerificationResponseDTO res = new VerificationResponseDTO();
            String code = twilioService.sendVerificationCode(dto.getPhone());

            if (StringUtils.isBlank(code)) {
                return Response.defaultError("Phone country is not supported");
            }

            res.setCode(code);

            log.info("phone: " + dto.getPhone() + ", code: " + code);

            return Response.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/register")
    public Response register(@RequestBody AuthenticationDTO dto) {
        try {
            if (dto.getCoins().isEmpty()) {
                return Response.defaultError("Empty coin list");
            }

            if (dto.getCoins().size() != 8) {
                return Response.defaultError("Some coin is missed");
            }

            User existingUser = userService.findByPhone(dto.getPhone());

            if (existingUser != null) {
                return Response.error(3, "Phone is already registered");
            }

            User user = userService.register(dto.getPhone(), dto.getPassword(), dto.getPlatform(), dto.getCoins());

            TokenDTO jwt = getJwt(user.getId(), user.getIdentity().getId(), dto.getPhone(), dto.getPassword());

            Token token = new Token();
            token.setRefreshToken(jwt.getRefreshToken());
            token.setAccessToken(jwt.getAccessToken());
            token.setUser(user);

            refreshTokenRep.save(token);

            List<String> coins = dto.getCoins().stream().map(e -> e.getCode()).collect(Collectors.toList());

            jwt.setBalance(coinService.getCoinsBalance(user.getId(), coins));

            return Response.ok(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/recover")
    public Response recover(@RequestBody AuthenticationDTO dto) {
        try {
            if (dto.getCoins().isEmpty()) {
                return Response.defaultError("Empty coin list");
            }

            if (dto.getCoins().size() != 8) {
                return Response.defaultError("Some coin is missed");
            }

            User user = userService.findByPhone(dto.getPhone());
            if (user == null) {
                return Response.error(3, "Phone doesn't exist");
            }

            boolean isPasswordMatch = passwordEncoder.matches(dto.getPassword(), user.getPassword());
            if (!isPasswordMatch) {
                return Response.error(4, "Incorrect password");
            }

            boolean isCoinAddressesMatch = coinService.isCoinsAddressMatch(user, dto.getCoins());
            if (!isCoinAddressesMatch) {
                return Response.error(5, "Coins address doesn't match");
            }

            TokenDTO jwt = getJwt(user.getId(), user.getIdentity().getId(), dto.getPhone(), dto.getPassword());

            Token token = refreshTokenRep.findByUserId(user.getId());
            token.setRefreshToken(jwt.getRefreshToken());
            token.setAccessToken(jwt.getAccessToken());
            refreshTokenRep.save(token);

            user.setPlatform(dto.getPlatform());
            userService.save(user);

            List<String> coins = dto.getCoins().stream().map(e -> e.getCode()).collect(Collectors.toList());

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

        throw new AccessDeniedException("Refresh token doesn't exist");
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

    @GetMapping("/user/{userId}/phone")
    public Response getPhone(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);

            return Response.ok(new PhoneDTO(user.getPhone()));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/phone")
    public Response updatePhone(@PathVariable Long userId, @RequestBody PhoneDTO dto) {
        try {
            Boolean isPhoneExist = userService.isPhoneExist(userId, dto.getPhone());

            if (isPhoneExist) {
                return Response.defaultError("Phone is already registered");
            } else {
                userService.updatePhone(userId, dto.getPhone());
            }

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/password")
    public Response updatePassword(@PathVariable Long userId, @RequestBody ChangePasswordDTO dto) {
        try {
            User user = userService.findById(userId);
            Boolean isMatch = passwordEncoder.matches(dto.getOldPassword(), user.getPassword());

            if (!isMatch) {
                return Response.defaultError("Old password doesn't match");
            } else {
                userService.updatePassword(userId, passwordEncoder.encode(dto.getNewPassword()));
            }

            return Response.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/kyc-details")
    public Response getKycDetails(@PathVariable Long userId) {
        try {
            return Response.ok(userService.getKycDetails(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/kyc-submit")
    public Response submitKyc(@PathVariable Long userId, @RequestBody @ModelAttribute SubmitKycDTO dto) {
        try {
            return Response.ok(userService.submitKyc(userId, dto));
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
}