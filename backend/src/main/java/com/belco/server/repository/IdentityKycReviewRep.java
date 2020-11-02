package com.belco.server.repository;

import com.belco.server.entity.Identity;
import com.belco.server.entity.IdentityKycReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityKycReviewRep extends JpaRepository<IdentityKycReview, Long> {

    IdentityKycReview findFirstByIdentityOrderByIdDesc(Identity identity);

    void deleteByIdentity(Identity identity);
}