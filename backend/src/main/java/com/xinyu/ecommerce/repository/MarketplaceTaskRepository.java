package com.xinyu.ecommerce.repository;

import com.xinyu.ecommerce.entity.MarketplaceTask;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketplaceTaskRepository extends JpaRepository<MarketplaceTask, String> {

    @EntityGraph(attributePaths = {"country"})
    List<MarketplaceTask> findByDeletedOrderByCreateTimeDesc(Integer deleted);

    @EntityGraph(attributePaths = {"country"})
    List<MarketplaceTask> findByTaskTypeAndDeletedOrderByCreateTimeDesc(String taskType, Integer deleted);

    @Override
    @EntityGraph(attributePaths = {"country"})
    Optional<MarketplaceTask> findById(String id);

    Optional<MarketplaceTask> findTopByTaskTypeAndCodeStartingWithOrderByCodeDesc(String taskType, String codePrefix);
}
