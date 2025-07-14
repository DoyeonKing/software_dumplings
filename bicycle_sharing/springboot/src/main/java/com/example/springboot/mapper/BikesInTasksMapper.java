package com.example.springboot.mapper;

import com.example.springboot.entity.BikesInTasks;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper // 标记这是一个 MyBatis Mapper 接口，Spring 会自动扫描并创建其实例。
public interface BikesInTasksMapper {

    /**
     * 插入一个调度任务和自行车关联记录。
     * 【作用】：将单个任务与自行车的关联信息保存到数据库。
     * @param bikesInTasks 调度任务和自行车关联实体对象。
     */
    void insert(BikesInTasks bikesInTasks); // 方法签名，SQL 在 XML 中定义

    /**
     * 批量插入调度任务和自行车关联记录。
     * 【作用】：一次性将多辆自行车与一个任务关联起来，效率更高。
     * @param list 包含多个 BikesInTasks 对象的列表。
     */
    void insertBatch(@Param("list") List<BikesInTasks> list); // 方法签名，SQL 在 XML 中定义

    /**
     * 根据任务ID查询所有关联的自行车ID。
     * 【作用】：在后续业务中，可以通过任务ID快速获取所有涉及的自行车。
     * @param taskId 调度任务的ID。
     * @return 关联的自行车ID字符串列表。
     */
    List<String> findBikeIdsByTaskId(@Param("taskId") Long taskId); // 方法签名，SQL 在 XML 中定义
}