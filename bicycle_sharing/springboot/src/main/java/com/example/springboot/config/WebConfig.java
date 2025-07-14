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
                        "/login",           // 放行登录接口
                        "/register",        // 放行注册接口
                        "/files/**",        // 如果有文件上传/下载，也需要放行
                        "/swagger-ui.html", // 放行Swagger文档页面
                        "/v3/api-docs/**"   // 放行Swagger API数据
                );
    }
}
