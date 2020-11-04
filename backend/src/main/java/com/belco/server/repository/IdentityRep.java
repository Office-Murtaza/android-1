package com.belco.server.repository;

import com.belco.server.entity.Identity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityRep extends JpaRepository<Identity, Long> {}