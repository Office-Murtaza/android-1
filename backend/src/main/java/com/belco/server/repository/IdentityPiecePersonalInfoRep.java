package com.belco.server.repository;

import com.belco.server.entity.IdentityPiece;
import com.belco.server.entity.IdentityPiecePersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IdentityPiecePersonalInfoRep extends JpaRepository<IdentityPiecePersonalInfo, Long> {

    Optional<IdentityPiecePersonalInfo> findFirstByIdentityPieceOrderByIdDesc(IdentityPiece identityPiece);

    void deleteAllByIdentityPieceIn(List<IdentityPiece> identityPieces);
}