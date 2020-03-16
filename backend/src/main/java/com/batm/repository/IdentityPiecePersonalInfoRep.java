package com.batm.repository;

import com.batm.entity.IdentityPiece;
import com.batm.entity.IdentityPiecePersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdentityPiecePersonalInfoRep extends JpaRepository<IdentityPiecePersonalInfo, Long> {

    void deleteAllByIdentityPieceIn(List<IdentityPiece> identityPieces);
}