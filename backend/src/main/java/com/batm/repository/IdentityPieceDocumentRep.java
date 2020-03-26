package com.batm.repository;

import com.batm.entity.IdentityPiece;
import com.batm.entity.IdentityPieceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IdentityPieceDocumentRep extends JpaRepository<IdentityPieceDocument, Long> {

    Optional<IdentityPieceDocument> findFirstByIdentityPieceOrderByIdDesc(IdentityPiece identityPiece);

    void deleteAllByIdentityPieceIn(List<IdentityPiece> identityPieces);
}