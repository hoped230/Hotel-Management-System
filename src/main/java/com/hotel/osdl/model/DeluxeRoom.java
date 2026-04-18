package com.hotel.osdl.model;

// Feature used: single inheritance, super keyword and method overriding.
public class DeluxeRoom extends Room implements Amenities {
    private Boolean freeWifi;
    private Boolean complimentaryBreakfast;

    public DeluxeRoom(Integer roomNumber) {
        this(roomNumber, RoomType.DELUXE.getBaseTariff(), Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, "");
    }

    public DeluxeRoom(Integer roomNumber, Double basePrice) {
        this(roomNumber, basePrice, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, "");
    }

    public DeluxeRoom(
            Integer roomNumber,
            Double basePrice,
            Boolean freeWifi,
            Boolean complimentaryBreakfast,
            Boolean booked,
            String guestName
    ) {
        super(roomNumber, RoomType.DELUXE, basePrice, booked, guestName);
        this.freeWifi = freeWifi;
        this.complimentaryBreakfast = complimentaryBreakfast;
    }

    public Boolean getFreeWifi() {
        return freeWifi;
    }

    public Boolean getComplimentaryBreakfast() {
        return complimentaryBreakfast;
    }

    @Override
    public Double calculateTariff(Integer days) {
        Double extraCharge = Double.valueOf(400.0 * days.intValue());
        return Double.valueOf(super.getBasePrice().doubleValue() * days.intValue() + extraCharge.doubleValue());
    }

    @Override
    public String provideWifi() {
        return freeWifi.booleanValue() ? "Free Wi-Fi" : "Wi-Fi not included";
    }

    @Override
    public String provideBreakfast() {
        return complimentaryBreakfast.booleanValue() ? "Complimentary breakfast" : "Breakfast not included";
    }
}
