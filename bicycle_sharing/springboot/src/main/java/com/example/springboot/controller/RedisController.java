package com.example.springboot.controller;

import com.example.springboot.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @RequestMapping("/aaa")
    public String testA(){
        User user = new User();
        user.setUsername("张三");
        user.setUserid("1");
        redisTemplate.opsForValue().set("user",user);
        return "success";
    }
}