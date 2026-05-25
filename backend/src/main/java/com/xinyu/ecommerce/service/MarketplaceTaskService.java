package com.xinyu.ecommerce.service;

import com.xinyu.ecommerce.entity.MarketplaceTask;

import java.util.List;

public interface MarketplaceTaskService {
    List<MarketplaceTask> getTasks(String taskType);
    MarketplaceTask getTaskById(String id);
    MarketplaceTask createTask(MarketplaceTask task);
    MarketplaceTask updateTask(String id, MarketplaceTask task);
    MarketplaceTask updateStatus(String id, String status);
    MarketplaceTask updateFeedback(String id, String feedbackLink, String feedbackImage, String channel);
    void deleteTask(String id);
    String generateCode(String taskType);
}
