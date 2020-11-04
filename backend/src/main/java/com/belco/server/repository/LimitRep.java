package com.belco.server.repository;

import com.belco.server.entity.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LimitRep extends JpaRepository<Limit, Long> {}