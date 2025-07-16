package com.example.springboot.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PasswordUpdateRequest {
    @NotNull(message = "用户ID不能为空")
    private Integer staffId;

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    // 构造函数
    public PasswordUpdateRequest() {
    }

    public PasswordUpdateRequest(Integer staffId, String oldPassword, String newPassword) {
        this.staffId = staffId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // Getter 和 Setter 方法
    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

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

    @Override
    public String toString() {
        return "PasswordUpdateRequest{" +
                "staffId=" + staffId +
                ", oldPassword='" + oldPassword + '\'' +
                ", newPassword='" + newPassword + '\'' +
                '}';
    }
}