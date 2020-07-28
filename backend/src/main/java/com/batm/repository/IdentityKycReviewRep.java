package com.batm.repository;

import com.batm.entity.Identity;
import com.batm.entity.IdentityKycReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityKycReviewRep extends JpaRepository<IdentityKycReview, Long> {

    IdentityKycReview findFirstByIdentityOrderByIdDesc(Identity identity);

    void deleteByIdentity(Identity identity);
}