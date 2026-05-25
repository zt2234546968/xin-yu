package com.xinyu.ecommerce.entity.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String phone;

    private String password;
}