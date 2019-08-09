package com.batm.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.batm.entity.Unlink;
import com.batm.entity.User;
import com.batm.repository.UnlinkRepository;
import com.batm.repository.UserRepository;

@Service
public class UnlinkService {

	@Autowired
	private UnlinkRepository unlinkRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	public Unlink unlinkUser(Long userId) {
		User user = this.userRepository.getOne(userId);
		if(user != null) {
			Unlink unlink = this.unlinkRepository.findByUserUserId(userId);
			if(unlink == null) {
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
	
}
