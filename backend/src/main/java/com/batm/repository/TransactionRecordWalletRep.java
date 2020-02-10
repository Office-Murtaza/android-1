package com.batm.repository;

import com.batm.entity.TransactionRecordWallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRecordWalletRep extends JpaRepository<TransactionRecordWallet, Long> {}