package com.batm.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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