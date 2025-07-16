package com.example.springboot.common;

// PasswordResetPair.java
public class PasswordResetPair {
    private Integer staffId;
    private String passwordHash;

    // 构造方法、Getter和Setter
    public PasswordResetPair(Integer staffId, String passwordHash) {
        this.staffId = staffId;
        this.passwordHash = passwordHash;
    }

    // Getter和Setter方法
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}