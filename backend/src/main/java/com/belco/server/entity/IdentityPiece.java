package com.belco.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "identitypiece")
public class IdentityPiece extends BaseEntity {

    public static final int TYPE_FINGERPRINT = 0;
    public static final int TYPE_EMAIL = 1;
    public static final int TYPE_ID_SCAN = 2;
    public static final int TYPE_PERSONAL_INFORMATION = 3;
    public static final int TYPE_CELLPHONE = 4;
    public static final int TYPE_SELFIE = 5;
    public static final int TYPE_CAMERA_IMAGE = 6;

    @ManyToOne(optional = false)
    private Identity identity;

    @Column(name = "piecetype")
    private int pieceType;

    @Column(name = "created")
    private Date created;

    @Column(name = "registration")
    private Boolean registration;
}