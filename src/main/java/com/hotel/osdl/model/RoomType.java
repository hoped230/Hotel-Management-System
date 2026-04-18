package com.hotel.osdl.model;

// Feature used: enum constructor and enum methods.
public enum RoomType {
    STANDARD(2200.0),
    DELUXE(3800.0),
    SUITE(5600.0);

    private final Double baseTariff;

    RoomType(Double baseTariff) {
        this.baseTariff = baseTariff;
    }

    public Double getBaseTariff() {
        return baseTariff;
    }

    public Double calculateCost(Integer days) {
        return Double.valueOf(baseTariff.doubleValue() * days.intValue());
    }
}
