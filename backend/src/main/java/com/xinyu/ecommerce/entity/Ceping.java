package com.xinyu.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ceping")
public class Ceping {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, nullable = false, unique = true)
    private String id;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "product_image")
    private String productImage;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_link")
    private String productLink;

    @Column(name = "country_id", insertable = false, updatable = false)
    private String countryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Country country;

    @Column(name = "asin", length = 50)
    private String asin;

    @Column(name = "review_title")
    private String reviewTitle;

    @Column(name = "review_content", columnDefinition = "TEXT")
    private String reviewContent;

    @Column(name = "shop")
    private String shop;

    @Column(name = "free_review", nullable = false)
    private Integer freeReview = 0;

    @Column(name = "star_review", nullable = false)
    private Integer starReview = 0;

    @Column(name = "text_review", nullable = false)
    private Integer textReview = 0;

    @Column(name = "image_review", nullable = false)
    private Integer imageReview = 0;

    @Column(name = "video_review", nullable = false)
    private Integer videoReview = 0;

    @Column(name = "feedback_review", nullable = false)
    private Integer feedbackReview = 0;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "is_positive")
    private Boolean isPositive = true;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "daily_quantity")
    private Integer dailyQuantity = 0;

    /**
     * 任务状态
     * 0 - 放单中（初始状态，可以添加订单）
     * 1 - 已放单（任务已发布等待接单）
     * 2 - 申请增加预算（用户申请增加预算，需要管理员审核）
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status = "0";

    @Column(name = "budget", precision = 10, scale = 2)
    private BigDecimal budget;

    @Column(name = "admin_budget", precision = 10, scale = 2)
    private BigDecimal adminBudget;

    @Column(name = "user_budget", precision = 10, scale = 2)
    private BigDecimal userBudget;

    @Column(name = "admin_message")
    private Boolean adminMessage = false;

    @Column(name = "user_message")
    private Boolean userMessage = false;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @OneToMany(mappedBy = "ceping", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ceping"})
    private List<OrderList> orderLists = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "deleted", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer deleted = 0;

    public void calculateTotalQuantity() {
        this.totalQuantity = this.freeReview + this.starReview + this.textReview + 
                            this.imageReview + this.videoReview + this.feedbackReview;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductLink() {
        return productLink;
    }

    public void setProductLink(String productLink) {
        this.productLink = productLink;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getReviewTitle() {
        return reviewTitle;
    }

    public void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public Integer getFreeReview() {
        return freeReview;
    }

    public void setFreeReview(Integer freeReview) {
        this.freeReview = freeReview;
    }

    public Integer getStarReview() {
        return starReview;
    }

    public void setStarReview(Integer starReview) {
        this.starReview = starReview;
    }

    public Integer getTextReview() {
        return textReview;
    }

    public void setTextReview(Integer textReview) {
        this.textReview = textReview;
    }

    public Integer getImageReview() {
        return imageReview;
    }

    public void setImageReview(Integer imageReview) {
        this.imageReview = imageReview;
    }

    public Integer getVideoReview() {
        return videoReview;
    }

    public void setVideoReview(Integer videoReview) {
        this.videoReview = videoReview;
    }

    public Integer getFeedbackReview() {
        return feedbackReview;
    }

    public void setFeedbackReview(Integer feedbackReview) {
        this.feedbackReview = feedbackReview;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Boolean getIsPositive() {
        return isPositive;
    }

    public void setIsPositive(Boolean positive) {
        isPositive = positive;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getDailyQuantity() {
        return dailyQuantity;
    }

    public void setDailyQuantity(Integer dailyQuantity) {
        this.dailyQuantity = dailyQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getAdminBudget() {
        return adminBudget;
    }

    public void setAdminBudget(BigDecimal adminBudget) {
        this.adminBudget = adminBudget;
    }

    public BigDecimal getUserBudget() {
        return userBudget;
    }

    public void setUserBudget(BigDecimal userBudget) {
        this.userBudget = userBudget;
    }

    public Boolean getAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(Boolean adminMessage) {
        this.adminMessage = adminMessage;
    }

    public Boolean getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(Boolean userMessage) {
        this.userMessage = userMessage;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public List<OrderList> getOrderLists() {
        return orderLists;
    }

    public void setOrderLists(List<OrderList> orderLists) {
        this.orderLists = orderLists;
    }
}
