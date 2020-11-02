package com.belco.server.rest;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.belco.server.entity.TerminalLocation;
import com.belco.server.entity.TerminalLocationHour;
import com.belco.server.model.Response;
import com.belco.server.repository.TerminalLocationRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TerminalController {

    @Autowired
    private TerminalLocationRep terminalLocationRep;

    @GetMapping("/terminal/locations")
    public Response getTerminalLocations() {
        try {
            List<TerminalLocation> terminalLocations = terminalLocationRep.findAll();

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