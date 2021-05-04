package com.belco.server.repository;

import com.belco.server.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRep extends JpaRepository<Wallet, Long> {}