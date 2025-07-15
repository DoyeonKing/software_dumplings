// src/main/java/com/example/springboot/dto/CenterLatLonResponse.java
package com.example.springboot.dto;

import java.math.BigDecimal;

public class CenterLatLonResponse {
    private BigDecimal centerLat;
    private BigDecimal centerLon;

    public CenterLatLonResponse(BigDecimal centerLat, BigDecimal centerLon) {
        this.centerLat = centerLat;
        this.centerLon = centerLon;
    }

    public BigDecimal getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(BigDecimal centerLat) {
        this.centerLat = centerLat;
    }

    public BigDecimal getCenterLon() {
        return centerLon;
    }

    public void setCenterLon(BigDecimal centerLon) {
        this.centerLon = centerLon;
    }
}