package com.example.springboot.entity;

/**
 * Staff实体类
 * 对应数据库中的 'staff' 表
 * 用于表示工作人员或管理人员信息
 */
public class Staff {
    // 工作人员唯一标识符，PRIMARY KEY, AUTO_INCREMENT
    private Integer staffId; // 使用Integer来匹配AUTO_INCREMENT的INT类型

    // 登录用户名，NOT NULL, UNIQUE
    private String username;

    // 密码的哈希值，NOT NULL (实际存储应是加密后的哈希值)
    private String passwordHash;

    // 角色类型，ENUM('管理员', '工作人员')，NOT NULL
    //private String staffType; // 使用String来表示ENUM类型

    private Integer managerId;

    private Integer geohash;

    // --- 构造函数 ---
    public Staff() {
    }

    public Staff(Integer staffId, String username, String passwordHash, String staffType, Integer managerId, Integer geohash) {
        this.staffId = staffId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.managerId = managerId;
        this.geohash = geohash;
    }

    // --- Getter 和 Setter 方法 ---
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

//    public String getStaffType() {
//        return staffType;
//    }
//
//    public void setStaffType(String staffType) {
//        this.staffType = staffType;
//    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public Integer getGeohash() {return geohash;}

    public void setGeohash(Integer geohash) {this.geohash = geohash; }

    @Override
    public String toString() {
        return "Staff{" +
               "staffId=" + staffId +
               ", username='" + username + '\'' +
               ", managerId='" + managerId + '\'' +
                ", geohash=" + geohash + '\'' +
               '}';
    }
}