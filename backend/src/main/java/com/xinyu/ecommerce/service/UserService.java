package com.xinyu.ecommerce.service;

import com.xinyu.ecommerce.entity.Role;
import com.xinyu.ecommerce.entity.User;
import com.xinyu.ecommerce.entity.dto.LoginRequest;
import com.xinyu.ecommerce.entity.dto.LoginResponse;
import com.xinyu.ecommerce.entity.dto.RegisterRequest;
import com.xinyu.ecommerce.repository.RoleRepository;
import com.xinyu.ecommerce.repository.UserRepository;
import com.xinyu.ecommerce.service.InvitationCodeService;
import com.xinyu.ecommerce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InvitationCodeService invitationCodeService;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 手动加载角色，避免外键关联问题
        String roleName = "USER";
        if (user.getRole() != null && user.getRole().getId() != null) {
            Optional<Role> roleOpt = roleRepository.findById(user.getRole().getId());
            if (roleOpt.isPresent()) {
                roleName = roleOpt.get().getRoleName();
            }
        }

        String token = JwtUtil.generateToken(user.getId(), user.getPhone(), roleName);
        return new LoginResponse(token, user);
    }

    /**
     * 注册用户
     */
    public User register(RegisterRequest request) {
        // 验证必填字段
        if (request.getRealName() == null || request.getRealName().trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (request.getWechat() == null || request.getWechat().trim().isEmpty()) {
            throw new RuntimeException("微信号不能为空");
        }
        if (request.getInviteCode() == null || request.getInviteCode().trim().isEmpty()) {
            throw new RuntimeException("邀请码不能为空");
        }

        // 验证密码二次确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 检查手机号是否已存在
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }

        // 检查微信号是否已存在
        if (userRepository.existsByWechat(request.getWechat())) {
            throw new RuntimeException("微信号已存在");
        }

        // 验证邀请码是否有效
        if (!"SYSTEM".equals(request.getInviteCode()) && !invitationCodeService.validateCode(request.getInviteCode())) {
            throw new RuntimeException("邀请码无效");
        }

        // 获取默认用户角色
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new RuntimeException("默认角色不存在，请联系管理员"));

        // 构建用户对象
        User user = new User();
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setWechat(request.getWechat());
        user.setInviteCode(request.getInviteCode());
        user.setAvatar(request.getAvatar());
        user.setRole(userRole);

        // 保存用户
        return userRepository.save(user);
    }

    /**
     * 获取所有用户
     */
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 更新用户信息
     */
    public User updateUserInfo(String id, String realName, String phone, String wechat) {
        // 验证用户是否存在
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新用户信息
        if (realName != null && !realName.trim().isEmpty()) {
            user.setRealName(realName);
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            // 检查手机号是否已被其他用户使用
            if (userRepository.existsByPhoneAndIdNot(phone, id)) {
                throw new RuntimeException("手机号已被使用");
            }
            user.setPhone(phone);
        }
        
        if (wechat != null && !wechat.trim().isEmpty()) {
            // 检查微信号是否已被其他用户使用
            if (userRepository.existsByWechatAndIdNot(wechat, id)) {
                throw new RuntimeException("微信号已被使用");
            }
            user.setWechat(wechat);
        }
        
        // 保存更新
        return userRepository.save(user);
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 修改密码
     */
    public void updatePassword(String id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }
}