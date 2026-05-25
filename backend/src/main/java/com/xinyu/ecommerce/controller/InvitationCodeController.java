package com.xinyu.ecommerce.controller;

import com.xinyu.ecommerce.common.Result;
import com.xinyu.ecommerce.entity.InvitationCode;
import com.xinyu.ecommerce.service.InvitationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invitationCode")
@RequiredArgsConstructor
public class InvitationCodeController {
    
    private final InvitationCodeService invitationCodeService;
    
    /**
     * 生成邀请码
     * POST /api/invitationCode/generate
     */
    @PostMapping("/generate")
    @Operation(summary = "生成邀请码", description = "生成新的邀请码")
    public Result<InvitationCode> generate() {
        try {
            InvitationCode code = invitationCodeService.generateCode();
            return Result.success("生成成功", code);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 验证邀请码
     * GET /api/invitationCode/validate
     */
    @GetMapping("/validate")
    @Operation(summary = "验证邀请码", description = "验证邀请码是否有效")
    public Result<Boolean> validate(String code) {
        try {
            boolean valid = invitationCodeService.validateCode(code);
            return Result.success(valid);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据邀请码查询
     * GET /api/invitationCode/getByCode
     */
    @GetMapping("/getByCode")
    @Operation(summary = "根据邀请码查询", description = "通过邀请码获取邀请码详细信息")
    public Result<InvitationCode> getByCode(String code) {
        return invitationCodeService.getByCode(code)
                .map(Result::success)
                .orElse(Result.error(404, "邀请码不存在"));
    }
    
    /**
     * 获取所有邀请码
     * GET /api/invitationCode/list
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有邀请码", description = "获取所有邀请码列表")
    public Result<java.util.List<InvitationCode>> list() {
        return Result.success(invitationCodeService.getAllCodes());
    }
    
    /**
     * 更新备注
     * POST /api/invitationCode/updateRemark
     */
    @PostMapping("/updateRemark")
    @Operation(summary = "更新备注", description = "更新邀请码的备注信息")
    public Result<InvitationCode> updateRemark(String id, String remark) {
        try {
            InvitationCode code = invitationCodeService.updateRemark(id, remark);
            return Result.success("更新成功", code);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}