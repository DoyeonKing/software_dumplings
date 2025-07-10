package com.example.springboot.dto; // 请替换为你的实际包名

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Integer staffId;
    private String username;
    private String staffType;
    // 不包含 passwordHash
}
