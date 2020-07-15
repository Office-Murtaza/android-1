package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.User;

public interface UserRep extends JpaRepository<User, Long> {

    User findOneByPhone(String phone);
}