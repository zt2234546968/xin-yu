package com.xinyu.ecommerce.service;

import com.xinyu.ecommerce.entity.Country;

import java.util.List;

public interface CountryService {
    List<Country> getAllCountries();
    Country getCountryById(String id);
    Country getCountryByName(String countryName);
    Country createCountry(Country country);
    Country updateCountry(String id, Country country);
    void deleteCountry(String id);
    void initDefaultCountries();
}