package com.example.springboot.entity;

import java.time.LocalDateTime;

/**
 * DispatchTasks实体类
 * 对应数据库中的 'dispatch_tasks' 表
 * 用于表示共享单车的调度任务
 */
public class DispatchTasks { // 实体类名纠正为 DispatchTasks
    // 任务唯一ID，PRIMARY KEY AUTO_INCREMENT
    private Long taskId; // 对应BIGINT

    // 起始区域Geohash，VARCHAR(6)，NOT NULL
    private String startGeohash;

    // 目标区域Geohash，VARCHAR(6)，NOT NULL
    private String endGeohash;

    // 调度车辆数量，INT，NOT NULL
    private Integer bikeCount;

    // 被分配的工作人员ID，INT，NOT NULL (对应staff表的staff_id)
    private Integer assignedTo;

    // 任务状态，ENUM('未处理', '处理中', '处理完成')，NOT NULL
    private String status; // 使用String来表示ENUM类型

    // 任务创建时间，DATETIME，NOT NULL
    private LocalDateTime createdAt;

    // 任务完成时间，DATETIME，NULL
    private LocalDateTime completedAt;

    // --- 构造函数 ---
    public DispatchTasks() { // 构造函数名与类名一致
    }

    public DispatchTasks(Long taskId, String startGeohash, String endGeohash, Integer bikeCount, Integer assignedTo, String status, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.taskId = taskId;
        this.startGeohash = startGeohash;
        this.endGeohash = endGeohash;
        this.bikeCount = bikeCount;
        this.assignedTo = assignedTo;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // --- Getter 和 Setter 方法 ---
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getStartGeohash() {
        return startGeohash;
    }

    public void setStartGeohash(String startGeohash) {
        this.startGeohash = startGeohash;
    }

    public String getEndGeohash() {
        return endGeohash;
    }

    public void setEndGeohash(String endGeohash) {
        this.endGeohash = endGeohash;
    }

    public Integer getBikeCount() {
        return bikeCount;
    }

    public void setBikeCount(Integer bikeCount) {
        this.bikeCount = bikeCount;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "DispatchTasks{" +
               "taskId=" + taskId +
               ", startGeohash='" + startGeohash + '\'' +
               ", endGeohash='" + endGeohash + '\'' +
               ", bikeCount=" + bikeCount +
               ", assignedTo=" + assignedTo +
               ", status='" + status + '\'' +
               ", createdAt=" + createdAt +
               ", completedAt=" + completedAt +
               '}';
    }
}