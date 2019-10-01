package com.batm.service;

import java.time.Instant;
import java.util.Optional;

import com.batm.dto.GiftAddressDTO;
import com.batm.entity.Unlink;
import com.batm.repository.UnlinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.batm.entity.User;
import com.batm.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UnlinkRepository unlinkRepository;

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
