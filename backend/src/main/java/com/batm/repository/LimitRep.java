package com.batm.repository;

import com.batm.entity.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LimitRep extends JpaRepository<Limit, Long> {}