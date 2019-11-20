package com.batm.rest;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.batm.entity.TerminalLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.batm.entity.TerminalLocationHour;
import com.batm.model.Response;
import com.batm.repository.TerminalLocationRepository;

@RestController
@RequestMapping("/api/v1")
public class TerminalController {

    @Autowired
    private TerminalLocationRepository terminalLocationRepository;

    @GetMapping("/terminal/locations")
    public Response getTerminalLocations() {
        try {
            List<TerminalLocation> terminalLocations = terminalLocationRepository.findAll();

            for (TerminalLocation terminalLocation : terminalLocations) {
                List<TerminalLocationHour> sortedData = terminalLocation
                        .getHours()
                        .stream()
                        .sorted(Comparator.comparing(TerminalLocationHour::getId))
                        .collect(Collectors.toList());

                terminalLocation.setHours(sortedData);
            }

            Map<String, List<TerminalLocation>> res = new HashMap<>();
            res.put("addresses", terminalLocations);

            return Response.ok(res);
        } catch (Exception e) {
            return Response.serverError();
        }
    }
}