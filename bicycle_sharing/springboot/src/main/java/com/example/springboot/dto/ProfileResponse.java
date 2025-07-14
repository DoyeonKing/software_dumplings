package com.example.springboot.dto;

// 不再需要 import lombok 的东西了

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProfileResponse {
    // 为了完整，最好也把getter/setter加上
    private Integer staffId;
    private String username;

    // --- 手动写下面的代码 ---

    // 1. 无参数的构造函数
    public ProfileResponse() {
    }

    // 2. 包含所有参数的构造函数
    public ProfileResponse(Integer staffId, String username, String staffType) {
        this.staffId = staffId;
        this.username = username;
    }

}