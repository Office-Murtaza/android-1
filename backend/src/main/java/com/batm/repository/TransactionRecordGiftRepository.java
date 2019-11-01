package com.batm.repository;

import com.batm.entity.TransactionRecordGift;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRecordGiftRepository extends JpaRepository<TransactionRecordGift, Long> {

    List<TransactionRecordGift> findByStatus(Integer status, Pageable page);

    List<TransactionRecordGift> findByTypeAndStatusAndStep(Integer type, Integer status, Integer step, Pageable page);
}