package com.xinyu.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_list")
public class OrderList {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, nullable = false, unique = true)
    private String id;

    @Column(name = "ceping_id", insertable = false, updatable = false)
    @JsonProperty("cepingId")
    private String cepingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceping_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "orderLists"})
    private Ceping ceping;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "order_screenshot")
    private String orderScreenshot;

    @Column(name = "review_link")
    private String reviewLink;

    @Column(name = "review_screenshot")
    private String reviewScreenshot;

    @Column(name = "expense_detail", columnDefinition = "TEXT")
    private String expenseDetail;

    @Column(name = "principal", precision = 10, scale = 2)
    private BigDecimal principal;

    @Column(name = "pp_multiplier", precision = 5, scale = 4)
    private BigDecimal ppMultiplier;

    @Column(name = "exchange_rate", precision = 10, scale = 4)
    private BigDecimal exchangeRate;

    @Column(name = "exchange_rate_add", precision = 10, scale = 2)
    private BigDecimal exchangeRateAdd;

    @Column(name = "commission", precision = 10, scale = 2)
    private BigDecimal commission;

    @Column(name = "pp_price", precision = 10, scale = 2)
    private BigDecimal ppPrice;

    @Column(name = "sum", precision = 10, scale = 2)
    private BigDecimal sum;

    /**
     * 订单状态
     * 0 - 已放单（初始状态，任务已发布等待接单）
     * 1 - 已完成（任务完成，已提交订单截图）
     */
    @Column(name = "status", length = 50)
    private String status = "0";

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "deleted", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer deleted = 0;

    public void calculatePpPrice() {
        if (principal != null && ppMultiplier != null) {
            BigDecimal multiplierPart = principal.multiply(ppMultiplier);
            BigDecimal addPart = exchangeRateAdd != null ? exchangeRateAdd : BigDecimal.ZERO;
            this.ppPrice = multiplierPart.add(addPart).add(principal);
        }
    }

    public void calculateSum() {
        if (ppPrice != null && exchangeRate != null) {
            BigDecimal exchangePart = ppPrice.multiply(exchangeRate);
            BigDecimal commissionPart = commission != null ? commission : BigDecimal.ZERO;
            this.sum = exchangePart.add(commissionPart);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCepingId() {
        return cepingId;
    }

    public void setCepingId(String cepingId) {
        this.cepingId = cepingId;
    }

    public Ceping getCeping() {
        return ceping;
    }

    public void setCeping(Ceping ceping) {
        this.ceping = ceping;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderScreenshot() {
        return orderScreenshot;
    }

    public void setOrderScreenshot(String orderScreenshot) {
        this.orderScreenshot = orderScreenshot;
    }

    public String getReviewLink() {
        return reviewLink;
    }

    public void setReviewLink(String reviewLink) {
        this.reviewLink = reviewLink;
    }

    public String getReviewScreenshot() {
        return reviewScreenshot;
    }

    public void setReviewScreenshot(String reviewScreenshot) {
        this.reviewScreenshot = reviewScreenshot;
    }

    public String getExpenseDetail() {
        return expenseDetail;
    }

    public void setExpenseDetail(String expenseDetail) {
        this.expenseDetail = expenseDetail;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getPpMultiplier() {
        return ppMultiplier;
    }

    public void setPpMultiplier(BigDecimal ppMultiplier) {
        this.ppMultiplier = ppMultiplier;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public BigDecimal getExchangeRateAdd() {
        return exchangeRateAdd;
    }

    public void setExchangeRateAdd(BigDecimal exchangeRateAdd) {
        this.exchangeRateAdd = exchangeRateAdd;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public BigDecimal getPpPrice() {
        return ppPrice;
    }

    public void setPpPrice(BigDecimal ppPrice) {
        this.ppPrice = ppPrice;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}