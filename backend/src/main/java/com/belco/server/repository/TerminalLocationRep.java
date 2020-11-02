package com.belco.server.repository;

import com.belco.server.entity.TerminalLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalLocationRep extends JpaRepository<TerminalLocation, Long> {}