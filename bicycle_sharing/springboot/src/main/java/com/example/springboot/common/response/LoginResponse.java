// src/main/java/com/example/springboot/common/response/LoginResponse.java
package com.example.springboot.common.response;

import com.example.springboot.entity.User; // 导入 User 实体 (或 Staff 实体)

/**
 * 登录响应数据传输对象 (DTO)
 * 用于封装登录成功后返回给前端的数据，包括用户信息和认证令牌
 */
public class LoginResponse {
    private Object  userInfo; // 登录成功的用户基本信息 (已脱敏)
    private String token; // 生成的认证令牌 JWT
    private String role;

    public LoginResponse(Object userInfo, String token) {
        this.userInfo = userInfo;
        this.token = token;
    }

    public LoginResponse(Object userInfo, String token, String role) {
        this.userInfo = userInfo;
        this.token = token;
        this.role = role;
    }

    public Object getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Object userInfo) {
        this.userInfo = userInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "LoginResponse{" +
               "user=" + userInfo +
               ", token='[PROTECTED]'" + // 避免日志中打印Token
               '}';
    }
}
