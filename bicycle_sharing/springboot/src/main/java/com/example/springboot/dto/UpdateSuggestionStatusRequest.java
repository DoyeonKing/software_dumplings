package com.example.springboot.dto;

import lombok.Data; // 确保导入 lombok.Data

@Data
public class UpdateSuggestionStatusRequest {
    private Integer suggestionId;
    private String newStatus;

    // 如果您没有使用Lombok，请手动添加 Getter/Setter：
    /*
    public Integer getSuggestionId() {
        return suggestionId;
    }

    public void setSuggestionId(Integer suggestionId) {
        this.suggestionId = suggestionId;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
    */
}