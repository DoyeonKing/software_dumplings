package com.example.springboot.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @class WebConfig
 * @description Spring MVC 配置类
 *
 * 1. 这个文件的核心作用是注册拦截器，并指定它要拦截和放行的路径。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 2. 注入我们写好的JWT拦截器。
     */
    @Resource
    private JwtInterceptor jwtInterceptor;

    /**
     * 3. 重写 addInterceptors 方法来配置拦截规则。
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 4. 添加JWT拦截器到注册表中。
        registry.addInterceptor(jwtInterceptor)
                // 5. **核心配置**：指定要拦截的路径。
                // "/**" 表示拦截所有路径。
                .addPathPatterns("/**")
                // 6. **核心配置**：指定要放行（不拦截）的路径。
                // 我们需要放行登录和注册接口，否则用户永远无法登录。
                .excludePathPatterns(
                        // 认证相关
                        "/login",
                        "/register",
                        "/managers/**", // 管理员相关接口

                        // 公开数据查询
                        "/bikes/**",        // 所有与车辆信息查看相关的接口
                        "/weather/**",      // 天气接口
                        "/route/**",        // 路线规划接口
                        "/staff/**",        //工作人员
                        "/managers/**",      //管理员
                        "/elite-sites/**",  // 精华站点接口
                        "/dispatchTasks/**", // 调度任务接口

                        // 其他
                        "/files/**",        // 文件服务
                        "/swagger-ui.html", // Swagger文档页面
                        "/v3/api-docs/**"   // Swagger API数据
                );
    }
}
