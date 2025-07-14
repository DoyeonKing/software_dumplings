package com.example.springboot.dto;

public class ProfileResponse {
    private Integer staffId;
    private String username;
    // 移除：private String staffType; // 这个字段之前存在，现在已经移除

    // 无参数构造器 (如果需要，建议保留，JSON反序列化可能需要)
    public ProfileResponse() {
    }

    // 新增/修改：接受 Integer staffId 和 String username 的构造器
    public ProfileResponse(Integer staffId, String username) {
        this.staffId = staffId;
        this.username = username;
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

    // 移除 staffType 的 Getter 和 Setter (如果之前有的话)
    // public String getStaffType() { /* ... */ }
    // public void setStaffType(String staffType) { /* ... */ }
}
