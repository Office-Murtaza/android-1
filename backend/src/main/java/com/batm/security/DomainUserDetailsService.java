package com.batm.security;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.batm.entity.User;
import com.batm.repository.UserRepository;

@Component("userDetailsService")
@AllArgsConstructor
public class DomainUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        return userRepository.findOneByPhone(login)
                .map(user -> createSpringSecurityUser(user))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

        return new org.springframework.security.core.userdetails.User(user.getPassword(), user.getPassword(),
                authorities);
    }
}