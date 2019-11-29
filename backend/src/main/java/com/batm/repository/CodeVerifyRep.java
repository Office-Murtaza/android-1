package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.CodeVerify;

public interface CodeVerifyRep extends JpaRepository<CodeVerify, Long> {

    CodeVerify findByUserId(Long userId);
}