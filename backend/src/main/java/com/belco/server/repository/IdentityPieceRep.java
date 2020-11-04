package com.belco.server.repository;

import com.belco.server.entity.Identity;
import com.belco.server.entity.IdentityPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IdentityPieceRep extends JpaRepository<IdentityPiece, Long> {

    IdentityPiece findFirstByIdentityAndPieceTypeOrderByIdDesc(Identity identity, int pieceType);

    List<IdentityPiece> findAllByIdentityAndPieceTypeIn(Identity identity, int[] pieceType);
}