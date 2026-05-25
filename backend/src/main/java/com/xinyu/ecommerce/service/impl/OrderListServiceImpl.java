package com.xinyu.ecommerce.service.impl;

import com.xinyu.ecommerce.entity.Ceping;
import com.xinyu.ecommerce.entity.OrderList;
import com.xinyu.ecommerce.repository.CepingRepository;
import com.xinyu.ecommerce.repository.OrderListRepository;
import com.xinyu.ecommerce.service.OrderListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderListServiceImpl implements OrderListService {

    @Autowired
    private OrderListRepository orderListRepository;

    @Autowired
    private CepingRepository cepingRepository;

    @Override
    @Transactional
    public OrderList create(OrderList orderList) {
        orderList.setDeleted(0);
        
        if (orderList.getCepingId() != null && !orderList.getCepingId().isEmpty()) {
            Ceping ceping = cepingRepository.findById(orderList.getCepingId()).orElse(null);
            if (ceping != null) {
                orderList.setCeping(ceping);
            }
        }
        
        return orderListRepository.save(orderList);
    }

    @Override
    public OrderList getById(String id) {
        return orderListRepository.findById(id).orElse(null);
    }

    @Override
    public List<OrderList> getByCepingId(String cepingId) {
        return orderListRepository.findByCepingIdAndDeleted(cepingId, 0);
    }

    @Override
    public List<OrderList> getAll() {
        return orderListRepository.findAll().stream()
                .filter(order -> order.getDeleted() == null || order.getDeleted() == 0)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public OrderList update(OrderList orderList) {
        OrderList existing = orderListRepository.findById(orderList.getId()).orElse(null);
        if (existing != null) {
            existing.setOrderNumber(orderList.getOrderNumber());
            existing.setOrderScreenshot(orderList.getOrderScreenshot());
            existing.setReviewLink(orderList.getReviewLink());
            existing.setReviewScreenshot(orderList.getReviewScreenshot());
            existing.setExpenseDetail(orderList.getExpenseDetail());
            
            if (orderList.getPrincipal() != null) {
                existing.setPrincipal(orderList.getPrincipal());
            }
            if (orderList.getPpMultiplier() != null) {
                existing.setPpMultiplier(orderList.getPpMultiplier());
            }
            if (orderList.getExchangeRate() != null) {
                existing.setExchangeRate(orderList.getExchangeRate());
            }
            if (orderList.getExchangeRateAdd() != null) {
                existing.setExchangeRateAdd(orderList.getExchangeRateAdd());
            }
            if (orderList.getCommission() != null) {
                existing.setCommission(orderList.getCommission());
            }
            if (orderList.getPpPrice() != null) {
                existing.setPpPrice(orderList.getPpPrice());
            }
            if (orderList.getSum() != null) {
                existing.setSum(orderList.getSum());
            }
            if (orderList.getStatus() != null) {
                existing.setStatus(orderList.getStatus());
            }
            
            existing.calculatePpPrice();
            existing.calculateSum();
            
            return orderListRepository.save(existing);
        }
        return null;
    }

    @Override
    @Transactional
    public void delete(String id) {
        OrderList orderList = orderListRepository.findById(id).orElse(null);
        if (orderList != null) {
            orderList.setDeleted(1);
            orderListRepository.save(orderList);
        }
    }
}
