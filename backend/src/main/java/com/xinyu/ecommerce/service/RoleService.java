package com.xinyu.ecommerce.service;

import com.xinyu.ecommerce.entity.Role;
import com.xinyu.ecommerce.entity.User;
import com.xinyu.ecommerce.repository.RoleRepository;
import com.xinyu.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public Role createRole(String roleName, String description) {
        if (roleRepository.existsByRoleName(roleName)) {
            throw new RuntimeException("权限名称已存在");
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setStatus(1);

        return roleRepository.save(role);
    }

    public Optional<Role> getRoleById(String id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> getRoleByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role updateRole(String id, String roleName, String description, Integer status) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));

        if (roleName != null && !roleName.equals(role.getRoleName())) {
            if (roleRepository.existsByRoleName(roleName)) {
                throw new RuntimeException("权限名称已存在");
            }
            role.setRoleName(roleName);
        }

        if (description != null) {
            role.setDescription(description);
        }

        if (status != null) {
            role.setStatus(status);
        }

        return roleRepository.save(role);
    }

    public void deleteRole(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));

        role.setDeleted(1);
        roleRepository.save(role);
    }

    public void initDefaultRoles() {
        if (!roleRepository.existsByRoleName("SUPER_ADMIN")) {
            createRole("SUPER_ADMIN", "超级管理员，拥有系统所有权限");
        }
        if (!roleRepository.existsByRoleName("ADMIN")) {
            createRole("ADMIN", "管理员，拥有大部分管理权限");
        }
        if (!roleRepository.existsByRoleName("USER")) {
            createRole("USER", "普通用户，拥有基本访问权限");
        }
    }

    public void initSuperAdmin() {
        if (userRepository.existsByPhone("18530957887")) {
            return;
        }

        Optional<Role> superAdminRole = getRoleByRoleName("SUPER_ADMIN");
        if (superAdminRole.isEmpty()) {
            initDefaultRoles();
            superAdminRole = getRoleByRoleName("SUPER_ADMIN");
        }

        if (superAdminRole.isPresent()) {
            User superAdmin = new User();
            superAdmin.setPhone("18530957887");
            superAdmin.setPassword("xinyu0508");
            superAdmin.setRealName("柴新玉");
            superAdmin.setWechat("chai_xinyu");
            superAdmin.setInviteCode("SYSTEM");
            superAdmin.setRole(superAdminRole.get());
            superAdmin.setStatus(1);
            userRepository.save(superAdmin);
        }
    }
}