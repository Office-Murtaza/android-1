package com.belco.server.rest;

import com.belco.server.dto.*;
import com.belco.server.entity.Token;
import com.belco.server.entity.User;
import com.belco.server.model.Response;
import com.belco.server.repository.TokenRep;
import com.belco.server.security.JWTTokenProvider;
import com.belco.server.service.CoinService;
import com.belco.server.service.TransactionService;
import com.belco.server.service.TwilioService;
import com.belco.server.service.UserService;
import com.belco.server.util.Constant;
import com.belco.server.util.Util;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Value("${security.jwt.access-token-duration}")
    private Long tokenDuration;

    @Autowired
    private JWTTokenProvider tokenProvider;

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
            Optional<User> userOpt = userService.findByPhone(dto.getPhone());

            res.setPhoneExist(userOpt.isPresent());
            res.setPasswordMatch(userOpt.isPresent() && passwordEncoder.matches(dto.getPassword(), userOpt.get().getPassword()));

            return Response.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/verify")
    public Response verify(@RequestBody VerificationDTO dto) {
        try {
            String code = twilioService.sendVerificationCode(dto.getPhone());

            if (StringUtils.isBlank(code)) {
                return Response.validationError("Not supported phone number");
            }

            log.info("phone: " + dto.getPhone() + ", code: " + code);

            return Response.ok("code", code);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/register")
    public Response register(@RequestBody AuthenticationDTO dto) {
        try {
            if (dto.getCoins().isEmpty()) {
                return Response.validationError("Empty coin list");
            }

            Optional<User> userOpt = userService.findByPhone(dto.getPhone());

            if (userOpt.isPresent()) {
                return Response.error(4, "Phone is already used");
            }

            User user = userService.register(dto);

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
                return Response.validationError("Empty coin list");
            }

            Optional<User> userOpt = userService.findByPhone(dto.getPhone());
            if (!userOpt.isPresent()) {
                return Response.error(4, "Phone doesn't exist");
            }

            User user = userOpt.get();

            boolean isPasswordMatch = passwordEncoder.matches(dto.getPassword(), user.getPassword());
            if (!isPasswordMatch) {
                return Response.error(5, "Wrong password");
            }

            boolean isCoinAddressesMatch = coinService.isCoinsAddressMatch(user, dto.getCoins());
            if (!isCoinAddressesMatch) {
                return Response.error(6, "Seed phrase you entered is invalid. Please try again");
            }

            TokenDTO jwt = getJwt(user.getId(), user.getIdentity().getId(), dto.getPhone(), dto.getPassword());

            Token token = refreshTokenRep.findByUserId(user.getId());
            token.setRefreshToken(jwt.getRefreshToken());
            token.setAccessToken(jwt.getAccessToken());
            refreshTokenRep.save(token);

            user.setPlatform(dto.getPlatform());
            user.setDeviceModel(dto.getDeviceModel());
            user.setDeviceOS(dto.getDeviceOS());
            user.setAppVersion(dto.getAppVersion());
            user.setLatitude(dto.getLatitude());
            user.setLongitude(dto.getLongitude());
            user.setTimezone(dto.getTimezone());
            user.setNotificationsToken(dto.getNotificationsToken());

            if (user.getReferral() == null) {
                userService.addUserReferral(user);
            }

            userService.save(user);
            coinService.addUserCoins(user, dto.getCoins());

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
            return Response.ok(userService.unlinkUser(userId));
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
                return Response.validationError("Phone is already used");
            } else {
                return Response.ok(userService.updatePhone(userId, dto.getPhone()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/phone-verify")
    public Response verifyPhone(@PathVariable Long userId, @RequestBody PhoneDTO dto) {
        try {
            return Response.ok(userService.isPhoneExist(userId, dto.getPhone()));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/password")
    public Response updatePassword(@PathVariable Long userId, @RequestBody PasswordDTO dto) {
        try {
            User user = userService.findById(userId);

            if (StringUtils.isNotBlank(dto.getOldPassword())) {
                Boolean isMatch = passwordEncoder.matches(dto.getOldPassword(), user.getPassword());

                if (!isMatch) {
                    return Response.validationError("Wrong password");
                }
            }

            return Response.ok(userService.updatePassword(userId, passwordEncoder.encode(dto.getNewPassword())));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/password-verify")
    public Response verifyPassword(@PathVariable Long userId, @RequestBody PasswordDTO dto) {
        try {
            User user = userService.findById(userId);

            return Response.ok(passwordEncoder.matches(dto.getPassword(), user.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @GetMapping("/user/{userId}/verification")
    public Response getVerificationDetails(@PathVariable Long userId) {
        try {
            return Response.ok(userService.getVerificationDetails(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/user/{userId}/verification")
    public Response submitVerification(@PathVariable Long userId, @RequestBody VerificationDTO dto) {
        try {
            return userService.submitVerification(userId, dto);
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

    @GetMapping("/user/{userId}/referral")
    public Response referral(@PathVariable Long userId) {
        try {
            return Response.ok(userService.findById(userId).getReferral().toDTO());
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
                authentication.getAuthorities().stream().map(role -> role.getAuthority()).collect(Collectors.toList()), null);
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
                authentication.getAuthorities().stream().map(role -> role.getAuthority()).collect(Collectors.toList()), null);
    }
}