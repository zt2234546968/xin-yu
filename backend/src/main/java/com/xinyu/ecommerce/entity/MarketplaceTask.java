package com.xinyu.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "marketplace_task")
public class MarketplaceTask {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, nullable = false, unique = true)
    private String id;

    @Column(name = "task_type", nullable = false, length = 80)
    private String taskType;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Country country;

    @Column(name = "country_id", insertable = false, updatable = false)
    private String countryId;

    @Column(length = 50)
    private String asin;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "product_link", length = 500)
    private String productLink;

    @Column(name = "task_image", nullable = false, length = 255)
    private String taskImage = "/test.jpg";

    @Column(length = 255)
    private String shop;

    @Column(length = 255)
    private String keyword;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false, length = 20)
    private String priority = "NORMAL";

    @Column(name = "issue_type", length = 100)
    private String issueType;

    @Column(name = "target_action", length = 100)
    private String targetAction;

    @Column(name = "claim_reason", columnDefinition = "TEXT")
    private String claimReason;

    @Column(name = "evidence_link", length = 500)
    private String evidenceLink;

    @Column(name = "evidence_image", length = 255)
    private String evidenceImage;

    @Column(length = 255)
    private String channel;

    @Column(nullable = false, length = 50)
    private String status = "0";

    @Column(name = "feedback_link", length = 500)
    private String feedbackLink;

    @Column(name = "feedback_image", length = 255)
    private String feedbackImage;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer deleted = 0;
}
