package com.example.springboot.service.Interface;

import com.example.springboot.common.request.LoginRequest;
import com.example.springboot.common.request.RegisterRequest;
import com.example.springboot.common.response.LoginResponse;
import com.example.springboot.entity.Manager;

public interface IManagerService {
    LoginResponse login(LoginRequest loginRequest);
    Manager register(RegisterRequest registerRequest);
}