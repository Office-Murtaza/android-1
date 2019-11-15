package com.batm.rest;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.batm.entity.AtmAddress;
import com.batm.entity.OpenHour;
import com.batm.model.Response;
import com.batm.repository.AtmAddressRepository;

@RestController
@RequestMapping("/api/v1")
public class TerminalController {

    @Autowired
    private AtmAddressRepository atmAddressRepository;

    @GetMapping("/terminal/locations")
    public Response getTerminalLocations() {
        try {
            List<AtmAddress> atmAddresses = atmAddressRepository.findAll();

            for (AtmAddress atmAddress : atmAddresses) {
                List<OpenHour> sortedData = atmAddress
                        .getOpenHours()
                        .stream()
                        .sorted(Comparator.comparing(OpenHour::getId))
                        .collect(Collectors.toList());

                atmAddress.setOpenHours(sortedData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("addressList", atmAddresses);

            return Response.ok(response);
        } catch (Exception e) {
            return Response.serverError();
        }
    }
}