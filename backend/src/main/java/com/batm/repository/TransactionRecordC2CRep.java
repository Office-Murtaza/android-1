package com.batm.repository;

import com.batm.entity.TransactionRecordC2C;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRecordC2CRep extends JpaRepository<TransactionRecordC2C, Long> {

    @Query(value = "SELECT * FROM w_transactionrecordc2c WHERE status = :status AND update_date > NOW() - INTERVAL :hoursAgo HOUR", nativeQuery = true)
    List<TransactionRecordC2C> findByStatusAndHoursAgo(@Param("status") Integer status, @Param("hoursAgo") Integer hours, Pageable page);

    @Query(value = "SELECT * FROM w_transactionrecordc2c WHERE type = :type AND status = :status AND ref_tx_id IS NULL AND update_date > NOW() - INTERVAL :hoursAgo HOUR", nativeQuery = true)
    List<TransactionRecordC2C> findByTypeAndStatusAndRefTxIdNullAndHoursAgo(@Param("type") Integer type, @Param("status") Integer status, @Param("hoursAgo") Integer hours, Pageable page);
}