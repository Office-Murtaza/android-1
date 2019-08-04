package com.batm.service;

import java.time.Instant;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.batm.entity.UpdatePhone;
import com.batm.entity.User;
import com.batm.repository.UpdatePhoneRepository;
import com.batm.repository.UserRepository;
import com.batm.rest.vm.PhoneRequestVM;
import com.batm.util.TwilioComponent;

@Service
public class PhoneService {

    @Autowired
    private UpdatePhoneRepository updatePhoneRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwilioComponent twilioComponent;

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

        twilioComponent.sendOTP(user);
        return updatePhone;
    }

    @Transactional
    public UpdatePhone getUpdatePhone(Long userId) {
        return updatePhoneRepository.getOne(userId);
    }

    public UpdatePhone save(UpdatePhone updatePhone) {
        return updatePhoneRepository.save(updatePhone);
    }
}