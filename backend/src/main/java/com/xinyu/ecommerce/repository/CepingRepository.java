package com.xinyu.ecommerce.repository;

import com.xinyu.ecommerce.entity.Ceping;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CepingRepository extends JpaRepository<Ceping, String> {

    @EntityGraph(attributePaths = {"country"})
    List<Ceping> findByDeletedIsFalse();
}
