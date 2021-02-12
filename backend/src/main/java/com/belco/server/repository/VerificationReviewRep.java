package com.belco.server.repository;

import com.belco.server.entity.Identity;
import com.belco.server.entity.VerificationReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationReviewRep extends JpaRepository<VerificationReview, Long> {

    VerificationReview findFirstByIdentityOrderByIdDesc(Identity identity);

    void deleteByIdentity(Identity identity);
}