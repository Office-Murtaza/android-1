package com.batm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "identitypiece")
public class IdentityPiece extends BaseEntity {

    @ManyToOne(optional = false)
    private Identity identity;

    @Column(name = "piecetype")
    private int pieceType;

    @Column(name = "created")
    private Date created;

    @Column(name = "registration")
    private Boolean registration;
}