package com.xinyu.ecommerce.service.impl;

import com.xinyu.ecommerce.entity.Ceping;
import com.xinyu.ecommerce.entity.Country;
import com.xinyu.ecommerce.entity.OrderList;
import com.xinyu.ecommerce.repository.CepingRepository;
import com.xinyu.ecommerce.repository.OrderListRepository;
import com.xinyu.ecommerce.service.CepingService;
import com.xinyu.ecommerce.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CepingServiceImpl implements CepingService {

    private final CepingRepository cepingRepository;
    private final CountryService countryService;
    private final OrderListRepository orderListRepository;

    @Autowired
    public CepingServiceImpl(CepingRepository cepingRepository, CountryService countryService, OrderListRepository orderListRepository) {
        this.cepingRepository = cepingRepository;
        this.countryService = countryService;
        this.orderListRepository = orderListRepository;
    }

    @Override
    public List<Ceping> getAllCepings() {
        return cepingRepository.findByDeletedIsFalse();
    }

    @Override
    public Ceping getCepingById(String id) {
        return cepingRepository.findById(id).orElse(null);
    }

    @Override
    public Ceping createCeping(Ceping ceping) {
        if (ceping.getCode() == null || ceping.getCode().isEmpty()) {
            ceping.setCode(generateCode());
        }
        if (ceping.getCountry() == null && ceping.getCountryId() != null && !ceping.getCountryId().isEmpty()) {
            Country country = countryService.getCountryById(ceping.getCountryId());
            if (country != null) {
                ceping.setCountry(country);
            }
        }
        if (ceping.getStatus() == null) {
            ceping.setStatus("0");
        }
        ceping.calculateTotalQuantity();
        return cepingRepository.save(ceping);
    }

    @Override
    @Transactional
    public Ceping updateCeping(String id, Ceping ceping) {
        Ceping existingCeping = cepingRepository.findById(id).orElse(null);
        if (existingCeping != null) {
            if (ceping.getProductImage() != null) {
                existingCeping.setProductImage(ceping.getProductImage());
            }
            if (ceping.getProductName() != null) {
                existingCeping.setProductName(ceping.getProductName());
            }
            if (ceping.getProductLink() != null) {
                existingCeping.setProductLink(ceping.getProductLink());
            }
            if (ceping.getAsin() != null) {
                existingCeping.setAsin(ceping.getAsin());
            }
            if (ceping.getReviewTitle() != null) {
                existingCeping.setReviewTitle(ceping.getReviewTitle());
            }
            if (ceping.getReviewContent() != null) {
                existingCeping.setReviewContent(ceping.getReviewContent());
            }
            if (ceping.getShop() != null) {
                existingCeping.setShop(ceping.getShop());
            }
            if (ceping.getFreeReview() != null) {
                existingCeping.setFreeReview(ceping.getFreeReview());
            }
            if (ceping.getStarReview() != null) {
                existingCeping.setStarReview(ceping.getStarReview());
            }
            if (ceping.getTextReview() != null) {
                existingCeping.setTextReview(ceping.getTextReview());
            }
            if (ceping.getImageReview() != null) {
                existingCeping.setImageReview(ceping.getImageReview());
            }
            if (ceping.getVideoReview() != null) {
                existingCeping.setVideoReview(ceping.getVideoReview());
            }
            if (ceping.getFeedbackReview() != null) {
                existingCeping.setFeedbackReview(ceping.getFeedbackReview());
            }
            if (ceping.getIsPositive() != null) {
                existingCeping.setIsPositive(ceping.getIsPositive());
            }
            if (ceping.getPrice() != null) {
                existingCeping.setPrice(ceping.getPrice());
            }
            if (ceping.getKeyword() != null) {
                existingCeping.setKeyword(ceping.getKeyword());
            }
            if (ceping.getDailyQuantity() != null) {
                existingCeping.setDailyQuantity(ceping.getDailyQuantity());
            }
            if (ceping.getStatus() != null) {
                existingCeping.setStatus(ceping.getStatus());
            }
            if (ceping.getBudget() != null) {
                existingCeping.setBudget(ceping.getBudget());
            }
            if (ceping.getAdminBudget() != null) {
                existingCeping.setAdminBudget(ceping.getAdminBudget());
            }
            if (ceping.getUserBudget() != null || ceping.getUserBudget() != null && ceping.getUserBudget().compareTo(BigDecimal.ZERO) == 0 || ceping.getAdminBudget() != null) {
                existingCeping.setUserBudget(ceping.getUserBudget());
            }
            if (ceping.getAdminMessage() != null) {
                existingCeping.setAdminMessage(ceping.getAdminMessage());
            }
            if (ceping.getUserMessage() != null) {
                existingCeping.setUserMessage(ceping.getUserMessage());
            }
            if (ceping.getRemark() != null) {
                existingCeping.setRemark(ceping.getRemark());
            }
            if (ceping.getCountry() != null) {
                existingCeping.setCountry(ceping.getCountry());
            }
            existingCeping.calculateTotalQuantity();
            cepingRepository.save(existingCeping);

            if (ceping.getOrderLists() != null && !ceping.getOrderLists().isEmpty()) {
                for (OrderList orderItem : ceping.getOrderLists()) {
                    if (orderItem.getOrderNumber() != null && !orderItem.getOrderNumber().isEmpty()) {
                        OrderList order = new OrderList();
                        order.setCeping(existingCeping);
                        order.setOrderNumber(orderItem.getOrderNumber());
                        order.setOrderScreenshot(orderItem.getOrderScreenshot());
                        order.setStatus("0");
                        orderListRepository.save(order);
                    }
                }
            }

            return cepingRepository.findById(id).orElse(null);
        }
        return null;
    }

    @Override
    public void deleteCeping(String id) {
        Ceping ceping = cepingRepository.findById(id).orElse(null);
        if (ceping != null) {
            ceping.setDeleted(1);
            cepingRepository.save(ceping);
        }
    }

    @Override
    public String generateCode() {
        List<Ceping> allCepings = cepingRepository.findAll();
        int maxCode = 999;
        for (Ceping ceping : allCepings) {
            String code = ceping.getCode();
            if (code != null && code.startsWith("CP")) {
                try {
                    int num = Integer.parseInt(code.substring(2));
                    if (num > maxCode) {
                        maxCode = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "CP" + (maxCode + 1);
    }
}
