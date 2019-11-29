package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.Unlink;

public interface UnlinkRep extends JpaRepository<Unlink, Long> {

    Unlink findByUserId(Long userId);
}