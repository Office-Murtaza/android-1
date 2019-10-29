package com.batm.repository;

import com.batm.entity.Identity;
import com.batm.entity.TransactionRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    @Query("SELECT tr FROM TransactionRecord tr WHERE tr.detail IS NOT NULL AND tr.cryptoCurrency IN :currency AND tr.tracked = false AND ((tr.type = 0 AND tr.status = 1) OR (tr.type = 1 AND tr.status = 3))")
    List<TransactionRecord> findUntrackedAndClosedTransactions(@Param("currency") List<String> currency, Pageable page);

    @Query("SELECT SUM(tr.cashAmount) FROM TransactionRecord tr WHERE tr.identity = :identity AND tr.serverTime >= :startDate AND tr.serverTime <= :endDate")
    BigDecimal getTransactionsSumByDate(@Param("identity") Identity identity, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}