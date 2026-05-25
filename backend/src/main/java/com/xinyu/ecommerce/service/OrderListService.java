package com.xinyu.ecommerce.service;

import com.xinyu.ecommerce.entity.OrderList;

import java.util.List;

public interface OrderListService {
    
    OrderList create(OrderList orderList);
    
    OrderList getById(String id);
    
    List<OrderList> getByCepingId(String cepingId);
    
    List<OrderList> getAll();
    
    OrderList update(OrderList orderList);
    
    void delete(String id);
}
