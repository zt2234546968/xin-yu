package com.xinyu.ecommerce.service.impl;

import com.xinyu.ecommerce.entity.Country;
import com.xinyu.ecommerce.entity.MarketplaceTask;
import com.xinyu.ecommerce.repository.MarketplaceTaskRepository;
import com.xinyu.ecommerce.service.CountryService;
import com.xinyu.ecommerce.service.MarketplaceTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarketplaceTaskServiceImpl implements MarketplaceTaskService {

    private static final Map<String, String> CODE_PREFIXES = new LinkedHashMap<>();

    static {
        CODE_PREFIXES.put("vp_negative", "VPN");
        CODE_PREFIXES.put("counter_adult", "CA");
        CODE_PREFIXES.put("counter_split_variant", "CSV");
        CODE_PREFIXES.put("counter_copyright_image", "CCI");
        CODE_PREFIXES.put("counter_authenticity_complaint", "CAC");
        CODE_PREFIXES.put("counter_authenticity_vp_negative", "CAV");
        CODE_PREFIXES.put("counter_product_safety", "CPS");
        CODE_PREFIXES.put("counter_dog", "CD");
        CODE_PREFIXES.put("buyer_show", "BS");
        CODE_PREFIXES.put("other_business", "OB");
    }

    private final MarketplaceTaskRepository marketplaceTaskRepository;
    private final CountryService countryService;

    @Override
    public List<MarketplaceTask> getTasks(String taskType) {
        if (StringUtils.hasText(taskType)) {
            return marketplaceTaskRepository.findByTaskTypeAndDeletedOrderByCreateTimeDesc(taskType, 0);
        }
        return marketplaceTaskRepository.findByDeletedOrderByCreateTimeDesc(0);
    }

    @Override
    public MarketplaceTask getTaskById(String id) {
        return marketplaceTaskRepository.findById(id)
                .filter(task -> task.getDeleted() == null || task.getDeleted() == 0)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Override
    public MarketplaceTask createTask(MarketplaceTask task) {
        normalizeTask(task);
        if (!StringUtils.hasText(task.getCode())) {
            task.setCode(generateCode(task.getTaskType()));
        }
        if (!StringUtils.hasText(task.getStatus())) {
            task.setStatus("0");
        }
        if (!StringUtils.hasText(task.getPriority())) {
            task.setPriority("NORMAL");
        }
        if (!StringUtils.hasText(task.getTaskImage())) {
            task.setTaskImage("/test.jpg");
        }
        if (task.getQuantity() == null || task.getQuantity() < 1) {
            task.setQuantity(1);
        }
        return marketplaceTaskRepository.save(task);
    }

    @Override
    public MarketplaceTask updateTask(String id, MarketplaceTask task) {
        MarketplaceTask existing = getTaskById(id);
        updateIfPresent(existing, task);
        bindCountry(existing, task.getCountryId());
        return marketplaceTaskRepository.save(existing);
    }

    @Override
    public MarketplaceTask updateStatus(String id, String status) {
        MarketplaceTask existing = getTaskById(id);
        existing.setStatus(status);
        return marketplaceTaskRepository.save(existing);
    }

    @Override
    public MarketplaceTask updateFeedback(String id, String feedbackLink, String feedbackImage, String channel) {
        MarketplaceTask existing = getTaskById(id);
        if (feedbackLink != null) {
            existing.setFeedbackLink(feedbackLink);
        }
        if (feedbackImage != null) {
            existing.setFeedbackImage(feedbackImage);
        }
        if (StringUtils.hasText(channel)) {
            existing.setChannel(channel);
        }
        return marketplaceTaskRepository.save(existing);
    }

    @Override
    public void deleteTask(String id) {
        MarketplaceTask existing = getTaskById(id);
        existing.setDeleted(1);
        marketplaceTaskRepository.save(existing);
    }

    @Override
    public String generateCode(String taskType) {
        String prefix = getPrefix(taskType);
        return marketplaceTaskRepository
                .findTopByTaskTypeAndCodeStartingWithOrderByCodeDesc(taskType, prefix)
                .map(task -> nextCode(prefix, task.getCode()))
                .orElse(prefix + "1000");
    }

    private void normalizeTask(MarketplaceTask task) {
        if (!StringUtils.hasText(task.getTaskType())) {
            throw new RuntimeException("Task type is required");
        }
        if (!CODE_PREFIXES.containsKey(task.getTaskType())) {
            throw new RuntimeException("Unsupported task type: " + task.getTaskType());
        }
        bindCountry(task, task.getCountryId());
    }

    private void bindCountry(MarketplaceTask task, String countryId) {
        if (StringUtils.hasText(countryId)) {
            Country country = countryService.getCountryById(countryId);
            task.setCountry(country);
        }
    }

    private void updateIfPresent(MarketplaceTask existing, MarketplaceTask incoming) {
        if (StringUtils.hasText(incoming.getTaskType())) existing.setTaskType(incoming.getTaskType());
        if (StringUtils.hasText(incoming.getAsin())) existing.setAsin(incoming.getAsin());
        if (StringUtils.hasText(incoming.getProductName())) existing.setProductName(incoming.getProductName());
        if (StringUtils.hasText(incoming.getProductLink())) existing.setProductLink(incoming.getProductLink());
        if (StringUtils.hasText(incoming.getTaskImage())) existing.setTaskImage(incoming.getTaskImage());
        if (StringUtils.hasText(incoming.getShop())) existing.setShop(incoming.getShop());
        if (StringUtils.hasText(incoming.getKeyword())) existing.setKeyword(incoming.getKeyword());
        if (incoming.getQuantity() != null) existing.setQuantity(incoming.getQuantity());
        if (StringUtils.hasText(incoming.getPriority())) existing.setPriority(incoming.getPriority());
        if (StringUtils.hasText(incoming.getIssueType())) existing.setIssueType(incoming.getIssueType());
        if (StringUtils.hasText(incoming.getTargetAction())) existing.setTargetAction(incoming.getTargetAction());
        if (StringUtils.hasText(incoming.getClaimReason())) existing.setClaimReason(incoming.getClaimReason());
        if (StringUtils.hasText(incoming.getEvidenceLink())) existing.setEvidenceLink(incoming.getEvidenceLink());
        if (StringUtils.hasText(incoming.getEvidenceImage())) existing.setEvidenceImage(incoming.getEvidenceImage());
        if (StringUtils.hasText(incoming.getChannel())) existing.setChannel(incoming.getChannel());
        if (StringUtils.hasText(incoming.getStatus())) existing.setStatus(incoming.getStatus());
        if (incoming.getFeedbackLink() != null) existing.setFeedbackLink(incoming.getFeedbackLink());
        if (incoming.getFeedbackImage() != null) existing.setFeedbackImage(incoming.getFeedbackImage());
        if (StringUtils.hasText(incoming.getRemark())) existing.setRemark(incoming.getRemark());
    }

    private String getPrefix(String taskType) {
        String prefix = CODE_PREFIXES.get(taskType);
        if (prefix == null) {
            throw new RuntimeException("Unsupported task type: " + taskType);
        }
        return prefix;
    }

    private String nextCode(String prefix, String code) {
        try {
            int current = Integer.parseInt(code.substring(prefix.length()));
            return prefix + (current + 1);
        } catch (RuntimeException e) {
            return prefix + "1000";
        }
    }
}
