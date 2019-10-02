package com.batm.service;

import java.time.Instant;
import java.util.Optional;

import com.batm.entity.CodeVerification;
import com.batm.entity.Unlink;
import com.batm.entity.UpdatePhone;
import com.batm.repository.CodeVerificationRepository;
import com.batm.repository.UnlinkRepository;
import com.batm.repository.UpdatePhoneRepository;
import com.batm.rest.vm.PhoneRequestVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.batm.entity.User;
import com.batm.repository.UserRepository;

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

    public User registerUser(String phone, String password) {

        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setPhone(phone);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setRole("ROLE_USER");
        userRepository.save(newUser);
        return newUser;
    }

    public Optional<User> findOneByPhoneIgnoreCase(String phone) {
        // TODO Auto-generated method stub
        return this.userRepository.findOneByPhoneIgnoreCase(phone);
    }

    public User findById(Long userId) {
        return this.userRepository.getOne(userId);
    }

    public User save(User user) {
        return this.userRepository.save(user);
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
        return this.codeValidatorRepository.findByUserUserId(userId);
    }

    public void save(CodeVerification codeVerification) {
        this.codeValidatorRepository.save(codeVerification);
    }
}
