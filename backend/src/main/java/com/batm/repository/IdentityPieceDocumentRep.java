package com.batm.repository;

import com.batm.entity.IdentityPiece;
import com.batm.entity.IdentityPieceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdentityPieceDocumentRep extends JpaRepository<IdentityPieceDocument, Long> {

    void deleteAllByIdentityPieceIn(List<IdentityPiece> identityPieces);
}