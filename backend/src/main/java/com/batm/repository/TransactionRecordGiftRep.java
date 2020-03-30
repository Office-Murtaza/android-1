package com.batm.repository;

import com.batm.entity.Identity;
import com.batm.entity.TransactionRecordGift;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TransactionRecordGiftRep extends JpaRepository<TransactionRecordGift, Long> {

    @Query("SELECT trg FROM TransactionRecordGift trg WHERE trg.identity = :identity AND UPPER(trg.coin.code) = UPPER(:coinCode)")
    List<TransactionRecordGift> findAllByIdentityAndCoinCode(@Param("identity") Identity identity, @Param("coinCode") String coinCode);

    @Query("SELECT trg FROM TransactionRecordGift trg WHERE trg.identity = :identity AND UPPER(trg.txId) = UPPER(:txId) AND UPPER(trg.coin.code) = UPPER(:coinCode)")
    Optional<TransactionRecordGift> findOneByIdentityAndTxIdAndCoinCode(@Param("identity") Identity identity, @Param("txId") String txId, @Param("coinCode") String coinCode);

    @Query(value = "SELECT * FROM w_transactionrecordgift WHERE status = :status AND update_date > NOW() - INTERVAL :hoursAgo HOUR", nativeQuery = true)
    List<TransactionRecordGift> findByStatusAndHoursAgo(@Param("status") Integer status, @Param("hoursAgo") Integer hours, Pageable page);

    @Query(value = "SELECT * FROM w_transactionrecordgift WHERE type = :type AND status = :status AND receiver_status = :receiverStatus AND update_date > NOW() - INTERVAL :daysAgo DAY", nativeQuery = true)
    List<TransactionRecordGift> findByTypeAndStatusAndStepAndDaysAgo(@Param("type") Integer type, @Param("status") Integer status, @Param("receiverStatus") Integer receiverStatus, @Param("daysAgo") Integer days, Pageable page);
}