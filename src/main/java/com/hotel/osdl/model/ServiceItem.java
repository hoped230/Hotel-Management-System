package com.hotel.osdl.model;

import java.io.Serializable;

public class ServiceItem implements Billable, Serializable {
    private String bookingId;
    private String name;
    private Integer quantity;
    private Double unitPrice;

    public ServiceItem(String bookingId, String name, Integer quantity, Double unitPrice) {
        this.bookingId = bookingId;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getName() {
        return name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    @Override
    public String getBillLabel() {
        return name + " x" + quantity;
    }

    @Override
    public double getBillAmount() {
        return Double.valueOf(quantity.intValue() * unitPrice.doubleValue()).doubleValue();
    }
}
