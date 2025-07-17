package com.example.springboot.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator; // for activateDefaultTyping
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 1. String 序列化器用于键
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer); // Hash键通常也是字符串

        // 2. Jackson 序列化器用于值（推荐：可以处理任何Java对象，将其序列化为JSON字符串）
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 如果你需要存储复杂对象（有继承关系），并希望反序列化时能识别具体类型，可以加上下面这行
        // 注意：Jackson版本不同，方法可能略有差异，这是Jackson 2.10+的推荐做法
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL); // 添加类型信息到JSON

        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 为值和Hash值设置Jackson序列化器
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        // **不要设置setDefaultSerializer，除非你明确知道它的作用并且所有键和值都适合该序列化器**
        // 如果你设置了，它可能会覆盖你对KeySerializer和ValueSerializer的单独设置，导致问题。
        // redisTemplate.setDefaultSerializer(stringRedisSerializer); // 移除或注释掉这行！

        redisTemplate.afterPropertiesSet(); // 初始化Bean
        return redisTemplate;
    }
}
