package com.batm.repository;

import com.batm.entity.TransactionRecordWallet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRecordWalletRep extends JpaRepository<TransactionRecordWallet, Long> {

    @Query(value = "SELECT * FROM w_transactionrecordwallet WHERE status = :status AND update_date > NOW() - INTERVAL :hoursAgo HOUR", nativeQuery = true)
    List<TransactionRecordWallet> findByStatusAndHoursAgo(@Param("status") Integer status, @Param("hoursAgo") Integer hours, Pageable page);
}