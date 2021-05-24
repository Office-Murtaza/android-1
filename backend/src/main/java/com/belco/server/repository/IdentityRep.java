package com.belco.server.repository;

import com.belco.server.entity.Identity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdentityRep extends JpaRepository<Identity, Long> {

    Optional<Identity> findOneByPublicId(String publicId);
}