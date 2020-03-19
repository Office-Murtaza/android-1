package com.batm.repository;

import com.batm.entity.IdentityPiece;
import com.batm.entity.IdentityPieceSelfie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdentityPieceSelfieRep extends JpaRepository<IdentityPieceSelfie, Long> {
    Optional<IdentityPieceSelfie> findFirstByIdentityPieceOrderByIdDesc(IdentityPiece identityPiece);
    void deleteAllByIdentityPieceIn(List<IdentityPiece> identityPieces);
}