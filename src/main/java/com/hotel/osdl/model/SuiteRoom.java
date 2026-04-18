package com.hotel.osdl.model;

public class SuiteRoom extends Room implements Amenities {
    public SuiteRoom(Integer roomNumber) {
        super(roomNumber, RoomType.SUITE);
    }

    public SuiteRoom(Integer roomNumber, Double basePrice) {
        super(roomNumber, RoomType.SUITE, basePrice);
    }

    public SuiteRoom(Integer roomNumber, Double basePrice, Boolean booked, String guestName) {
        super(roomNumber, RoomType.SUITE, basePrice, booked, guestName);
    }

    @Override
    public Double calculateTariff(Integer days) {
        Double serviceCharge = Double.valueOf(900.0 * days.intValue());
        return Double.valueOf(getBasePrice().doubleValue() * days.intValue() + serviceCharge.doubleValue());
    }

    @Override
    public String provideWifi() {
        return "Premium Wi-Fi";
    }

    @Override
    public String provideBreakfast() {
        return "Luxury breakfast included";
    }
}
