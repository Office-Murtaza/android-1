package com.batm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.batm.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findOneByPhoneIgnoreCase(String phone);

}
