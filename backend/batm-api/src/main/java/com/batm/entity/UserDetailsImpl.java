package com.batm.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Arrays;
import java.util.Collection;


public class UserDetailsImpl extends User implements UserDetails {

    public UserDetailsImpl(User user) {
        super(user.getUserId(), user.getPhone(), user.getPassword(), user.getRole(), user.getCreateDate(), user.getUpdateDate());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_" + super.getRole()));
    }

    @Override
    public String getUsername() {
        return super.getPhone();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}