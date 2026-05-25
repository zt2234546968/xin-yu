package com.xinyu.ecommerce.controller;

import com.xinyu.ecommerce.common.Result;
import com.xinyu.ecommerce.entity.Zhiping;
import com.xinyu.ecommerce.service.ZhipingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/zhiping")
@RequiredArgsConstructor
public class ZhipingController {

    private final ZhipingService zhipingService;

    @GetMapping("/list")
    @Operation(summary = "获取直评列表", description = "获取所有直评任务")
    public Result<List<Zhiping>> list() {
        return Result.success(zhipingService.getAllZhipings());
    }

    @GetMapping("/getById")
    @Operation(summary = "获取直评详情", description = "根据ID获取直评任务详情")
    public Result<Zhiping> getById(@RequestParam String id) {
        try {
            return Result.success(zhipingService.getZhipingById(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "创建直评任务", description = "创建新的直评任务，状态默认为等待提交")
    public Result<Zhiping> create(@RequestBody Zhiping zhiping) {
        try {
            Zhiping created = zhipingService.createZhiping(zhiping);
            return Result.success("创建成功", created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新直评任务", description = "根据ID更新直评任务信息")
    public Result<Zhiping> update(@RequestParam String id, @RequestBody Zhiping zhiping) {
        try {
            Zhiping updated = zhipingService.updateZhiping(id, zhiping);
            return Result.success("更新成功", updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "更新任务状态", description = "根据ID更新直评任务状态")
    public Result<Zhiping> updateStatus(@RequestParam String id, @RequestParam String status) {
        try {
            Zhiping updated = zhipingService.updateStatus(id, status);
            return Result.success("状态更新成功", updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/updateFeedback")
    @Operation(summary = "更新反馈信息", description = "根据ID更新直评任务反馈链接和图片")
    public Result<Zhiping> updateFeedback(@RequestParam String id, 
                                          @RequestParam(required = false) String feedbackLink, 
                                          @RequestParam(required = false) String feedbackImage,
                                          @RequestParam(required = false) String channelId) {
        System.out.println("========== Controller: updateFeedback ==========");
        System.out.println("id: " + id);
        System.out.println("feedbackLink: " + feedbackLink);
        System.out.println("feedbackImage: " + feedbackImage);
        System.out.println("channelId: " + channelId);
        System.out.println("===============================================");
        try {
            Zhiping updated = zhipingService.updateFeedback(id, feedbackLink, feedbackImage, channelId);
            return Result.success("反馈信息更新成功", updated);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除直评任务", description = "根据ID删除直评任务")
    public Result<Void> delete(@RequestParam String id) {
        try {
            zhipingService.deleteZhiping(id);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/generateCode")
    @Operation(summary = "生成直评编码", description = "生成从1000开始的四位数编码")
    public Result<String> generateCode() {
        try {
            String code = zhipingService.generateCode();
            return Result.success("生成成功", code);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}