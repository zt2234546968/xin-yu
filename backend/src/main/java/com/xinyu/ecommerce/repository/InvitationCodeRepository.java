package com.xinyu.ecommerce.repository;

import com.xinyu.ecommerce.entity.InvitationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationCodeRepository extends JpaRepository<InvitationCode, String> {
    
    // 根据邀请码查询
    Optional<InvitationCode> findByCode(String code);
    
    // 查询未使用的邀请码
    Optional<InvitationCode> findByUsed(Integer used);
    
    // 检查邀请码是否存在
    boolean existsByCode(String code);
    
    // 根据用户ID查询邀请码
    Optional<InvitationCode> findByUserId(String userId);
}