package com.xinyu.ecommerce.repository;

import com.xinyu.ecommerce.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, String> {
    Country findByCountryName(String countryName);

    Country findByCountryNameAndDeleted(String countryName, Integer deleted);

    List<Country> findByDeletedOrderBySortOrderAscCountryNameAsc(Integer deleted);
}
