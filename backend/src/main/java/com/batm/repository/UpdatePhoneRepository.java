package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.UpdatePhone;

public interface UpdatePhoneRepository extends JpaRepository<UpdatePhone, Long> {

    UpdatePhone findByUserId(Long userId);
}