package com.batm.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import com.batm.dto.GiftAddressDTO;
import com.batm.entity.*;
import com.batm.repository.*;
import com.batm.dto.PhoneDTO;
import com.batm.util.Constant;
import com.batm.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UnlinkRepository unlinkRepository;

    @Autowired
    private UpdatePhoneRepository updatePhoneRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CodeVerificationRepository codeValidatorRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private UserCoinRepository userCoinRepository;

    @Autowired
    private LimitRepository limitRepository;

    @Transactional
    public User register(String phone, String password) {
        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        User savedUser = userRepository.save(user);

        Limit dailyLimit = new Limit();
        dailyLimit.setAmount(Constant.DAILY_LIMIT);
        dailyLimit.setCurrency("USD");
        Limit savedDailyLimit = limitRepository.save(dailyLimit);

        Limit trxLimit = new Limit();
        trxLimit.setAmount(Constant.TX_LIMIT);
        trxLimit.setCurrency("USD");
        Limit savedTrxLimit = limitRepository.save(trxLimit);

        Date date = new Date();
        Identity identity = new Identity();
        identity.setPublicId(Util.generatePublicId());
        identity.setState(0);
        identity.setUser(savedUser);
        identity.setCreated(date);
        identity.setLastUpdatedAt(date);
        identity.setRegistered(date);
        identity.setLimitCashPerDay(Arrays.asList(savedDailyLimit));
        identity.setLimitCashPerTransaction(Arrays.asList(savedTrxLimit));
        Identity savedIdentity = identityRepository.save(identity);

        user.setIdentity(savedIdentity);

        return user;
    }

    public User findById(Long userId) {
        return this.userRepository.getOne(userId);
    }

    public void updatePassword(String encodedPassword, Long userId) {
        this.userRepository.updatePassword(encodedPassword, userId);
    }

    public void updatePhone(String phone, Long userId) {
        this.userRepository.updatePhone(phone, userId);
    }

    public Boolean isPhoneExist(String phone, Long userId) {
        User user = this.userRepository.isPhoneExist(phone, userId);
        return user != null ? true : false;
    }

    public Unlink unlinkUser(Long userId) {
        User user = this.userRepository.getOne(userId);
        if (user != null) {
            Unlink unlink = this.unlinkRepository.findByUserUserId(userId);
            if (unlink == null) {
                unlink = new Unlink();
                unlink.setUser(user);
                unlink.setCreatedDate(Instant.now());
            }

            unlink.setLastModifiedDate(Instant.now());
            unlinkRepository.save(unlink);
            return unlink;
        }
        return null;
    }

    public UpdatePhone updatePhone(PhoneDTO phoneRequest, Long userId) {
        User user = userRepository.getOne(userId);
        UpdatePhone updatePhone = user.getUpdatePhone();
        if (updatePhone == null || updatePhone.getId() == null) {
            updatePhone = new UpdatePhone();
        }
        updatePhone.setUser(user);
        updatePhone.setPhone(phoneRequest.getPhone());
        updatePhone.setStatus(0);
        updatePhone.setCreatedDate(Instant.now());
        updatePhone.setLastModifiedDate(Instant.now());

        updatePhoneRepository.save(updatePhone);

        messageService.sendVerificationCode(user);
        return updatePhone;
    }

    @Transactional
    public UpdatePhone getUpdatePhone(Long userId) {
        return updatePhoneRepository.getOne(userId);
    }

    public UpdatePhone save(UpdatePhone updatePhone) {
        return updatePhoneRepository.save(updatePhone);
    }

    public List<UserCoin> save(List<UserCoin> list) {
        return userCoinRepository.saveAll(list);
    }

    public CodeVerification getCodeByUserId(Long userId) {
        return codeValidatorRepository.findByUserUserId(userId);
    }

    public UserCoin getUserCoin(Long userId, String coin) {
        return userCoinRepository.findByUserUserIdAndCoinId(userId, coin);
    }

    public List<UserCoin> getUserCoins(Long userId) {
        return userCoinRepository.findByUserUserId(userId);
    }

    public void save(CodeVerification codeVerification) {
        codeValidatorRepository.save(codeVerification);
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findOneByPhoneIgnoreCase(phone);
    }

    public GiftAddressDTO getUserGiftAddress(CoinService.CoinEnum coinId, String phone) {
        Optional<User> user = findByPhone(phone);

        if (user.isPresent()) {
            String address = user.get().getUserCoins().stream()
                    .filter(k -> k.getCoinId().equalsIgnoreCase(coinId.name()))
                    .findFirst().get()
                    .getPublicKey();

            return new GiftAddressDTO(address);
        } else {
            return new GiftAddressDTO(coinId.getWalletAddress());
        }
    }
}