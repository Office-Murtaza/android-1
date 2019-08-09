package com.batm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.batm.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findOneByPhoneIgnoreCase(String phone);

	@Transactional
	@Modifying
	@Query("update User user set user.password =:password where user.userId =:userId")
	void updatePassword(@Param("password") String password, @Param("userId") Long userId);
	
	@Transactional
	@Modifying
	@Query("update User user set user.phone =:phone where user.userId =:userId")
	void updatePhone(@Param("phone") String phone, @Param("userId") Long userId);
	
	@Query("select from User user where user.phone =:phone where user.userId !=:userId")
	User isPhoneExist(@Param("phone") String phone, @Param("userId") Long userId);
}
