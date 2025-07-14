package com.example.springboot.entity;

public class Manager {
    private Integer managerId;
    private String username;
    private String passwordHash;

    // 构造函数、Getter 和 Setter 方法
    public Manager() {
    }

    public Manager(Integer managerId, String username, String passwordHash) {
        this.managerId = managerId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}