package com.belco.server.repository;

import com.belco.server.entity.IdentityPiece;
import com.belco.server.entity.IdentityPieceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IdentityPieceDocumentRep extends JpaRepository<IdentityPieceDocument, Long> {

    Optional<IdentityPieceDocument> findFirstByIdentityPieceOrderByIdDesc(IdentityPiece identityPiece);

    void deleteAllByIdentityPieceIn(List<IdentityPiece> identityPieces);
}