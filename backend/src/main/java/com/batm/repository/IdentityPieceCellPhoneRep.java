package com.batm.repository;

import com.batm.entity.Identity;
import com.batm.entity.IdentityPiece;
import com.batm.entity.IdentityPieceCellPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IdentityPieceCellPhoneRep extends JpaRepository<IdentityPieceCellPhone, Long> {

    List<IdentityPieceCellPhone> findByPhoneNumber(String phone);

    IdentityPieceCellPhone findByIdentityAndIdentityPiece(Identity identity, IdentityPiece identityPiece);
}