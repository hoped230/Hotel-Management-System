package com.hotel.osdl.service;

// Feature used: generic class with two parameters for room details.
public class RoomDetail<T, U> {
    private T roomId;
    private U roomAttribute;

    public RoomDetail(T roomId, U roomAttribute) {
        this.roomId = roomId;
        this.roomAttribute = roomAttribute;
    }

    public T getRoomId() {
        return roomId;
    }

    public U getRoomAttribute() {
        return roomAttribute;
    }
}
