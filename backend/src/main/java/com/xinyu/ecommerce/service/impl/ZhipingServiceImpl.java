package com.xinyu.ecommerce.service.impl;

import com.xinyu.ecommerce.entity.Country;
import com.xinyu.ecommerce.entity.Zhiping;
import com.xinyu.ecommerce.repository.ZhipingRepository;
import com.xinyu.ecommerce.service.CountryService;
import com.xinyu.ecommerce.service.ZhipingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ZhipingServiceImpl implements ZhipingService {

    private final ZhipingRepository zhipingRepository;
    private final CountryService countryService;

    @Override
    public List<Zhiping> getAllZhipings() {
        return zhipingRepository.findAll();
    }

    @Override
    public Zhiping getZhipingById(String id) {
        return zhipingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("直评任务不存在"));
    }

    @Override
    public Zhiping createZhiping(Zhiping zhiping) {
        // 生成编码
        if (zhiping.getCode() == null || zhiping.getCode().isEmpty()) {
            zhiping.setCode(generateCode());
        }
        // 设置默认状态为等待提交
        if (zhiping.getStatus() == null) {
            zhiping.setStatus("等待提交");
        }
        // 设置默认质保时间为7天
        if (zhiping.getWarrantyTime() == null || zhiping.getWarrantyTime().isEmpty()) {
            zhiping.setWarrantyTime("0");
        }
        // 根据countryId获取国家对象
        if (zhiping.getCountryId() != null && !zhiping.getCountryId().isEmpty()) {
            Country country = countryService.getCountryById(zhiping.getCountryId());
            zhiping.setCountry(country);
        }
        return zhipingRepository.save(zhiping);
    }

    @Override
    public Zhiping updateZhiping(String id, Zhiping zhiping) {
        Zhiping existingZhiping = getZhipingById(id);
        // 只更新非null的字段
        if (zhiping.getAsin() != null && !zhiping.getAsin().isEmpty()) {
            existingZhiping.setAsin(zhiping.getAsin());
        }
        if (zhiping.getReviewTitle() != null && !zhiping.getReviewTitle().isEmpty()) {
            existingZhiping.setReviewTitle(zhiping.getReviewTitle());
        }
        if (zhiping.getReviewContent() != null && !zhiping.getReviewContent().isEmpty()) {
            existingZhiping.setReviewContent(zhiping.getReviewContent());
        }
        if (zhiping.getTaskImage() != null && !zhiping.getTaskImage().isEmpty()) {
            existingZhiping.setTaskImage(zhiping.getTaskImage());
        }
        if (zhiping.getStarRating() != null) {
            existingZhiping.setStarRating(zhiping.getStarRating());
        }
        // 根据countryId获取国家对象
        if (zhiping.getCountryId() != null && !zhiping.getCountryId().isEmpty()) {
            Country country = countryService.getCountryById(zhiping.getCountryId());
            existingZhiping.setCountry(country);
        }
        if (zhiping.getChannel() != null && !zhiping.getChannel().isEmpty()) {
            existingZhiping.setChannel(zhiping.getChannel());
        }
        if (zhiping.getStatus() != null && !zhiping.getStatus().isEmpty()) {
            existingZhiping.setStatus(zhiping.getStatus());
        }
        if (zhiping.getWarrantyTime() != null && !zhiping.getWarrantyTime().isEmpty()) {
            existingZhiping.setWarrantyTime(zhiping.getWarrantyTime());
        }
        if (zhiping.getFeedbackLink() != null) {
            existingZhiping.setFeedbackLink(zhiping.getFeedbackLink());
        }
        if (zhiping.getFeedbackImage() != null) {
            existingZhiping.setFeedbackImage(zhiping.getFeedbackImage());
        }
        return zhipingRepository.save(existingZhiping);
    }

    @Override
    public Zhiping updateFeedback(String id, String feedbackLink, String feedbackImage) {
        Zhiping existingZhiping = getZhipingById(id);
        existingZhiping.setFeedbackLink(feedbackLink);
        existingZhiping.setFeedbackImage(feedbackImage);
        return zhipingRepository.save(existingZhiping);
    }

    @Override
    public Zhiping updateFeedback(String id, String feedbackLink, String feedbackImage, String channel) {
        System.out.println("========== updateFeedback ==========");
        System.out.println("id: " + id);
        System.out.println("feedbackLink: " + feedbackLink);
        System.out.println("feedbackImage: " + feedbackImage);
        System.out.println("channel: " + channel);
        
        Zhiping existingZhiping = getZhipingById(id);
        
        if (feedbackLink != null) {
            existingZhiping.setFeedbackLink(feedbackLink);
        }
        if (feedbackImage != null) {
            existingZhiping.setFeedbackImage(feedbackImage);
        }
        
        // 更新渠道
        if (channel != null && !channel.isEmpty()) {
            existingZhiping.setChannel(channel);
        }
        
        Zhiping savedZhiping = zhipingRepository.save(existingZhiping);
        
        System.out.println("Saved zhiping - channel: " + savedZhiping.getChannel());
        System.out.println("====================================");
        
        return savedZhiping;
    }

    @Override
    public Zhiping updateStatus(String id, String status) {
        Zhiping existingZhiping = getZhipingById(id);
        existingZhiping.setStatus(status);
        return zhipingRepository.save(existingZhiping);
    }

    @Override
    public void deleteZhiping(String id) {
        zhipingRepository.deleteById(id);
    }

    @Override
    public String generateCode() {
        List<Zhiping> allZhipings = zhipingRepository.findAll();
        int maxCode = 999;
        for (Zhiping zhiping : allZhipings) {
            String code = zhiping.getCode();
            if (code != null && code.startsWith("ZP")) {
                try {
                    int num = Integer.parseInt(code.substring(2));
                    if (num > maxCode) {
                        maxCode = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "ZP" + (maxCode + 1);
    }
}