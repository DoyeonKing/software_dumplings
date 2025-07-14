// src/main/java/com/example/yourproject/dto/request/UpdateUsernameRequest.java
package com.example.springboot.dto; // 请替换为您的实际包名

import jakarta.validation.constraints.NotBlank; // For Spring Boot 3+ / Spring Framework 6+
// import javax.validation.constraints.NotBlank; // For Spring Boot 2 / Spring Framework 5

public class UpdateUsernameRequest {

    @NotBlank(message = "新用户名不能为空") // 确保字段非空且非空白
    private String newUsername;

    // 默认构造函数是必须的，或者至少是推荐的，Spring会用它来实例化对象
    public UpdateUsernameRequest() {
    }

    public UpdateUsernameRequest(String newUsername) {
        this.newUsername = newUsername;
    }

    // Getter
    public String getNewUsername() {
        return newUsername;
    }

    // Setter
    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    @Override
    public String toString() {
        return "UpdateUsernameRequest{" +
                "newUsername='" + newUsername + '\'' +
                '}';
    }
}
