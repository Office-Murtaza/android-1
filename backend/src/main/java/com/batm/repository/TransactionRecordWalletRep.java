package com.batm.repository;

import com.batm.entity.Coin;
import com.batm.entity.Identity;
import com.batm.entity.TransactionRecordWallet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TransactionRecordWalletRep extends JpaRepository<TransactionRecordWallet, Long> {

    Optional<TransactionRecordWallet> findFirstByIdentityAndCoinAndTxIdAndTypeIn(Identity identity, Coin coin, String txId, List<Integer> types);

    List<TransactionRecordWallet> findAllByIdentityAndCoinAndTypeIn(Identity identity, Coin coin, List<Integer> types);

    @Query(value = "SELECT * FROM w_transactionrecordwallet WHERE status = :status AND update_date > NOW() - INTERVAL :hoursAgo HOUR", nativeQuery = true)
    List<TransactionRecordWallet> findAllByStatusAndHoursAgo(@Param("status") Integer status, @Param("hoursAgo") Integer hours, Pageable page);

    @Query(value = "SELECT * FROM w_transactionrecordwallet WHERE type = :type AND status = :status AND receiver_status = :receiverStatus AND update_date > NOW() - INTERVAL :daysAgo DAY", nativeQuery = true)
    List<TransactionRecordWallet> findByTypeAndStatusAndStepAndDaysAgo(@Param("type") Integer type, @Param("status") Integer status, @Param("receiverStatus") Integer receiverStatus, @Param("daysAgo") Integer days, Pageable page);

    @Query(value = "SELECT * FROM w_transactionrecordwallet WHERE type = :type AND status = :status AND ref_tx_id IS NULL AND update_date > NOW() - INTERVAL :hoursAgo HOUR", nativeQuery = true)
    List<TransactionRecordWallet> findByTypeAndStatusAndRefTxIdNullAndHoursAgo(@Param("type") Integer type, @Param("status") Integer status, @Param("hoursAgo") Integer hours, Pageable page);
}