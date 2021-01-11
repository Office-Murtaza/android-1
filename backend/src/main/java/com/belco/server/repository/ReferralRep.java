package com.belco.server.repository;

import com.belco.server.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRep extends JpaRepository<Referral, Long> {}