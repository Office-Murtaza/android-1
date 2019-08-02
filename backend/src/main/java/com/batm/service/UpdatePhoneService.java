package com.batm.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.batm.entity.UpdatePhone;
import com.batm.entity.User;
import com.batm.repository.UpdatePhoneRepository;
import com.batm.repository.UserRepository;
import com.batm.rest.vm.PhoneRequestVM;
import com.batm.util.TwilioComponent;

@Service
public class UpdatePhoneService {

	@Autowired
	private UpdatePhoneRepository updatePhoneRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TwilioComponent twilioComponent;

	@Transactional
	public UpdatePhone udpatePhone(PhoneRequestVM phoneRequest, Long userId) {
		User user = this.userRepository.getOne(userId);
		UpdatePhone updatePhone = updatePhoneRepository.getOne(userId);
		if(updatePhone == null) {
			updatePhone = new UpdatePhone();
		}
		updatePhone.setUser(user);
		updatePhone.setPhone(phoneRequest.getPhone());
		updatePhone.setStatus("0");
		updatePhone.setCreatedDate(Instant.now());
		updatePhone.setLastModifiedDate(Instant.now());
		
		updatePhoneRepository.save(updatePhone);
		
		twilioComponent.sendOTP(user);
		return updatePhone;
	}
	
	public UpdatePhone getUpdatePhone(Long userId) {
		return updatePhoneRepository.getOne(userId);
	}
	
	public UpdatePhone save(UpdatePhone updatePhone) {
		return updatePhoneRepository.save(updatePhone);
	}

}
