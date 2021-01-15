package com.belco.server.repository;

import com.belco.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRep extends JpaRepository<User, Long> {

    Optional<User> findOneByPhone(String phone);
}