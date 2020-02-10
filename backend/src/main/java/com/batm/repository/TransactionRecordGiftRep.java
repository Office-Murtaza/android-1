package com.batm.repository;

import com.batm.entity.Coin;
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
    List<TransactionRecordGift> findAllByIdentityAndCoinCode(
            @Param("identity") Identity identity,
            @Param("coinCode") String coinCode);

    @Query("SELECT trg FROM TransactionRecordGift trg WHERE trg.identity = :identity AND UPPER(trg.txId) = UPPER(:txId) AND UPPER(trg.coin.code) = UPPER(:coinCode)")
    Optional<TransactionRecordGift> findOneByIdentityAndTxIdAndCoinCode(
            @Param("identity") Identity identity,
            @Param("txId") String txId,
            @Param("coinCode") String coinCode
    );

    List<TransactionRecordGift> findByStatus(Integer status, Pageable page);

    List<TransactionRecordGift> findByTypeAndStatusAndReceiverStatus(Integer type, Integer status, Integer receiverStatus, Pageable page);
}