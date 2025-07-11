// src/main/java/com/example/dispatch/dto/DispatchTaskRequest.java
package com.example.springboot.dto;

public class DispatchTaskRequest {

    private String startGeohash; // 对应 DispatchTasks.startGeohash
    private String endGeohash;   // 对应 DispatchTasks.endGeohash
    private Integer assignedTo;  // 对应 DispatchTasks.assignedTo (Integer 类型)
    private Integer bikeCount;   // 对应 DispatchTasks.bikeCount

    // Getters and Setters
    public String getStartGeohash() {
        return startGeohash;
    }

    public void setStartGeohash(String startGeohash) {
        this.startGeohash = startGeohash;
    }

    public String getEndGeohash() {
        return endGeohash;
    }

    public void setEndGeohash(String endGeohash) {
        this.endGeohash = endGeohash;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Integer getBikeCount() {
        return bikeCount;
    }

    public void setBikeCount(Integer bikeCount) {
        this.bikeCount = bikeCount;
    }
}
