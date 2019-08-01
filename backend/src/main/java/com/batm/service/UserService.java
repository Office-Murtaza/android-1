package com.batm.service;

import java.util.Optional;

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

}
