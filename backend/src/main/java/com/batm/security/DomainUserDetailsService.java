package com.batm.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.batm.entity.User;
import com.batm.repository.UserRepository;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

	private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

	private final UserRepository userRepository;

	public DomainUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(final String login) {
		log.debug("Authenticating {}", login);

		return userRepository.findOneByPhoneIgnoreCase(login).map(user -> createSpringSecurityUser(login, user))
				.orElseThrow(() -> new UsernameNotFoundException(
						"User with email " + login + " was not found in the database"));

	}

	private org.springframework.security.core.userdetails.User createSpringSecurityUser(String lowercaseLogin,
			User user) {
		Set<GrantedAuthority> authorities = new HashSet<>();
		authorities.add(new SimpleGrantedAuthority(user.getRole()));
		return new org.springframework.security.core.userdetails.User(user.getPassword(), user.getPassword(),
				authorities);
	}
}
