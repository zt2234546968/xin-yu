package com.xinyu.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, nullable = false, unique = true)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String countryName;

    @Column(nullable = false, length = 255)
    private String flagImage;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 999;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer deleted = 0;
}
