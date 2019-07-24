package com.batm.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.batm.entity.AtmAddress;
import com.batm.entity.Response;
import com.batm.repository.AtmAddressRepository;

@RestController
@RequestMapping("/api/v1")
public class AddressController {

	@Autowired
	private AtmAddressRepository atmAddressRepository;

	@GetMapping("/static/atm/address")
	public Response getAtmAddress() {
		List<AtmAddress> atmAddresses = atmAddressRepository.findAll();

		Map<String, Object> response = new HashMap<>();
		response.put("addressList", atmAddresses);
		return Response.ok(response);
	}

}
