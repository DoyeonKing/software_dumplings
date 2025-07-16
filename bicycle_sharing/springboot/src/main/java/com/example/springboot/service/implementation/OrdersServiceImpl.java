package com.example.springboot.service.implementation;

import com.example.springboot.entity.Bikes;
import com.example.springboot.entity.EliteSites;
import com.example.springboot.entity.Orders;
import com.example.springboot.mapper.BikesMapper;
import com.example.springboot.mapper.EliteSitesMapper;
import com.example.springboot.mapper.OrdersMapper; // 导入Mapper接口
import com.example.springboot.service.Interface.IOrdersService; // 导入Service接口
import com.example.springboot.util.GeohashUtil;
import com.example.springboot.util.LocationUtil;
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.stereotype.Service; // 导入Service注解
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * TripsServiceImpl类空壳
 * 实现ITripsService接口，包含用户行程记录业务逻辑的具体实现框架
 */
@Service // 标记这是一个Spring管理的Service组件
public class OrdersServiceImpl implements IOrdersService { // 实现接口命名为 ITripsService

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private BikesMapper bikesMapper;

    @Resource
    private EliteSitesMapper eliteSitesMapper;

    /**
     * 用车接口：根据用户ID和单车ID生成订单信息。
     *
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @return 新创建的订单对象，如果失败则返回null
     */
    @Override
    @Transactional // 确保原子性操作
    public Orders rentBike(String userId, String bikeId) {
        // 1. 查找单车信息
        Bikes bike = bikesMapper.findByBikeId(bikeId);
        if (bike == null) {
            System.out.println("单车不存在: " + bikeId);
            return null; // 单车不存在
        }

        // 2. 检查单车状态
        if ("使用中".equals(bike.getBikeStatus())) {
            System.out.println("该单车当前正在使用中，无法租借: " + bikeId);
            return null; // 单车正在使用中，无法租借
        }

        // 3. 检查用户是否有未完成的订单 (可选，根据业务需求决定是否允许一人多开)
        Orders existingActiveOrder = ordersMapper.findActiveOrderByUserAndBike(userId, bikeId);
        if (existingActiveOrder != null) {
            System.out.println("用户 " + userId + " 已有正在进行的单车 " + bikeId + " 的订单，请勿重复租借。");
            return null;
        }

        // 4. 创建订单对象并填写初始信息
        Orders newOrder = new Orders();
        newOrder.setOrderid(UUID.randomUUID().toString()); // 生成一个随机的UUID字符串作为订单ID
        System.out.println("生成的订单ID: " + newOrder.getOrderid()); // 方便调试查看生成的ID

        newOrder.setBikeid(bikeId);
        newOrder.setUserid(userId);

        LocalDateTime startTime = LocalDateTime.now();
        newOrder.setStartTime(startTime);
        newOrder.setStartLat(bike.getCurrentLat());
        newOrder.setStartLon(bike.getCurrentLon());

        // 直接从单车对象中获取 startGeohash
        newOrder.setStartGeohash(bike.getCurrentGeohash());

        // 计算日期时间相关信息
        newOrder.setStartWeekday(startTime.getDayOfWeek().getValue()); // 1=Monday, 7=Sunday
        newOrder.setStartHour(startTime.getHour());
        newOrder.setIsWeekend(startTime.getDayOfWeek() == DayOfWeek.SATURDAY ||
                startTime.getDayOfWeek() == DayOfWeek.SUNDAY ? 1 : 0);

        // 5. 插入订单
        int insertedRows = ordersMapper.insert(newOrder);
        if (insertedRows == 0) {
            System.out.println("创建订单失败。");
            return null; // 订单插入失败
        }

        // 6. 更新单车状态为“使用中”
        int updatedBikeRows = bikesMapper.updateBikeStatusAndLocation(
                bikeId,
                "使用中",
                bike.getCurrentLat(), // 保持起始经纬度不变，表示正在使用
                bike.getCurrentLon(),
                bike.getCurrentGeohash() // 更新单车的 Geohash
        );
        if (updatedBikeRows == 0) {
            System.out.println("更新单车状态失败，可能导致数据不一致。");
            throw new RuntimeException("更新单车状态失败"); // 抛出异常触发事务回滚
        }

        System.out.println("用户 " + userId + " 成功租借单车 " + bikeId + "。订单ID: " + newOrder.getOrderid());
        return newOrder;
    }
    /**
     * 还车接口：根据用户ID、单车ID、结束经纬度生成订单信息。
     *
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @param endLat 结束纬度
     * @param endLon 结束经度
     * @return 更新后的订单对象，如果失败则返回null
     */
    @Override
    @Transactional
    public Orders returnBike(String userId, String bikeId, BigDecimal endLat, BigDecimal endLon) {
        // 1. 查找用户和单车对应的活跃订单
        Orders activeOrder = ordersMapper.findActiveOrderByUserAndBike(userId, bikeId);
        if (activeOrder == null) {
            System.out.println("未找到用户 " + userId + " 针对单车 " + bikeId + " 的活跃订单。");
            return null; // 没有正在进行的订单
        }

        // 2. 检查停车区域是否合法 (判断结束经纬度是否在任一精英站点区域内)
        boolean isParkingAllowed = false;
        List<EliteSites> allEliteSites = eliteSitesMapper.findAll(); // 获取所有停车区域信息
        if (allEliteSites == null || allEliteSites.isEmpty()) {
            System.out.println("未找到任何停车区域信息。");
            return null; // 没有停车区域信息
        }

        for (EliteSites site : allEliteSites) {
            boolean isWithinBounds = LocationUtil.isWithinParkingArea(endLat, endLon, site);
            if (isWithinBounds) {
                isParkingAllowed = true;
                break;
            }
        }

        if (!isParkingAllowed) {
            System.out.println("还车失败：该地点不允许停车。结束经纬度: (" + endLat + ", " + endLon + ")");
            return null;
        }

        // 3. 填充还车信息
        activeOrder.setEndTime(LocalDateTime.now());
        activeOrder.setEndLat(endLat);
        activeOrder.setEndLon(endLon);

        // 直接从停车区域对象中获取 endGeohash
        for (EliteSites site : allEliteSites) {
            if (LocationUtil.isWithinParkingArea(endLat, endLon, site)) {
                activeOrder.setEndGeohash(site.getGeohash());
                break;
            }
        }

        // 4. 计算骑行距离
        BigDecimal distance = LocationUtil.calculateDistance(
                activeOrder.getStartLat(),
                activeOrder.getStartLon(),
                endLat,
                endLon
        );
        activeOrder.setDistanceM(distance);

        // 5. 计算骑行时长（分钟）
        long durationMinutes = ChronoUnit.MINUTES.between(activeOrder.getStartTime(), activeOrder.getEndTime());
        activeOrder.setDurationMinutes((int) durationMinutes);

        // 6. 计算费用 (简单示例：每分钟0.5元，不足1分钟按1分钟计费，最低1元)
        BigDecimal costPerMinute = new BigDecimal("0.5");
        BigDecimal minCost = new BigDecimal("1.0");
        BigDecimal calculatedCost = costPerMinute.multiply(new BigDecimal(Math.max(1, durationMinutes))); // 最少1分钟
        activeOrder.setCost(calculatedCost.max(minCost).setScale(2, BigDecimal.ROUND_HALF_UP));

        // 7. 更新订单
        int updatedOrderRows = ordersMapper.update(activeOrder);
        if (updatedOrderRows == 0) {
            System.out.println("更新订单失败。订单ID: " + activeOrder.getOrderid());
            return null; // 订单更新失败
        }

        // 8. 更新单车状态为“待使用”和新位置信息
        int updatedBikeRows = bikesMapper.updateBikeStatusAndLocation(
                bikeId,
                "待使用",
                endLat,
                endLon,
                activeOrder.getEndGeohash() // 使用订单中的 endGeohash
        );
        if (updatedBikeRows == 0) {
            System.out.println("更新单车状态失败，可能导致数据不一致。");
            throw new RuntimeException("更新单车状态失败"); // 抛出异常触发事务回滚
        }

        System.out.println("用户 " + userId + " 成功归还单车 " + bikeId + "。订单ID: " + activeOrder.getOrderid());
        return activeOrder;
    }
}