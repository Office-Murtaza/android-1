package com.batm.entity;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractAuditingEntity implements Serializable {

    @JsonIgnore
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private Instant createdDate = Instant.now();

    @JsonIgnore
    @UpdateTimestamp
    @Column(name = "update_date")
    private Instant lastModifiedDate = Instant.now();
}