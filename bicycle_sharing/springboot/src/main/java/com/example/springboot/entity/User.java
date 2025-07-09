package com.example.springboot.entity;

import java.math.BigDecimal; // 用于DECIMAL类型

/**
 * User实体类
 * 对应数据库中的 'user' 表
 */
public class User {
    // 骑行用户唯一标识符，PRIMARY KEY
    private String userid;

    // 登录用户名，可以是电话号码或开放ID，NOT NULL
    private String username;

    // 密码的哈希值，NOT NULL (注意：实际存储应是加密后的哈希值)
    private String passwordHash;

    // 电话号码，UNIQUE
    private String phoneNumber;

    // 骑行总次数，DEFAULT 0 (从trips聚合计算)
    private Integer totalRides;

    // 骑行总时长 (分钟)，DEFAULT 0 (从trips聚合计算)
    private Integer totalDurationMinutes;

    // 总消费金额，DECIMAL(8, 2)，DEFAULT 0.00 (从trips聚合计算)
    private BigDecimal totalCost;

    // --- 构造函数 ---
    public User() {
    }

    public User(String userid, String username, String passwordHash, String phoneNumber, Integer totalRides, Integer totalDurationMinutes, BigDecimal totalCost) {
        this.userid = userid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.totalRides = totalRides;
        this.totalDurationMinutes = totalDurationMinutes;
        this.totalCost = totalCost;
    }

    // --- Getter 和 Setter 方法 ---
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getTotalRides() {
        return totalRides;
    }

    public void setTotalRides(Integer totalRides) {
        this.totalRides = totalRides;
    }

    public Integer getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(Integer totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public String toString() {
        return "User{" +
               "userid='" + userid + '\'' +
               ", username='" + username + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", totalRides=" + totalRides +
               ", totalDurationMinutes=" + totalDurationMinutes +
               ", totalCost=" + totalCost +
               '}';
    }
}