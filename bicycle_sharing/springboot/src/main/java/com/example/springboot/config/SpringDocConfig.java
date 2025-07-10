// src/main/java/com/example/springboot/config/SpringDocConfig.java (建议在config包中)
package com.example.springboot.config; // 确保包名正确

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("我的 Spring Boot API 文档") // API 标题
                        .description("这是一个为我的 Spring Boot 项目生成的 API 文档。") // API 描述
                        .version("v1.0.0") // API 版本
                        .contact(new Contact().name("您的名字").url("http://yourwebsite.com").email("youremail@example.com")) // 联系人
                        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"))) // 许可证
                .externalDocs(new ExternalDocumentation() // 外部文档
                        .description("更多信息请访问我们的官网")
                        .url("http://yourcompany.com"));
    }
}
