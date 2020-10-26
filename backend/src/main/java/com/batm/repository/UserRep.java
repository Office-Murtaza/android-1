package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.User;
import java.util.Optional;

public interface UserRep extends JpaRepository<User, Long> {

    Optional<User> findOneByPhone(String phone);
}