// src/main/java/com/example/springboot/common/request/ChangePasswordRequest.java
package com.example.springboot.common.request;

/**
 * 修改密码请求数据传输对象 (DTO)
 * 用于封装前端发送的修改密码请求数据
 */
public class ChangePasswordRequest {
    private String oldPassword; // 旧密码（明文）
    private String newPassword; // 新密码（明文）
    private String confirmNewPassword; // 确认新密码（前端校验用）

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
               "oldPassword='[PROTECTED]'" +
               ", newPassword='[PROTECTED]'" +
               ", confirmNewPassword='[PROTECTED]'" +
               '}';
    }
}