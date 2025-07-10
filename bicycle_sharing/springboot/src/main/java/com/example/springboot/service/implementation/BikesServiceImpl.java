package com.example.springboot.service.implementation;

import com.example.springboot.dto.UtilizationResponse;
import com.example.springboot.entity.Bikes;
import com.example.springboot.exception.CustomException;
import com.example.springboot.mapper.BikesMapper;
import com.example.springboot.service.Interface.IBikesService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


/**
 * BikesServiceImpl类空壳
 * 实现IBikesService接口，包含自行车业务逻辑的具体实现框架
 */
@Service // 标记这是一个Spring管理的Service组件
public class BikesServiceImpl implements IBikesService { // 实现接口名纠正为IBikesService

    @Resource // 注入BikesMapper
    private BikesMapper bikesMapper; // 注入的Mapper类型纠正为BikesMapper

    @Override
    public UtilizationResponse getVehicleUtilization() {
        // 1. 获取所有车辆的总数 (在线车辆)
        int onlineVehicles = bikesMapper.countAllBikes();

        // 2. 获取 '使用中' 车辆数
        int inUseVehicles = bikesMapper.countByStatus("使用中");

        // 3. 计算 '空闲' 车辆数
        int idleVehicles = onlineVehicles - inUseVehicles;
        // 确保空闲车辆不为负数，尽管在正常逻辑下不会出现
        if (idleVehicles < 0) {
            idleVehicles = 0;
        }

        // 4. 计算使用率
        double utilizationRatePercentage;
        if (onlineVehicles == 0) {
            utilizationRatePercentage = 0.0; // 避免除以零
        } else {
            // 使用 BigDecimal 进行精确计算，避免浮点数精度问题，并四舍五入到两位小数
            BigDecimal inUseBd = new BigDecimal(inUseVehicles);
            BigDecimal onlineBd = new BigDecimal(onlineVehicles);
            utilizationRatePercentage = inUseBd.divide(onlineBd, 4, RoundingMode.HALF_UP) // 计算到4位小数
                    .multiply(new BigDecimal(100))
                    .doubleValue(); // 转换为double
        }

        // 5. 构建并返回响应 DTO
        return new UtilizationResponse(
                utilizationRatePercentage,
                onlineVehicles,
                inUseVehicles,
                idleVehicles
        );
    }


    /**
     * 获取所有可用（状态为“待使用”）的单车列表
     *
     * @return 可用单车列表
     * @throws CustomException 如果获取失败
     */
    @Override
    public List<Bikes> getAllAvailableBikes() {
        try {
            // 假设 bikesMapper 有一个 findAllByStatus 方法
            // 这里的 "待使用" 应该与数据库中 ENUM('使用中', '待使用') 的值严格匹配
            return bikesMapper.findAllByStatus("待使用");
        } catch (Exception e) {
            throw new CustomException("获取所有可用单车失败: " + e.getMessage(), "500");
        }
    }

/**
 * 获取单车列表，支持按状态筛选和分页
 * @param pageNum 页码
 * @param pageSize 每页数量
 * @param bikeStatus 单车状态 (可选，如果为null则查询所有状态)
 * @return 包含分页信息的单车列表
 * @throws CustomException 如果获取失败
 */
@Override
public PageInfo<Bikes> getBikesByPage(Integer pageNum, Integer pageSize, String bikeStatus) {
    try {
        // 启动 PageHelper 分页
        PageHelper.startPage(pageNum, pageSize);

        List<Bikes> bikesList;
        if (bikeStatus != null && !bikeStatus.isEmpty()) {
            // 如果指定了状态，则按状态查询
            bikesList = bikesMapper.findAllByStatus(bikeStatus);
        } else {
            // 如果未指定状态，则查询所有单车
            bikesList = bikesMapper.findAlltoPages(); // 假设 BikesMapper 有一个 findAll 方法
        }

        // 使用 PageInfo 包装查询结果，它会自动计算总数、总页数等
        return new PageInfo<>(bikesList);
    } catch (Exception e) {
        throw new CustomException("获取单车列表失败: " + e.getMessage(), "500");
    }
}

    /**
     * 获取单个单车的详细信息（处理后不包含bikeId）
     *
     * @param bikeId 单车唯一标识符
     * @return 单车详情对象，如果未找到则为 null
     * @throws CustomException 如果获取失败
     */
    @Override
    public Bikes getBikeDetails(String bikeId) {
        try {
            Bikes bike = bikesMapper.findByBikeId(bikeId);
            if (bike != null) {
                // 根据需求，不显示单车ID，可以在返回前将其置空
                bike.setBikeId(null);
            }
            return bike;
        } catch (Exception e) {
            throw new CustomException("获取单车详情失败: " + e.getMessage(), "500");
        }
    }
/**
 * 获取指定地理范围内（视口内）的单车列表，支持按状态筛选
 * @param minLat 最小纬度
 * @param maxLat 最大纬度
 * @param minLon 最小经度
 * @param maxLon 最大经度
 * @param bikeStatus 单车状态 (可选，如果为null则查询所有状态)
 * @return 指定范围内的单车列表
 * @throws CustomException 如果获取失败
 */
@Override
public List<Bikes> getBikesInViewport(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLon, BigDecimal maxLon, String bikeStatus) {
    try {
        // 在这里可以添加参数校验，确保经纬度范围有效
        if (minLat == null || maxLat == null || minLon == null || maxLon == null) {
            throw new CustomException("地理范围参数不能为空", "400");
        }

        return bikesMapper.findInViewport(minLat, maxLat, minLon, maxLon, bikeStatus);
    } catch (CustomException e) {
        throw e; // 重新抛出业务异常
    } catch (Exception e) {
        throw new CustomException("获取视口内单车失败: " + e.getMessage(), "500");
    }
}
}