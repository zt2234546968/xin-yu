package com.xinyu.ecommerce.entity.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    
    private String password;
    
    private String confirmPassword;
    
    private String realName;
    
    private String phone;
    
    private String wechat;
    
    private String inviteCode;
    
    private String avatar;
}