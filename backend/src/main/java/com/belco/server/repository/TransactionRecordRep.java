package com.belco.server.repository;

import com.belco.server.entity.Identity;
import com.belco.server.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TransactionRecordRep extends JpaRepository<TransactionRecord, Long> {

    @Query("SELECT tr FROM TransactionRecord tr WHERE tr.identity = :identity AND UPPER(tr.cryptoCurrency) = UPPER(:coinCode)")
    List<TransactionRecord> findAllByIdentityAndCryptoCurrency(@Param("identity") Identity identity, @Param("coinCode") String coinCode);

    @Query("SELECT tr FROM TransactionRecord tr WHERE tr.identity = :identity AND UPPER(tr.detail) = UPPER(:txId) AND UPPER(tr.cryptoCurrency) = UPPER(:coinCode)")
    Optional<TransactionRecord> findOneByIdentityAndDetailAndCryptoCurrency(@Param("identity") Identity identity, @Param("txId") String txId, @Param("coinCode") String coinCode);

    @Query("SELECT SUM(tr.cashAmount) FROM TransactionRecord tr WHERE tr.identity = :identity AND tr.serverTime >= :startDate AND tr.serverTime <= :endDate")
    BigDecimal getTransactionsSumByDate(@Param("identity") Identity identity, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}