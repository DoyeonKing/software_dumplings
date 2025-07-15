package com.example.springboot.dto;

public class ProfileResponse {
    private Integer staffId;
    private String username;
    private Integer managerId; // 新增字段
    private Integer geohash;   // 新增字段

    // 无参数构造器 (建议保留，JSON 反序列化可能需要)
    public ProfileResponse() {
    }

    // 更新的构造器：现在接受 staffId, username, managerId, geohash
    public ProfileResponse(Integer staffId, String username, Integer managerId, Integer geohash) {
        this.staffId = staffId;
        this.username = username;
        this.managerId = managerId; // 设置 managerId
        this.geohash = geohash;     // 设置 geohash
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

    // 新增 managerId 的 Getter 和 Setter
    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    // 新增 geohash 的 Getter 和 Setter
    public Integer getGeohash() {
        return geohash;
    }

    public void setGeohash(Integer geohash) {
        this.geohash = geohash;
    }
}
