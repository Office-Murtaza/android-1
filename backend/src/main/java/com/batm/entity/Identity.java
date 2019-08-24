package com.batm.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "identity")
public class Identity implements Serializable {

    private static final long serialVersionUID = -1712969322089989538L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long identityId;

    @Column(name = "publicid")
    private String publicId;

    @JsonManagedReference
    @OneToMany(mappedBy = "identity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Transaction> transactions;
}
