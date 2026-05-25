package com.xinyu.ecommerce.repository;

import com.xinyu.ecommerce.entity.Zhiping;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZhipingRepository extends JpaRepository<Zhiping, String> {
    
    @Override
    @EntityGraph(attributePaths = {"country", "channel"})
    List<Zhiping> findAll();
    
    @Override
    @EntityGraph(attributePaths = {"country", "channel"})
    Optional<Zhiping> findById(String id);
}