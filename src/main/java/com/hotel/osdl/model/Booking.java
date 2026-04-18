package com.hotel.osdl.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// Feature used: interface implementation for billing polymorphism.
public class Booking implements Billable, Serializable {
    private final String id;
    private final Guest guest;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final Integer adults;
    private final Integer children;
    private BookingStatus status;
    private final List<ServiceItem> serviceItems = new ArrayList<>();

    public Booking(
            String id,
            Guest guest,
            Room room,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer adults,
            Integer children,
            BookingStatus status
    ) {
        this.id = id;
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.adults = adults;
        this.children = children;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public Integer getAdults() {
        return adults;
    }

    public Integer getChildren() {
        return children;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public List<ServiceItem> getServiceItems() {
        return serviceItems;
    }

    public void addServiceItem(ServiceItem item) {
        serviceItems.add(item);
    }

    public Long getNightCount() {
        Long nights = Long.valueOf(ChronoUnit.DAYS.between(checkIn, checkOut));
        if (nights.longValue() <= 0) {
            nights = Long.valueOf(1);
        }
        return nights;
    }

    public Double getRoomCharge() {
        return room.calculateTariff(Integer.valueOf(getNightCount().intValue()));
    }

    public Double getServiceCharge() {
        Double total = Double.valueOf(0.0);
        for (ServiceItem item : serviceItems) {
            total = Double.valueOf(total.doubleValue() + item.getBillAmount());
        }
        return total;
    }

    public Double getTaxAmount() {
        return Double.valueOf((getRoomCharge().doubleValue() + getServiceCharge().doubleValue()) * 0.12);
    }

    public Double getGrandTotal() {
        return Double.valueOf(getRoomCharge().doubleValue() + getServiceCharge().doubleValue() + getTaxAmount().doubleValue());
    }

    @Override
    public String getBillLabel() {
        return "Room " + room.getRoomNumber() + " stay";
    }

    @Override
    public double getBillAmount() {
        return getRoomCharge();
    }
}
