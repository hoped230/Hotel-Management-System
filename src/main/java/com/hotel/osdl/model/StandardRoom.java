package com.hotel.osdl.model;

public class StandardRoom extends Room implements Amenities {
    public StandardRoom(Integer roomNumber) {
        super(roomNumber, RoomType.STANDARD);
    }

    public StandardRoom(Integer roomNumber, Double basePrice) {
        super(roomNumber, RoomType.STANDARD, basePrice);
    }

    public StandardRoom(Integer roomNumber, Double basePrice, Boolean booked, String guestName) {
        super(roomNumber, RoomType.STANDARD, basePrice, booked, guestName);
    }

    @Override
    public Double calculateTariff(Integer days) {
        return Double.valueOf(getBasePrice().doubleValue() * days.intValue());
    }

    @Override
    public String provideWifi() {
        return "Basic Wi-Fi";
    }

    @Override
    public String provideBreakfast() {
        return "No complimentary breakfast";
    }
}
