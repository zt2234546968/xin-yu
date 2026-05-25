package com.xinyu.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "zhiping")
public class Zhiping {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, nullable = false, unique = true)
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String asin;

    @Column(nullable = false, length = 255)
    private String reviewTitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reviewContent;

    @Column(nullable = false, length = 255)
    private String taskImage;

    @Column(nullable = false)
    private Integer starRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Country country;

    @Column(name = "country_id", insertable = false, updatable = false)
    private String countryId;

    @Column(name = "channel", length = 255)
    private String channel;

    @Column(name = "keyword", length = 255)
    private String keyword;

    @Column(name = "daily_quantity")
    private Integer dailyQuantity;

    @Column(name = "warranty_time", nullable = false, length = 50)
    private String warrantyTime = "0";

    @Column(nullable = false, length = 50)
    private String status = "0";

    @Column(length = 255)
    private String feedbackLink;

    @Column(length = 255)
    private String feedbackImage;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer deleted = 0;
}