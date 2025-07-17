package com.example.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class RedisController {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @RequestMapping("/aaa")
    public String testA(){
        redisTemplate.opsForValue().set("test","123456");
        return "success";
    }
}