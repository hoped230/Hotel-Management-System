package com.hotel.osdl.model;

import java.io.Serializable;

// Feature used: abstract class, constructor overloading, encapsulation and abstraction.
public abstract class Room implements Serializable, Billable {
    private Integer roomNumber;
    private RoomType roomType;
    private Double basePrice;
    private Boolean booked;
    private String guestName;

    public Room(Integer roomNumber, RoomType roomType) {
        this(roomNumber, roomType, roomType.getBaseTariff(), Boolean.FALSE, "");
    }

    public Room(Integer roomNumber, RoomType roomType, Double basePrice) {
        this(roomNumber, roomType, basePrice, Boolean.FALSE, "");
    }

    public Room(Integer roomNumber, RoomType roomType, Double basePrice, Boolean booked, String guestName) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.basePrice = basePrice;
        this.booked = booked;
        this.guestName = guestName;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        if (basePrice != null && basePrice.doubleValue() > 0) {
            this.basePrice = basePrice;
        }
    }

    public Boolean getBooked() {
        return booked;
    }

    public void setBooked(Boolean booked) {
        this.booked = booked;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public abstract Double calculateTariff(Integer days);

    public String displayRoomDetails() {
        return "Room " + roomNumber + " - " + roomType + " - Rs." + String.format("%.2f", basePrice.doubleValue());
    }

    @Override
    public String getBillLabel() {
        return "Room " + roomNumber + " stay";
    }

    @Override
    public double getBillAmount() {
        return basePrice.doubleValue();
    }

    @Override
    public String toString() {
        return roomNumber + " - " + roomType + " (Rs." + String.format("%.0f", basePrice.doubleValue()) + "/night)";
    }
}
