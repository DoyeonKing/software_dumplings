// src/main/java/com/example/springboot/controller/UserController.java (示例)
package com.example.springboot.controller;

import com.example.springboot.entity.User; // 假设您有User实体
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "用户管理", description = "用户相关的各项操作接口") // 为整个控制器添加标签和描述
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Operation(summary = "获取所有用户列表", description = "返回系统中所有用户的详细信息") // 描述API操作
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户列表",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class, type = "array"))), // 返回类型为User数组
            @ApiResponse(responseCode = "404", description = "未找到任何用户")
    })
    @GetMapping
    public List<User> getAllUsers() {
        // 假设这里从数据库获取用户
        return Arrays.asList(new User(1L, "Alice", "alice@example.com"), new User(2L, "Bob", "bob@example.com"));
    }

    @Operation(summary = "根据ID获取用户", description = "根据用户唯一ID获取指定用户的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功找到用户",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{id}")
    public User getUserById(
            @Parameter(description = "用户ID", required = true, example = "1") // 描述请求参数
            @PathVariable Long id) {
        // 假设这里从数据库获取用户
        if (id == 1L) {
            return new User(1L, "Alice", "alice@example.com");
        }
        return null; // 或者抛出异常
    }

    @Operation(summary = "创建新用户", description = "创建一个新的用户账户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "用户创建成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    @PostMapping
    public User createUser(@RequestBody User user) {
        // 假设这里保存用户到数据库
        user.setId(System.currentTimeMillis()); // 模拟生成ID
        System.out.println("创建用户: " + user.getName());
        return user;
    }
}
