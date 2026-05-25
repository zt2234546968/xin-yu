package com.xinyu.ecommerce.repository;

import com.xinyu.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // 根据手机号查询用户（不加载role关联，避免外键问题）
    @Query("SELECT u FROM User u WHERE u.phone = :phone AND u.deleted = 0")
    Optional<User> findByPhone(@Param("phone") String phone);

    // 检查手机号是否存在
    boolean existsByPhone(String phone);

    // 检查微信号是否存在
    boolean existsByWechat(String wechat);
    
    // 检查手机号是否被其他用户使用
    boolean existsByPhoneAndIdNot(String phone, String id);
    
    // 检查微信号是否被其他用户使用
    boolean existsByWechatAndIdNot(String wechat, String id);
}