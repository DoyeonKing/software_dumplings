// src/main/java/com/example/springboot/common/request/LoginRequest.java
package com.example.springboot.common.request;

/**
 * 登录请求数据传输对象 (DTO)
 * 用于封装前端发送的登录请求数据
 */
public class LoginRequest {
    private String username; // 用户名，对应前端的“用户名”输入（可能是邮箱或手机号）
    private String password; // 密码（前端明文）
    private String role;     // 用户选择的角色： "user", "admin", "worker"

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
               "username='" + username + '\'' +
               ", password='[PROTECTED]'" + // 避免日志中打印密码
               ", role='" + role + '\'' +
               '}';
    }
}
