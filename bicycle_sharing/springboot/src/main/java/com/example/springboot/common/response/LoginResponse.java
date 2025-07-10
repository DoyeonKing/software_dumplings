// src/main/java/com/example/springboot/common/response/LoginResponse.java
package com.example.springboot.common.response;

import com.example.springboot.entity.User; // 导入 User 实体 (或 Staff 实体)

/**
 * 登录响应数据传输对象 (DTO)
 * 用于封装登录成功后返回给前端的数据，包括用户信息和认证令牌
 */
public class LoginResponse {
    private User user; // 登录成功的用户基本信息 (已脱敏)
    private String token; // 生成的认证令牌 JWT

    public LoginResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
               "user=" + user +
               ", token='[PROTECTED]'" + // 避免日志中打印Token
               '}';
    }
}
