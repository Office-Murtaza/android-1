package com.batm.repository;

import com.batm.entity.IdentityPiece;
import com.batm.entity.IdentityPieceSelfie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdentityPieceSelfieRep extends JpaRepository<IdentityPieceSelfie, Long> {

    void deleteAllByIdentityPieceIn(List<IdentityPiece> identityPieces);
}