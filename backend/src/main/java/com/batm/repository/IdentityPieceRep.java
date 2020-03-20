package com.batm.repository;

import com.batm.entity.Identity;
import com.batm.entity.IdentityPiece;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdentityPieceRep extends JpaRepository<IdentityPiece, Long> {
    Optional<IdentityPiece> findFirstByIdentityAndPieceTypeOrderByIdDesc(Identity identity, int pieceType);
    List<IdentityPiece> findAllByIdentityAndPieceTypeIn(Identity identity, int[] pieceType);
}
