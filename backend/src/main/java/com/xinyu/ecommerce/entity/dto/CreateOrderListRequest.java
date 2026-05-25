package com.xinyu.ecommerce.entity.dto;

public class CreateOrderListRequest {
    private String cepingId;
    private String orderNumber;
    private String orderScreenshot;

    public String getCepingId() {
        return cepingId;
    }

    public void setCepingId(String cepingId) {
        this.cepingId = cepingId;
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

    @Override
    public String toString() {
        return "CreateOrderListRequest{" +
                "cepingId='" + cepingId + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", orderScreenshot='" + (orderScreenshot != null ? orderScreenshot.substring(0, Math.min(50, orderScreenshot.length())) + "..." : null) + '\'' +
                '}';
    }
}
