package com.example.springboot.dto;

// 不再需要 import lombok 的东西了

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UtilizationResponse {
    // getter 和 setter
    private double utilization;
    private int totalBikes;
    private int availableBikes;
    private int inUseBikes;

    // --- 手动写下面的代码 ---

    // 1. 无参数的构造函数
    public UtilizationResponse() {
    }

    // 2. 包含所有参数的构造函数
    public UtilizationResponse(double utilization, int totalBikes, int availableBikes, int inUseBikes) {
        this.utilization = utilization;
        this.totalBikes = totalBikes;
        this.availableBikes = availableBikes;
        this.inUseBikes = inUseBikes;
    }

}