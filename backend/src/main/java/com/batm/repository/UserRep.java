package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.batm.entity.User;

public interface UserRep extends JpaRepository<User, Long> {

    User findOneByPhone(String phone);

    @Transactional
    @Modifying
    @Query("UPDATE User user SET user.password =:password WHERE user.id = :userId")
    void updatePassword(@Param("password") String password, @Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE User user SET user.phone =:phone WHERE user.id = :userId")
    void updatePhone(@Param("phone") String phone, @Param("userId") Long userId);

    @Query("SELECT user FROM User user WHERE user.phone =:phone AND user.id <> :userId")
    User isPhoneExist(@Param("phone") String phone, @Param("userId") Long userId);
}