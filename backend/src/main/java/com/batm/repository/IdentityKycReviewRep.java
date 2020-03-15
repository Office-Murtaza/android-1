package com.batm.repository;

import com.batm.entity.Identity;
import com.batm.entity.IdentityKycReview;
import com.batm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IdentityKycReviewRep extends JpaRepository<IdentityKycReview, Long> {

    List<IdentityKycReview> findAllByIdentityOrderByIdDesc(Identity identity);

    void deleteAllByIdentity(Identity identity);
}