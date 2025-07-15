package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.implementation.PredictionServiceImpl;
import com.example.springboot.service.ReportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

// 大模型控制器
@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final PredictionServiceImpl predictionService;
    private final ReportService reportService;

    public LlmController(PredictionServiceImpl predictionService, ReportService reportService) {
        this.predictionService = predictionService;
        this.reportService = reportService;
    }

    /**
     * 接收前端的文本请求，调用大模型生成内容 (此接口现在已禁用)
     * @param requestBody 包含prompt的请求体，例如：{"prompt": "你好，写一个关于自行车的笑话"}
     * @return 包含模型生成文本的Result对象
     */
    @PostMapping("/generate")
    public Result generateLlmText(@RequestBody Map<String, String> requestBody) {
        // 由于大模型功能已禁用，此接口返回功能禁用提示
        System.out.println("POST /api/llm/generate 接口被调用，但大模型功能已禁用。");
        return Result.error("503", "大模型文本生成功能目前已禁用。"); // 返回服务不可用错误

        /*
        // 原始代码，如果需要重新启用大模型，请取消注释并确保PredictionServiceImpl中的方法也已启用
        String prompt = requestBody.get("prompt");

        if (prompt == null || prompt.trim().isEmpty()) {
            return Result.error("400", "请求内容不能为空");
        }

        try {
           // String generatedText = predictionService.generateTextWithQwen(prompt); // 此行已在PredictionServiceImpl中移除
           // return Result.success(generatedText);
        } catch (Exception e) {
            System.err.println("调用大模型生成文本失败: " + e.getMessage());
            return Result.error("500", "大模型服务调用失败");
        }
        */
    }

    /**
     * 获取后端定时任务生成的最新报告
     * @return 包含最新报告文本的Result对象
     */
    @GetMapping("/latest-report")
    public Result getLatestReport() {
        try {
            String report = reportService.getLatestReport();
            return Result.success(report);
        } catch (Exception e) {
            System.err.println("获取最新报告失败: " + e.getMessage());
            return Result.error("500", "获取报告失败");
        }
    }
}
