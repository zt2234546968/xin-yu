package com.xinyu.ecommerce.controller;

import com.xinyu.ecommerce.common.Result;
import com.xinyu.ecommerce.entity.MarketplaceTask;
import com.xinyu.ecommerce.service.MarketplaceTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class MarketplaceTaskController {

    private final MarketplaceTaskService marketplaceTaskService;

    @GetMapping("/list")
    public Result<List<MarketplaceTask>> list(@RequestParam(required = false) String taskType) {
        return Result.success(marketplaceTaskService.getTasks(taskType));
    }

    @GetMapping("/getById")
    public Result<MarketplaceTask> getById(@RequestParam String id) {
        try {
            return Result.success(marketplaceTaskService.getTaskById(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/create")
    public Result<MarketplaceTask> create(@RequestBody MarketplaceTask task) {
        try {
            return Result.success("Create success", marketplaceTaskService.createTask(task));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<MarketplaceTask> update(@RequestParam String id, @RequestBody MarketplaceTask task) {
        try {
            return Result.success("Update success", marketplaceTaskService.updateTask(id, task));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/updateStatus")
    public Result<MarketplaceTask> updateStatus(@RequestParam String id, @RequestParam String status) {
        try {
            return Result.success("Status updated", marketplaceTaskService.updateStatus(id, status));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/updateFeedback")
    public Result<MarketplaceTask> updateFeedback(
            @RequestParam String id,
            @RequestParam(required = false) String feedbackLink,
            @RequestParam(required = false) String feedbackImage,
            @RequestParam(required = false) String channel) {
        try {
            return Result.success("Feedback updated", marketplaceTaskService.updateFeedback(id, feedbackLink, feedbackImage, channel));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam String id) {
        try {
            marketplaceTaskService.deleteTask(id);
            return Result.success("Delete success", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/generateCode")
    public Result<String> generateCode(@RequestParam String taskType) {
        try {
            return Result.success("Generate success", marketplaceTaskService.generateCode(taskType));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
