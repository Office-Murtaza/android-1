package com.batm.repository;

import java.util.List;

import com.batm.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.UserCoin;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCoinRep extends JpaRepository<UserCoin, Long> {

    List<UserCoin> findByUserId(Long userId);

    @Query("SELECT uc FROM UserCoin uc WHERE uc.user.id = :userId AND uc.coin.code = :coinCode ")
    UserCoin findByUserIdAndCoinCode(@Param("userId") Long userId, @Param("coinCode") String coinCode);

    List<UserCoin> findAllByCoin(Coin coin);
}