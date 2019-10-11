package com.batm.service;

import java.time.Instant;
import java.util.Optional;
import com.batm.entity.*;
import com.batm.repository.*;
import com.batm.rest.vm.PhoneRequestVM;
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

//    @Autowired
//    private LimitRepository limitRepository;
//
//    @Autowired
//    private DailyLimitRepository dailyLimitRepository;

    @Transactional
    public User register(String phone, String password) {
        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        User savedUser = userRepository.save(user);

        Identity identity = new Identity();
        identity.setPublicId(Util.generatePublicId());
        identity.setState(0);
        identity.setUser(savedUser);
        Identity savedIdentity = identityRepository.save(identity);

//        Limit limit = new Limit();
//        limit.setAmount(BigDecimal.valueOf(10000));
//        limit.setCurrency("USD");
//        Limit savedLimit = limitRepository.save(limit);
//
//        DailyLimit dailyLimit = new DailyLimit();
//        dailyLimit.setIdentity(savedIdentity);
//        dailyLimit.setLimit(savedLimit);
//        dailyLimitRepository.save(dailyLimit);

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

    public UpdatePhone updatePhone(PhoneRequestVM phoneRequest, Long userId) {
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

    public CodeVerification getCodeByUserId(Long userId) {
        return codeValidatorRepository.findByUserUserId(userId);
    }

    public void save(CodeVerification codeVerification) {
        codeValidatorRepository.save(codeVerification);
    }

    public Optional<User> getUser(String phone) {
        return userRepository.findOneByPhoneIgnoreCase(phone);
    }
}