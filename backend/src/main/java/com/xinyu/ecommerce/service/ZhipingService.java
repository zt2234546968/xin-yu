package com.xinyu.ecommerce.service;

import com.xinyu.ecommerce.entity.Zhiping;

import java.util.List;

public interface ZhipingService {
    List<Zhiping> getAllZhipings();
    Zhiping getZhipingById(String id);
    Zhiping createZhiping(Zhiping zhiping);
    Zhiping updateZhiping(String id, Zhiping zhiping);
    Zhiping updateStatus(String id, String status);
    Zhiping updateFeedback(String id, String feedbackLink, String feedbackImage);
    Zhiping updateFeedback(String id, String feedbackLink, String feedbackImage, String channel);
    void deleteZhiping(String id);
    String generateCode();
}