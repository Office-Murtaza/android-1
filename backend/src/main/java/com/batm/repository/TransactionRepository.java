package com.batm.repository;

import com.batm.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @EntityGraph(value = Transaction.IDENTITY)
    @Query("from Transaction tr where tr.cryptoCurrency in :currency and tr.tracked = false and ((tr.type = 0 and tr.status = 1) or (tr.type = 1 and tr.status = 3))")
    List<Transaction> findUnTrackedClosedTransactions(@Param("currency") Set<String> currency, Pageable page);

}
