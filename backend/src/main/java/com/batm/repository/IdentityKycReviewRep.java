package com.batm.repository;

import com.batm.entity.Identity;
import com.batm.entity.IdentityKycReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdentityKycReviewRep extends JpaRepository<IdentityKycReview, Long> {
    IdentityKycReview findOneByIdentityAndTierId(Identity identity, Integer tierId);

    List<IdentityKycReview> findAllByIdentityOrderByIdDesc(Identity identity);
}