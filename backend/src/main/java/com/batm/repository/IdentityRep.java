package com.batm.repository;

import com.batm.entity.Identity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityRep extends JpaRepository<Identity, Long> {}