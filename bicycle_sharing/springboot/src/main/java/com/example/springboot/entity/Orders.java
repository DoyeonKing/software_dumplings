package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trips实体类
 * 对应数据库中的 'trips' 表
 * 用于表示用户的每一次骑行记录
 */
public class Orders { // 实体类名根据表名 'trips' 命名为 Trips
    // 订单唯一标识符，PRIMARY KEY NOT NULL
    private String orderid; // 对应INT

    // 共享单车唯一标识符，PRIMARY KEY NOT NULL
    private String bikeid;

    // 骑行用户ID，PRIMARY KEY NOT NULL，FOREIGN KEY (userid)
    private String userid;

    // 骑行开始时间，DATETIME，NOT NULL
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    // 骑行结束时间，DATETIME，NOT NULL
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    // 骑行开始纬度，DECIMAL(13, 10)，NOT NULL
    private BigDecimal startLat;

    // 骑行开始经度，DECIMAL(13, 10)，NOT NULL
    private BigDecimal startLon;

    // 骑行结束纬度，DECIMAL(13, 10)，NOT NULL
    private BigDecimal endLat;

    // 骑行结束经度，DECIMAL(13, 10)，NOT NULL
    private BigDecimal endLon;

    // 骑行距离 (米)，DECIMAL(10, 2)，NOT NULL
    private BigDecimal distanceM;

    // 骑行费用，DECIMAL(8, 2)，NOT NULL
    private BigDecimal cost;

    // 起点Geohash编码，VARCHAR(6)，NOT NULL
    private String startGeohash;

    // 终点Geohash编码，VARCHAR(6)，NOT NULL
    private String endGeohash;

    // 骑行开始星期几 (数字)，INT，NOT NULL
    private Integer startWeekday;

    // 骑行开始小时，INT，NOT NULL
    private Integer startHour;

    // 是否周末，INT，NOT NULL (0 表示非周末，1 表示周末)
    private Integer isWeekend;

    // 骑行时长（分钟），INT，NULL
    private Integer durationMinutes;

    // --- 构造函数 ---
    public Orders() {
    }

    public Orders(String orderid, String bikeid, String userid, LocalDateTime startTime, LocalDateTime endTime, BigDecimal startLat, BigDecimal startLon, BigDecimal endLat, BigDecimal endLon, BigDecimal distanceM, BigDecimal cost, String startGeohash, String endGeohash, Integer startWeekday, Integer startHour, Integer isWeekend, Integer durationMinutes) {
        this.orderid = orderid;
        this.bikeid = bikeid;
        this.userid = userid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLat = startLat;
        this.startLon = startLon;
        this.endLat = endLat;
        this.endLon = endLon;
        this.distanceM = distanceM;
        this.cost = cost;
        this.startGeohash = startGeohash;
        this.endGeohash = endGeohash;
        this.startWeekday = startWeekday;
        this.startHour = startHour;
        this.isWeekend = isWeekend;
        this.durationMinutes = durationMinutes;
    }

    // --- Getter 和 Setter 方法 ---
    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getBikeid() {
        return bikeid;
    }

    public void setBikeid(String bikeid) {
        this.bikeid = bikeid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getStartLat() {
        return startLat;
    }

    public void setStartLat(BigDecimal startLat) {
        this.startLat = startLat;
    }

    public BigDecimal getStartLon() {
        return startLon;
    }

    public void setStartLon(BigDecimal startLon) {
        this.startLon = startLon;
    }

    public BigDecimal getEndLat() {
        return endLat;
    }

    public void setEndLat(BigDecimal endLat) {
        this.endLat = endLat;
    }

    public BigDecimal getEndLon() {
        return endLon;
    }

    public void setEndLon(BigDecimal endLon) {
        this.endLon = endLon;
    }

    public BigDecimal getDistanceM() {
        return distanceM;
    }

    public void setDistanceM(BigDecimal distanceM) {
        this.distanceM = distanceM;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
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

    public Integer getStartWeekday() {
        return startWeekday;
    }

    public void setStartWeekday(Integer startWeekday) {
        this.startWeekday = startWeekday;
    }

    public Integer getStartHour() {
        return startHour;
    }

    public void setStartHour(Integer startHour) {
        this.startHour = startHour;
    }

    public Integer getIsWeekend() {
        return isWeekend;
    }

    public void setIsWeekend(Integer isWeekend) {
        this.isWeekend = isWeekend;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    @Override
    public String toString() {
        return "Trips{" +
               "orderid=" + orderid +
               ", bikeid='" + bikeid + '\'' +
               ", userid='" + userid + '\'' +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", startLat=" + startLat +
               ", startLon=" + startLon +
               ", endLat=" + endLat +
               ", endLon=" + endLon +
               ", distanceM=" + distanceM +
               ", cost=" + cost +
               ", startGeohash='" + startGeohash + '\'' +
               ", endGeohash='" + endGeohash + '\'' +
               ", startWeekday=" + startWeekday +
               ", startHour=" + startHour +
               ", isWeekend=" + isWeekend +
               ", durationMinutes=" + durationMinutes +
               '}';
    }
}