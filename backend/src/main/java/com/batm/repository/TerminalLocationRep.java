package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.batm.entity.TerminalLocation;

public interface TerminalLocationRep extends JpaRepository<TerminalLocation, Long> {}