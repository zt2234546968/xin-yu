package com.xinyu.ecommerce.controller;

import com.xinyu.ecommerce.common.Result;
import com.xinyu.ecommerce.entity.Country;
import com.xinyu.ecommerce.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/country")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping("/list")
    @Operation(summary = "获取所有国家", description = "获取所有国家列表")
    public Result<List<Country>> list() {
        return Result.success(countryService.getAllCountries());
    }

    @GetMapping("/getById")
    @Operation(summary = "获取国家详情", description = "根据ID获取国家详情")
    public Result<Country> getById(@RequestParam String id) {
        try {
            return Result.success(countryService.getCountryById(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/getByName")
    @Operation(summary = "根据名称获取国家", description = "根据国家名称获取国家详情")
    public Result<Country> getByName(@RequestParam String countryName) {
        Country country = countryService.getCountryByName(countryName);
        if (country != null) {
            return Result.success(country);
        } else {
            return Result.error("国家不存在");
        }
    }

    @PostMapping("/create")
    @Operation(summary = "创建国家", description = "创建新的国家")
    public Result<Country> create(@RequestBody Country country) {
        try {
            Country created = countryService.createCountry(country);
            return Result.success("创建成功", created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新国家", description = "根据ID更新国家信息")
    public Result<Country> update(@RequestParam String id, @RequestBody Country country) {
        try {
            Country updated = countryService.updateCountry(id, country);
            return Result.success("更新成功", updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除国家", description = "根据ID删除国家")
    public Result<Void> delete(@RequestParam String id) {
        try {
            countryService.deleteCountry(id);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/init")
    @Operation(summary = "初始化默认国家", description = "初始化默认的8个国家数据")
    public Result<Void> init() {
        try {
            countryService.initDefaultCountries();
            return Result.success("初始化成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}