package com.batm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.UserCoin;

public interface UserCoinRepository extends JpaRepository<UserCoin, Long> {

    List<UserCoin> findByUserId(Long userId);
}