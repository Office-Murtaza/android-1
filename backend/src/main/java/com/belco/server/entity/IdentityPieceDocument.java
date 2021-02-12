package com.belco.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "identitypiecedocument")
public class IdentityPieceDocument extends BaseEntity {

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