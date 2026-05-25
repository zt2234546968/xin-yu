package com.xinyu.ecommerce.service.impl;

import com.xinyu.ecommerce.entity.Country;
import com.xinyu.ecommerce.repository.CountryRepository;
import com.xinyu.ecommerce.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private static final String PLACEHOLDER_IMAGE = "/test.jpg";
    private static final List<FixedCountry> FIXED_COUNTRIES = List.of(
            new FixedCountry("10000000-0000-0000-0000-000000000001", "美国", 1),
            new FixedCountry("10000000-0000-0000-0000-000000000002", "英国", 2),
            new FixedCountry("10000000-0000-0000-0000-000000000003", "德国", 3),
            new FixedCountry("10000000-0000-0000-0000-000000000004", "法国", 4),
            new FixedCountry("10000000-0000-0000-0000-000000000005", "意大利", 5),
            new FixedCountry("10000000-0000-0000-0000-000000000006", "西班牙", 6),
            new FixedCountry("10000000-0000-0000-0000-000000000007", "加拿大", 7),
            new FixedCountry("10000000-0000-0000-0000-000000000008", "日本", 8)
    );

    private final CountryRepository countryRepository;

    @Override
    public List<Country> getAllCountries() {
        initDefaultCountries();
        return countryRepository.findByDeletedOrderBySortOrderAscCountryNameAsc(0);
    }

    @Override
    public Country getCountryById(String id) {
        return countryRepository.findById(id)
                .filter(country -> Integer.valueOf(0).equals(country.getDeleted()))
                .orElseThrow(() -> new RuntimeException("国家不存在"));
    }

    @Override
    public Country getCountryByName(String countryName) {
        return countryRepository.findByCountryNameAndDeleted(countryName, 0);
    }

    @Override
    public Country createCountry(Country country) {
        throw new RuntimeException("国家为固定字典，不支持新增");
    }

    @Override
    public Country updateCountry(String id, Country country) {
        throw new RuntimeException("国家为固定字典，不支持修改");
    }

    @Override
    public void deleteCountry(String id) {
        throw new RuntimeException("国家为固定字典，不支持删除");
    }

    @Override
    public void initDefaultCountries() {
        for (FixedCountry fixedCountry : FIXED_COUNTRIES) {
            Country country = countryRepository.findByCountryName(fixedCountry.countryName());
            if (country == null) {
                country = new Country();
                country.setId(fixedCountry.id());
                country.setCountryName(fixedCountry.countryName());
            }
            country.setFlagImage(PLACEHOLDER_IMAGE);
            country.setSortOrder(fixedCountry.sortOrder());
            country.setDeleted(0);
            countryRepository.save(country);
        }

        List<String> fixedNames = FIXED_COUNTRIES.stream().map(FixedCountry::countryName).toList();
        countryRepository.findAll().stream()
                .filter(country -> !fixedNames.contains(country.getCountryName()))
                .forEach(country -> {
                    country.setDeleted(1);
                    countryRepository.save(country);
                });
    }

    private record FixedCountry(String id, String countryName, Integer sortOrder) {
    }
}
