package com.xinyu.ecommerce.repository;

import com.xinyu.ecommerce.entity.OrderList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderListRepository extends JpaRepository<OrderList, String> {
    
    List<OrderList> findByCepingIdAndDeleted(String cepingId, Integer deleted);
    
    List<OrderList> findByCepingId(String cepingId);
    
    List<OrderList> findByStatusAndDeleted(String status, Integer deleted);
}
