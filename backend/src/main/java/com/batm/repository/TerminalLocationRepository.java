package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.TerminalLocation;

public interface TerminalLocationRepository extends JpaRepository<TerminalLocation, Long> {}