package com.example.springboot.dto;

import java.math.BigDecimal;

public class WorkerInfoResponse {
    private Integer staffId;
    private String username;
    private Integer managerId;
    private Integer geohash;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public WorkerInfoResponse(Integer staffId, String username, Integer managerId, Integer geohash, BigDecimal latitude, BigDecimal longitude) {
        this.staffId = staffId;
        this.username = username;
        this.managerId = managerId;
        this.geohash = geohash;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter 和 Setter 方法
    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Integer getGeohash() {
        return geohash;
    }

    public void setGeohash(Integer geohash) {
        this.geohash = geohash;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
}