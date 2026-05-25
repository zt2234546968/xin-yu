package com.xinyu.ecommerce;

import com.xinyu.ecommerce.service.CountryService;
import com.xinyu.ecommerce.service.RoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
        System.out.println("====================================");
        System.out.println("Cross-border E-commerce System Backend Started!");
        System.out.println("====================================");
    }

    @Bean
    public CommandLineRunner initData(RoleService roleService, CountryService countryService) {
        return args -> {
            // Initialize default roles
            roleService.initDefaultRoles();
            // Initialize super admin
            roleService.initSuperAdmin();
            // Initialize default countries
            countryService.initDefaultCountries();
            System.out.println("====================================");
            System.out.println("Default roles, super admin and countries initialized!");
            System.out.println("====================================");
        };
    }
}