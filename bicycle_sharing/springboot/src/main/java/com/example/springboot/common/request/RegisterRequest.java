// src/main/java/com/example/springboot/common/request/RegisterRequest.java
package com.example.springboot.common.request;

/**
 * 注册请求数据传输对象 (DTO)
 * 用于封装前端发送的注册请求数据
 */
public class RegisterRequest {
    private String username;        // 用户名，对应前端的“用户名输入”（姓名）
    private String phoneNumber;     // 手机号，对应前端的“输入手机号”
    private String password;        // 密码（前端明文）
    private String confirmPassword; // 确认密码（前端校验用）
    private String role;            // 用户选择的角色： "user", "admin", "worker"

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
               "username='" + username + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", password='[PROTECTED]'" +
               ", confirmPassword='[PROTECTED]'" +
               ", role='" + role + '\'' +
               '}';
    }
}
