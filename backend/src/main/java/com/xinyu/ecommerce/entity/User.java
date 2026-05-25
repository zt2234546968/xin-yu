package com.xinyu.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, nullable = false, unique = true)
    private String id;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50, nullable = false)
    private String realName;

    @Column(length = 20, nullable = false, unique = true)
    private String phone;

    @Column(length = 50, nullable = false)
    private String wechat;

    @Column(length = 50, nullable = false)
    private String inviteCode;

    @Column(length = 255)
    private String avatar;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Role role;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer deleted = 0;
}