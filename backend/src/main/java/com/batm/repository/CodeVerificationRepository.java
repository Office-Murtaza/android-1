package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.CodeVerification;

public interface CodeVerificationRepository extends JpaRepository<CodeVerification, Long> {

    CodeVerification findByUserId(Long userId);
}