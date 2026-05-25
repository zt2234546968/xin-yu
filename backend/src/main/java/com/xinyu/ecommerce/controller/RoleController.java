package com.xinyu.ecommerce.controller;

import com.xinyu.ecommerce.common.Result;
import com.xinyu.ecommerce.entity.Role;
import com.xinyu.ecommerce.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/create")
    @Operation(summary = "创建角色", description = "创建新的角色权限")
    public Result<Role> createRole(@RequestParam String roleName,
                                  @RequestParam(required = false) String description) {
        try {
            Role role = roleService.createRole(roleName, description);
            return Result.success("创建成功", role);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/getById")
    @Operation(summary = "根据ID获取角色", description = "通过角色ID获取角色详细信息")
    public Result<Role> getById(String id) {
        return roleService.getRoleById(id)
                .map(Result::success)
                .orElse(Result.error(404, "权限不存在"));
    }

    @GetMapping("/getByRoleName")
    @Operation(summary = "根据角色名获取角色", description = "通过角色名获取角色详细信息")
    public Result<Role> getByRoleName(String roleName) {
        return roleService.getRoleByRoleName(roleName)
                .map(Result::success)
                .orElse(Result.error(404, "权限不存在"));
    }

    @GetMapping("/list")
    @Operation(summary = "获取角色列表", description = "获取所有角色的列表")
    public Result<List<Role>> getAllRoles() {
        return Result.success(roleService.getAllRoles());
    }

    @PostMapping("/update")
    @Operation(summary = "更新角色", description = "更新角色的信息")
    public Result<Role> updateRole(@RequestParam String id,
                                  @RequestParam(required = false) String roleName,
                                  @RequestParam(required = false) String description,
                                  @RequestParam(required = false) Integer status) {
        try {
            Role role = roleService.updateRole(id, roleName, description, status);
            return Result.success("更新成功", role);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    @Operation(summary = "删除角色", description = "根据角色ID删除角色")
    public Result<Void> delete(String id) {
        try {
            roleService.deleteRole(id);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/init")
    @Operation(summary = "初始化默认角色", description = "初始化系统默认角色：SUPER_ADMIN, ADMIN, USER")
    public Result<String> initDefaultRoles() {
        roleService.initDefaultRoles();
        return Result.success("初始化成功", "已创建默认权限：SUPER_ADMIN, ADMIN, USER");
    }

    @PostMapping("/initSuperAdmin")
    @Operation(summary = "初始化超级管理员", description = "初始化系统超级管理员账号")
    public Result<String> initSuperAdmin() {
        roleService.initSuperAdmin();
        return Result.success("初始化成功", "超级管理员账号：手机号18530957887，密码xinyu0508");
    }
}