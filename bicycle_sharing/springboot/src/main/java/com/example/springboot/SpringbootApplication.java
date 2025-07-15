package com.example.springboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption; // 导入 ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler; // 导入 ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler; // 导入 WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector; // 导入 ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient; // 导入 HttpClient
import java.time.Duration; // 导入 Duration
import java.util.concurrent.TimeUnit; // 导入 TimeUnit

@SpringBootApplication
@MapperScan("com.example.springboot.mapper")
@EnableScheduling
public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }

    /**
     * 配置WebClient.Builder Bean
     * 增加连接和读取超时时间
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 连接超时：10秒
                .responseTimeout(Duration.ofSeconds(120)) // 响应超时：120秒 (从发送请求到接收完整响应)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(120, TimeUnit.SECONDS)) // 读取超时：120秒
                        .addHandlerLast(new WriteTimeoutHandler(120, TimeUnit.SECONDS))); // 写入超时：120秒

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
