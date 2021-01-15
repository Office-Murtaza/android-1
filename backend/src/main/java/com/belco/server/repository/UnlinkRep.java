package com.belco.server.repository;

import com.belco.server.entity.Unlink;
import com.belco.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnlinkRep extends JpaRepository<Unlink, Long> {

    Unlink findFirstByUser(User user);
}