package com.example.springboot.config;

import io.swagger.v3.oas.models.Components; // 导入 Components
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme; // 导入 SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement; // 导入 SecurityRequirement

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        // 定义安全方案的名称，这个名称会在Swagger UI的Authorize对话框中显示
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info().title("我的 Spring Boot API 文档") // API 标题
                        .description("这是一个为我的 Spring Boot 项目生成的 API 文档。") // API 描述
                        .version("1.0.0") // API 版本
                        .contact(new Contact().name("您的名字").url("http://yourwebsite.com").email("youremail@example.com")) // 联系人
                        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"))) // 许可证
                .externalDocs(new ExternalDocumentation() // 外部文档
                        .description("更多信息请访问我们的官网")
                        .url("http://yourcompany.com"))
                // --- 关键添加部分：定义安全组件 ---
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName) // 安全方案的名称
                                .type(SecurityScheme.Type.HTTP) // 类型为 HTTP 认证
                                .scheme("bearer") // 使用 "bearer" 方案 (例如 Bearer Token)
                                .bearerFormat("JWT"))) // 格式为 JWT
                // --- 关键添加部分：应用安全要求 ---
                // 这会告诉SpringDoc，哪些接口需要这个安全方案。
                // .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
                // 上面这行代码会把认证应用到所有接口。如果你只想应用到部分接口，可以删除这行，
                // 然后在Controller或方法上使用 @SecurityRequirement(name = "bearerAuth") 注解。
                // 但为了方便测试，先全局应用是好的。
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}