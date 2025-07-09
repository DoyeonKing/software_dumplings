package com.example.springboot.controller;

import com.example.springboot.service.AmapService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/route")
public class RouteController {

    private final AmapService amapService;

    public RouteController(AmapService amapService) {
        this.amapService = amapService;
    }

    @GetMapping("/driving-with-address")
    public Map<String, Object> getRouteWithAddress(
            @RequestParam String origin,
            @RequestParam String destination
    ) {
        return amapService.getRouteWithAddress(origin, destination);
    }
}
