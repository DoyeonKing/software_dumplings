package com.example.springboot.service.implementation;

import com.example.springboot.mapper.BikesMapper; // 导入纠正后的Mapper接口名
import com.example.springboot.service.Interface.IBikesService; // 导入纠正后的Service接口名
import jakarta.annotation.Resource; // 导入Resource注解
import org.springframework.stereotype.Service; // 导入Service注解


/**
 * BikesServiceImpl类空壳
 * 实现IBikesService接口，包含自行车业务逻辑的具体实现框架
 */
@Service // 标记这是一个Spring管理的Service组件
public class BikesServiceImpl implements IBikesService { // 实现接口名纠正为IBikesService

    @Resource // 注入BikesMapper
    private BikesMapper bikesMapper; // 注入的Mapper类型纠正为BikesMapper

    // 空壳：不在此处定义任何方法实现
}