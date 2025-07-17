package com.example.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig { // 您的安全配置类名称

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 禁用CSRF（对于API通常需要，但要根据项目实际安全性要求决定）
            .authorizeHttpRequests(authorize -> authorize
                // ****** 确保以下所有公共API路径都被permitAll() ******
                .requestMatchers(
                    "/login",           // AuthController 的登录接口
                    "/register",        // AuthController 的注册接口
                    "/hello",           // 如果有 HelloController 的公开接口
                    // 根据您的API设计，添加所有公共访问的API前缀或完整路径
                    "/bikes/**",// 如果bikes/viewport是公开的，**表示匹配子路径
                    "/weatherRecord**",      // 如果天气API是公开的
                    // 其他可能公开的API，例如：
                     "/public/**",
                     "/auth/**", // 如果AuthContoller有@RequestMapping("/auth")
                        "/dispatchTasks/**",
                    "/elitesites/**",
                    "/managers/**",
                    "/orders/**",
                    "/api/predict/**",
                    "/api/route/**",
                    "/staffs/**",
                    "/users/**",
                    "/api/simulate/**",


                    // Swagger/OpenAPI 文档和UI路径（如果您有集成）
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll() // 允许所有这些路径无需认证即可访问
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            // 禁用Spring Security默认的表单登录、session管理和HTTP Basic认证，因为您使用JWT
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)); // 通常JWT应用是无状态的

        // 如果您有自定义的JWT过滤器，请确保它被正确添加到链中，并在认证通过后设置SecurityContext
        // 例如：
        // .addFilterBefore(yourJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 您可能还需要一个PasswordEncoder Bean
    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }
}