package com.batm.repository;

import com.batm.entity.Identity;
import com.batm.entity.IdentityPiece;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdentityPieceRep extends JpaRepository<IdentityPiece, Long> {
    List<IdentityPiece> findAllByIdentityAndPieceTypeIn(Identity identity, int[] pieceType);
}