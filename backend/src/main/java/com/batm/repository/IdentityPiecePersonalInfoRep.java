package com.batm.repository;

import com.batm.entity.IdentityPiece;
import com.batm.entity.IdentityPiecePersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IdentityPiecePersonalInfoRep extends JpaRepository<IdentityPiecePersonalInfo, Long> {

    Optional<IdentityPiecePersonalInfo> findFirstByIdentityPieceOrderByIdDesc(IdentityPiece identityPiece);

    void deleteAllByIdentityPieceIn(List<IdentityPiece> identityPieces);
}