package com.hotel.osdl.service;

import java.util.HashMap;

// Feature used: synchronization methods, synchronization blocks, wait() and notifyAll().
public class RoomAllocationMonitor {
    private final HashMap<Integer, Boolean> roomStatusMap = new HashMap<Integer, Boolean>();

    public synchronized void registerRoom(Integer roomNumber, Boolean booked) {
        roomStatusMap.put(roomNumber, booked);
    }

    public void bookRoom(Integer roomNumber) throws InterruptedException {
        synchronized (this) {
            while (Boolean.TRUE.equals(roomStatusMap.get(roomNumber))) {
                wait();
            }
            roomStatusMap.put(roomNumber, Boolean.TRUE);
            notifyAll();
        }
    }

    public synchronized void releaseRoom(Integer roomNumber) {
        roomStatusMap.put(roomNumber, Boolean.FALSE);
        notifyAll();
    }
}
