package com.belco.server.repository;

import com.belco.server.entity.Coin;
import com.belco.server.entity.Identity;
import com.belco.server.entity.TransactionRecordWallet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRecordWalletRep extends JpaRepository<TransactionRecordWallet, Long> {

    Optional<TransactionRecordWallet> findFirstByTxId(String txId);

    Optional<TransactionRecordWallet> findFirstByIdentityAndCoinAndTxIdAndTypeIn(Identity identity, Coin coin, String txId, List<Integer> types);

    Optional<TransactionRecordWallet> findFirstByIdentityAndCoinAndTypeAndStatusOrderByCreateDateDesc(Identity identity, Coin coin, Integer type, Integer status);

    Optional<TransactionRecordWallet> findFirstByIdentityAndCoinAndTypeOrderByCreateDateDesc(Identity identity, Coin coin, Integer type);

    List<TransactionRecordWallet> findAllByIdentityAndCoin(Identity identity, Coin coin);

    List<TransactionRecordWallet> findAllByProcessedAndStatus(Integer processed, Integer status, Pageable page);

    List<TransactionRecordWallet> findAllByProcessedAndTypeAndStatusAndReceiverStatus(Integer processed, Integer type, Integer status, Integer receiverStatus, Pageable page);

    List<TransactionRecordWallet> findAllByProcessedAndTypeAndStatusAndRefTxIdNull(Integer processed, Integer type, Integer status, Pageable page);
}