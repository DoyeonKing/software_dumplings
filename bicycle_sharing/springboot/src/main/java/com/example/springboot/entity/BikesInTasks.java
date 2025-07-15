package com.example.springboot.entity;

public class BikesInTasks {
    private Long taskId; // 对应 bikes_in_tasks 表的 task_id 列
    private String bikeId; // 对应 bikes_in_tasks 表的 bike_id 列

    // 无参构造函数
    public BikesInTasks() {
    }

    // 全参构造函数
    public BikesInTasks(Long taskId, String bikeId) {
        this.taskId = taskId;
        this.bikeId = bikeId;
    }

    // Getter 方法
    public Long getTaskId() {
        return taskId;
    }

    // Setter 方法
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    // Getter 方法
    public String getBikeId() {
        return bikeId;
    }

    // Setter 方法
    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    @Override
    public String toString() {
        return "BikesInTasks{" +
               "taskId=" + taskId +
               ", bikeId='" + bikeId + '\'' +
               '}';
    }
}