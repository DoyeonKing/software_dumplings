package com.example.springboot.dto;

public class WorkerInfoResponse {
    private Integer staffId;
    private String username;
    private Integer managerId;
    private Integer geohash;

    public WorkerInfoResponse(Integer staffId, String username, Integer managerId, Integer geohash) {
        this.staffId = staffId;
        this.username = username;
        this.managerId = managerId;
        this.geohash = geohash;
    }

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
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public Integer getGeohash() {
        return geohash;
    }
    public void setGeohash(Integer geohash) { this.geohash = geohash; }
}
