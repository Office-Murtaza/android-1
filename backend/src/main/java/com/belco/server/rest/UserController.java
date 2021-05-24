package com.belco.server.rest;

import com.belco.server.dto.*;
import com.belco.server.entity.Token;
import com.belco.server.entity.User;
import com.belco.server.model.Response;
import com.belco.server.repository.TokenRep;
import com.belco.server.security.JWTTokenProvider;
import com.belco.server.service.*;
import com.belco.server.util.Constant;
import com.belco.server.util.Util;
import com.mongodb.client.model.UpdateOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private static final String COLL_PHONE_VERIFY_TRACKER = "phone_verify_tracker";

    private final JWTTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TransactionService transactionService;
    private final TokenRep refreshTokenRep;
    private final PasswordEncoder passwordEncoder;
    private final TwilioService twilioService;
    private final CoinService coinService;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final MongoTemplate mongo;

    @Value("${security.jwt.access-token-duration}")
    private Long tokenDuration;

    @Value("${twilio.verify-delay}")
    private Long verifyDelay;

    public UserController(JWTTokenProvider tokenProvider, AuthenticationManager authenticationManager, UserService userService, TransactionService transactionService, TokenRep refreshTokenRep, PasswordEncoder passwordEncoder, TwilioService twilioService, CoinService coinService, WalletService walletService, NotificationService notificationService, MongoTemplate mongo) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.transactionService = transactionService;
        this.refreshTokenRep = refreshTokenRep;
        this.passwordEncoder = passwordEncoder;
        this.twilioService = twilioService;
        this.coinService = coinService;
        this.walletService = walletService;
        this.notificationService = notificationService;
        this.mongo = mongo;
    }

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
    public Response verify(@RequestBody PhoneDTO dto) {
        try {
            Document d = mongo.getCollection(COLL_PHONE_VERIFY_TRACKER).find(new Document("phone", dto.getPhone())).first();
            long time = System.currentTimeMillis() - verifyDelay;

            if (d == null || d.getLong("timestamp") < time) {
                String code = twilioService.sendVerificationCode(dto.getPhone());

                if (StringUtils.isBlank(code)) {
                    return Response.validationError("Not supported phone number");
                }

                log.info("phone: " + dto.getPhone() + ", code: " + code);

                mongo.getCollection(COLL_PHONE_VERIFY_TRACKER).updateOne(new Document("phone", dto.getPhone()), new Document("$set", new Document("phone", dto.getPhone()).append("timestamp", System.currentTimeMillis())), new UpdateOptions().upsert(true));

                return Response.ok("code", code);
            } else {
                return Response.validationError("Too many requests. Please, wait " + (d.getLong("timestamp") - time) / 1000 + " seconds");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @PostMapping("/register")
    public Response register(@RequestBody AuthenticationDTO dto) {
        try {
            if (userService.findByPhone(dto.getPhone()).isPresent()) {
                return Response.validationError("Phone is already used");
            }

            if (StringUtils.isNotBlank(dto.getUsername()) && userService.findByUsername(dto.getUsername()).isPresent()) {
                return Response.validationError("Username is already used");
            }

            User user = userService.register(dto);
            TokenDTO jwt = getJwt(user.getId(), user.getIdentity().getId(), dto.getPhone(), dto.getPassword());

            if (dto.getCoins().isEmpty()) {
                WalletDetailsDTO walletDetails = walletService.generateNewWallet();
                dto.setCoins(new ArrayList<>(walletDetails.getCoins().values()));
                jwt.setSeedEncrypted(walletService.encrypt(walletDetails.getWallet().mnemonic()));
            }

            coinService.addUserCoins(user, dto.getCoins());
            transactionService.deliverPendingTransfers(dto.getPhone());

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
            Optional<User> userOpt = userService.findByPhone(dto.getPhone());
            if (!userOpt.isPresent()) {
                return Response.validationError("Phone doesn't exist");
            }

            User user = userOpt.get();

            boolean isPasswordMatch = passwordEncoder.matches(dto.getPassword(), user.getPassword());
            if (!isPasswordMatch) {
                return Response.validationError("Wrong password");
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

            user = userService.save(user);

            if (!dto.getCoins().isEmpty()) {
                coinService.addUserCoins(user, dto.getCoins());
            }

            List<String> coins = user.getUserCoins().stream().map(e -> e.getCoin().getCode()).collect(Collectors.toList());

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
                TokenDTO jwt = getJwt(refreshToken.getUser().getId(), refreshToken.getUser().getIdentity().getId(), refreshToken.getUser().getPhone(), new String(Base64.decodeBase64(refreshToken.getUser().getPassword())));

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

    private TokenDTO getJwt(Long userId, Long identityId, String phone, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(phone, password);
        String firebaseToken = notificationService.getFirebaseToken(phone);

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication);
        String refreshToken = Util.createRefreshToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(Constant.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new TokenDTO(userId, identityId, jwt, System.currentTimeMillis() + tokenDuration, refreshToken, firebaseToken, authentication.getAuthorities().stream().map(role -> role.getAuthority()).collect(Collectors.toList()), null, null);
    }
}