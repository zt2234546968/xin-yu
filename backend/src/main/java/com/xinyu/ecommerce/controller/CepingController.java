package com.xinyu.ecommerce.controller;

import com.xinyu.ecommerce.common.Result;
import com.xinyu.ecommerce.entity.Ceping;
import com.xinyu.ecommerce.service.CepingService;
import com.xinyu.ecommerce.service.CountryService;
import com.xinyu.ecommerce.entity.Country;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ceping")
public class CepingController {

    private final CepingService cepingService;
    private final CountryService countryService;

    @Autowired
    public CepingController(CepingService cepingService, CountryService countryService) {
        this.cepingService = cepingService;
        this.countryService = countryService;
    }

    @GetMapping("/list")
    public Result<List<Ceping>> list() {
        return Result.success(cepingService.getAllCepings());
    }

    @GetMapping("/getById")
    public Result<Ceping> getById(@RequestParam String id) {
        Ceping ceping = cepingService.getCepingById(id);
        if (ceping == null) {
            return Result.error(404, "Ceping not found");
        }
        return Result.success(ceping);
    }

    @PostMapping("/create")
    public Result<Ceping> create(@RequestBody Ceping ceping) {
        if (ceping.getCountry() == null && ceping.getCountryId() != null && !ceping.getCountryId().isEmpty()) {
            Country country = countryService.getCountryById(ceping.getCountryId());
            if (country != null) {
                ceping.setCountry(country);
            }
        }
        Ceping created = cepingService.createCeping(ceping);
        return Result.success("创建成功", created);
    }

    @PutMapping("/update")
    public Result<Ceping> update(@RequestParam String id, @RequestBody Ceping ceping) {
        Ceping updated = cepingService.updateCeping(id, ceping);
        if (updated == null) {
            return Result.error(404, "Ceping not found");
        }
        return Result.success("更新成功", updated);
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam String id) {
        cepingService.deleteCeping(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/generateCode")
    public Result<String> generateCode() {
        return Result.success(cepingService.generateCode());
    }
}
