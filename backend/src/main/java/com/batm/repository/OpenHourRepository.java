package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.OpenHour;

public interface OpenHourRepository extends JpaRepository<OpenHour, Long> {}