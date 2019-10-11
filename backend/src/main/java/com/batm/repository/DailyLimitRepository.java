package com.batm.repository;

import com.batm.entity.DailyLimit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyLimitRepository extends JpaRepository<DailyLimit, Long> {}