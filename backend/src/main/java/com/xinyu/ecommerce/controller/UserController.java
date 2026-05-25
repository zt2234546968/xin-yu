package com.xinyu.ecommerce.controller;

import com.xinyu.ecommerce.common.Result;
import com.xinyu.ecommerce.entity.User;
import com.xinyu.ecommerce.entity.dto.LoginRequest;
import com.xinyu.ecommerce.entity.dto.LoginResponse;
import com.xinyu.ecommerce.entity.dto.RegisterRequest;
import com.xinyu.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 用户登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据手机号和密码登录系统")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse loginResponse = userService.login(request);
            return Result.success("登录成功", loginResponse);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账号")
    public Result<User> register(@RequestBody RegisterRequest request) {
        try {
            User registeredUser = userService.register(request);
            return Result.success("注册成功", registeredUser);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有用户
     * GET /api/user/list
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有用户", description = "获取所有用户列表")
    public Result<java.util.List<User>> list() {
        return Result.success(userService.getAllUsers());
    }
    
    /**
     * 更新用户信息
     * PUT /api/user/update
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新用户的姓名、手机号和微信号")
    public Result<User> update(@RequestBody User user) {
        try {
            User updatedUser = userService.updateUserInfo(user.getId(), user.getRealName(), user.getPhone(), user.getWechat());
            return Result.success("更新成功", updatedUser);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户信息
     * GET /api/user/info
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    public Result<User> info(@RequestParam String id) {
        try {
            User user = userService.getUserById(id);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     * PUT /api/user/password
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "根据用户ID修改密码")
    public Result<Void> updatePassword(@RequestParam String id, @RequestParam String oldPassword, @RequestParam String newPassword) {
        try {
            userService.updatePassword(id, oldPassword, newPassword);
            return Result.success("密码修改成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}