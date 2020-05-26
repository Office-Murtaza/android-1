package com.batm.repository;

import com.batm.entity.Identity;
import com.batm.entity.TransactionRecordC2C;
import com.batm.entity.TransactionRecordReserve;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRecordReserveRep extends JpaRepository<TransactionRecordReserve, Long> {

//    @Query("SELECT trg FROM TransactionRecordReserve trg WHERE trg.identity = :identity AND UPPER(trg.coin.code) = UPPER(:coinCode)")
//    List<TransactionRecordC2C> findAllByIdentityAndCoinCode(@Param("identity") Identity identity, @Param("coinCode") String coinCode);
//
//    @Query("SELECT trg FROM TransactionRecordC2C trg WHERE trg.identity = :identity AND UPPER(trg.txId) = UPPER(:txId) AND UPPER(trg.coin.code) = UPPER(:coinCode)")
//    Optional<TransactionRecordC2C> findOneByIdentityAndTxIdAndCoinCode(@Param("identity") Identity identity, @Param("txId") String txId, @Param("coinCode") String coinCode);

    @Query(value = "SELECT * FROM w_transactionrecordreserve WHERE type = :type AND status = :status AND update_date > NOW() - INTERVAL :hoursAgo HOUR", nativeQuery = true)
    List<TransactionRecordReserve> findByTypeAndStatusAndHoursAgo(@Param("type") Integer type, @Param("status") Integer status, @Param("hoursAgo") Integer hours, Pageable page);
}