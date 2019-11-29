package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.PhoneChange;

public interface PhoneChangeRep extends JpaRepository<PhoneChange, Long> {

    PhoneChange findByUserId(Long userId);
}