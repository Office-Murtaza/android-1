package com.belco.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.belco.server.entity.Unlink;

public interface UnlinkRep extends JpaRepository<Unlink, Long> {

    Unlink findByUserId(Long userId);
}