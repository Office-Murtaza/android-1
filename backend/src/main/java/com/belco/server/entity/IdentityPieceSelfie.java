package com.belco.server.entity;

import lombok.*;
import javax.persistence.*;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "identitypieceselfie")
public class IdentityPieceSelfie extends BaseEntity {

    @ManyToOne(optional = false)
    private Identity identity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "identitypiece_id")
    private IdentityPiece identityPiece;

    @Column(name = "filename")
    private String fileName;

    @Column(name = "mimetype")
    private String mimeType;

    @Column(name = "created")
    private Date created;
}