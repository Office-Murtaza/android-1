package com.batm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.batm.entity.AtmAddress;

public interface AtmAddressRepository extends JpaRepository<AtmAddress, Long> {

}
