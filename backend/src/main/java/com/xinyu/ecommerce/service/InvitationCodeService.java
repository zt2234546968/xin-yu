package com.xinyu.ecommerce.service;

import com.xinyu.ecommerce.entity.InvitationCode;
import com.xinyu.ecommerce.repository.InvitationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationCodeService {
    
    private final InvitationCodeRepository invitationCodeRepository;
    
    /**
     * 生成邀请码
     */
    public InvitationCode generateCode() {
        String code = generateUniqueCode();
        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setCode(code);
        invitationCode.setUsed(0);
        return invitationCodeRepository.save(invitationCode);
    }
    
    /**
     * 验证邀请码
     */
    public boolean validateCode(String code) {
        Optional<InvitationCode> invitationCode = invitationCodeRepository.findByCode(code.toUpperCase());
        return invitationCode.isPresent();
    }
    
    /**
     * 使用邀请码（邀请码可重复使用，不需要标记为已使用）
     */
    public void useCode(String code, String userId) {
        // 邀请码可重复使用，不需要标记为已使用
        // 仅记录使用情况（可选）
    }
    
    /**
     * 根据邀请码查询
     */
    public Optional<InvitationCode> getByCode(String code) {
        return invitationCodeRepository.findByCode(code);
    }
    
    /**
     * 获取所有邀请码
     */
    public java.util.List<InvitationCode> getAllCodes() {
        return invitationCodeRepository.findAll();
    }
    
    /**
     * 更新备注
     */
    public InvitationCode updateRemark(String id, String remark) {
        Optional<InvitationCode> codeOpt = invitationCodeRepository.findById(id);
        if (codeOpt.isPresent()) {
            InvitationCode code = codeOpt.get();
            code.setRemark(remark);
            return invitationCodeRepository.save(code);
        }
        throw new RuntimeException("邀请码不存在");
    }
    
    /**
     * 生成唯一邀请码
     */
    private String generateUniqueCode() {
        String code;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                int index = (int) (Math.random() * chars.length());
                sb.append(chars.charAt(index));
            }
            code = sb.toString().toUpperCase();
        } while (invitationCodeRepository.existsByCode(code));
        return code;
    }
}