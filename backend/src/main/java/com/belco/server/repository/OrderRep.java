package com.belco.server.repository;

import com.belco.server.entity.Order;
import com.belco.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRep extends JpaRepository<Order, Long> {

    List<Order> findAllByMakerOrTaker(User maker, User taker);
}