package com.belco.server.repository;

import com.belco.server.entity.Identity;
import com.belco.server.entity.IdentityPiece;
import com.belco.server.entity.IdentityPieceCellPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IdentityPieceCellPhoneRep extends JpaRepository<IdentityPieceCellPhone, Long> {

    List<IdentityPieceCellPhone> findByPhoneNumber(String phone);

    IdentityPieceCellPhone findByIdentityAndIdentityPiece(Identity identity, IdentityPiece identityPiece);
}