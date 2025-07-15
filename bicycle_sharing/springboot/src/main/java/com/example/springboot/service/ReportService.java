package com.example.springboot.service;

import org.springframework.stereotype.Service;

/**
 * 用于存储和获取最新生成报告的服务。
 * 报告内容由定时任务调用大模型生成。
 */
@Service
public class ReportService {

    // 使用volatile确保多线程可见性
    private volatile String latestReport = "等待首次报告生成...";

    /**
     * 设置最新的报告内容。
     * @param report 最新生成的报告字符串
     */
    public void setLatestReport(String report) {
        this.latestReport = report;
    }

    /**
     * 获取最新的报告内容。
     * @return 最新报告字符串
     */
    public String getLatestReport() {
        return latestReport;
    }
}
