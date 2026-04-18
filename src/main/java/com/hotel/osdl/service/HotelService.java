package com.hotel.osdl.service;

import com.hotel.osdl.model.Billable;
import com.hotel.osdl.model.Booking;
import com.hotel.osdl.model.BookingStatus;
import com.hotel.osdl.model.Guest;
import com.hotel.osdl.model.Room;
import com.hotel.osdl.model.RoomType;
import com.hotel.osdl.model.ServiceItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class HotelService {
    private static final HashMap<String, Double> SERVICE_RATES = new HashMap<String, Double>();
    private static final HotelService INSTANCE = new HotelService();

    private final HotelDataStore dataStore = new HotelDataStore();
    private final ArrayList<Room> rooms = new ArrayList<Room>();
    private final ArrayList<Booking> bookings = new ArrayList<Booking>();
    private final ArrayList<Runnable> listeners = new ArrayList<Runnable>();
    private final HashMap<Integer, Guest> roomCustomerMap = new HashMap<Integer, Guest>();
    private final RoomAllocationMonitor allocationMonitor = new RoomAllocationMonitor();

    private HotelService() {
        loadServiceRates();
        loadData();
    }

    public static HotelService getInstance() {
        return INSTANCE;
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public ArrayList<Room> getRooms() {
        ArrayList<Room> roomList = new ArrayList<Room>(rooms);
        Collections.sort(roomList, new Comparator<Room>() {
            @Override
            public int compare(Room firstRoom, Room secondRoom) {
                return firstRoom.getRoomNumber().compareTo(secondRoom.getRoomNumber());
            }
        });
        return roomList;
    }

    public ArrayList<Room> getAvailableRooms(RoomType type) {
        ArrayList<Room> availableRooms = new ArrayList<Room>();
        Iterator<Room> iterator = rooms.iterator();
        while (iterator.hasNext()) {
            Room room = iterator.next();
            if (room.getRoomType() == type && !room.getBooked().booleanValue()) {
                availableRooms.add(room);
            }
        }
        Collections.sort(availableRooms, new Comparator<Room>() {
            @Override
            public int compare(Room firstRoom, Room secondRoom) {
                return firstRoom.getRoomNumber().compareTo(secondRoom.getRoomNumber());
            }
        });
        return availableRooms;
    }

    public ArrayList<Booking> getBookings() {
        ArrayList<Booking> bookingList = new ArrayList<Booking>(bookings);
        Collections.sort(bookingList, new Comparator<Booking>() {
            @Override
            public int compare(Booking firstBooking, Booking secondBooking) {
                return secondBooking.getCheckIn().compareTo(firstBooking.getCheckIn());
            }
        });
        return bookingList;
    }

    public ArrayList<Booking> getActiveBookings() {
        ArrayList<Booking> activeBookings = new ArrayList<Booking>();
        Iterator<Booking> iterator = bookings.iterator();
        while (iterator.hasNext()) {
            Booking booking = iterator.next();
            if (booking.getStatus() == BookingStatus.ACTIVE) {
                activeBookings.add(booking);
            }
        }
        Collections.sort(activeBookings, new Comparator<Booking>() {
            @Override
            public int compare(Booking firstBooking, Booking secondBooking) {
                return firstBooking.getCheckIn().compareTo(secondBooking.getCheckIn());
            }
        });
        return activeBookings;
    }

    public double getTotalRevenue() {
        Double total = Double.valueOf(0.0);
        int index = 0;
        while (index < bookings.size()) {
            total = Double.valueOf(total.doubleValue() + bookings.get(index).getGrandTotal().doubleValue());
            index++;
        }
        return total.doubleValue();
    }

    public Booking createBooking(
            String guestName,
            String phone,
            String email,
            Room room,
            LocalDate checkIn,
            LocalDate checkOut,
            int adults,
            int children,
            Map<String, Integer> selectedServices
    ) throws ValidationException {
        validateBookingInput(guestName, phone, room, checkIn, checkOut, adults, children);
        runBackgroundTasks();

        Guest guest = new Guest(nextId("GST"), guestName.trim(), phone.trim(), email.trim());
        Booking booking = new Booking(
                nextId("BKG"),
                guest,
                room,
                checkIn,
                checkOut,
                adults,
                children,
                BookingStatus.ACTIVE
        );

        try {
            allocationMonitor.bookRoom(room.getRoomNumber());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        room.setBooked(Boolean.TRUE);
        room.setGuestName(guest.getName());
        roomCustomerMap.put(room.getRoomNumber(), guest);
        dataStore.updateRoomStatus(room.getRoomNumber(), Boolean.TRUE, guest.getName());

        for (Map.Entry<String, Integer> entry : selectedServices.entrySet()) {
            Integer quantity = entry.getValue();
            if (quantity != null && quantity.intValue() > 0) {
                booking.addServiceItem(new ServiceItem(booking.getId(), entry.getKey(), quantity, SERVICE_RATES.get(entry.getKey())));
            }
        }

        bookings.add(booking);
        persist();
        notifyListeners();
        return booking;
    }

    public void addServiceToBooking(Booking booking, String serviceName, int quantity) throws ValidationException {
        if (booking == null) {
            throw new ValidationException("Select a booking before adding a service.");
        }
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new ValidationException("Services can only be added to active bookings.");
        }
        if (!SERVICE_RATES.containsKey(serviceName) || quantity <= 0) {
            throw new ValidationException("Choose a valid service and quantity.");
        }

        booking.addServiceItem(new ServiceItem(booking.getId(), serviceName, quantity, SERVICE_RATES.get(serviceName)));
        persist();
        notifyListeners();
    }

    public void checkoutBooking(Booking booking) throws ValidationException {
        if (booking == null) {
            throw new ValidationException("Select a booking before checkout.");
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);
        booking.getRoom().setBooked(Boolean.FALSE);
        booking.getRoom().setGuestName("");
        roomCustomerMap.remove(booking.getRoom().getRoomNumber());
        allocationMonitor.releaseRoom(booking.getRoom().getRoomNumber());
        dataStore.updateRoomStatus(booking.getRoom().getRoomNumber(), Boolean.FALSE, "");
        dataStore.saveInvoice(booking.getId(), createInvoiceText(booking));
        persist();
        notifyListeners();
    }

    public String createInvoiceText(Booking booking) throws ValidationException {
        if (booking == null) {
            throw new ValidationException("Select a booking to generate the bill.");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("OSDL HOTEL MANAGEMENT\n");
        builder.append("Generated: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                .append("\n\n");
        builder.append("Booking ID: ").append(booking.getId()).append("\n");
        builder.append("Guest: ").append(booking.getGuest().getName()).append("\n");
        builder.append("Phone: ").append(booking.getGuest().getPhone()).append("\n");
        builder.append("Room: ").append(booking.getRoom().getRoomNumber()).append(" - ").append(booking.getRoom().getRoomType()).append("\n");
        builder.append("Stay: ").append(booking.getCheckIn()).append(" to ").append(booking.getCheckOut()).append(" (")
                .append(booking.getNightCount()).append(" nights)\n\n");

        builder.append(String.format(Locale.ENGLISH, "%-30s %10s%n", "Charge Item", "Amount"));
        builder.append("---------------------------------------------\n");
        appendBillLine(builder, booking);
        for (Billable service : booking.getServiceItems()) {
            appendBillLine(builder, service);
        }
        builder.append(String.format(Locale.ENGLISH, "%-30s %10.2f%n", "Tax (12%)", booking.getTaxAmount()));
        builder.append("---------------------------------------------\n");
        builder.append(String.format(Locale.ENGLISH, "%-30s %10.2f%n", "Grand Total", booking.getGrandTotal()));
        return builder.toString();
    }

    public Map<String, Double> getServiceRates() {
        return SERVICE_RATES;
    }

    public long getAvailableRoomCount() {
        Integer bookedCount = Integer.valueOf(getActiveBookings().size());
        Integer totalCount = Integer.valueOf(rooms.size());
        return Long.valueOf(totalCount.intValue() - bookedCount.intValue()).longValue();
    }

    public Map<RoomType, Long> getRoomTypeCounts() {
        HashMap<RoomType, Long> counts = new HashMap<RoomType, Long>();
        int index = 0;
        while (index < rooms.size()) {
            RoomType roomType = rooms.get(index).getRoomType();
            Long currentCount = counts.get(roomType);
            if (currentCount == null) {
                counts.put(roomType, Long.valueOf(1));
            } else {
                counts.put(roomType, Long.valueOf(currentCount.longValue() + 1));
            }
            index++;
        }
        return counts;
    }

    private void appendBillLine(StringBuilder builder, Billable item) {
        builder.append(String.format(Locale.ENGLISH, "%-30s %10.2f%n", item.getBillLabel(), item.getBillAmount()));
    }

    private void validateBookingInput(
            String guestName,
            String phone,
            Room room,
            LocalDate checkIn,
            LocalDate checkOut,
            int adults,
            int children
    ) throws ValidationException {
        if (guestName == null || guestName.isBlank()) {
            throw new ValidationException("Guest name is required.");
        }
        if (phone == null || phone.isBlank()) {
            throw new ValidationException("Phone number is required.");
        }
        if (room == null) {
            throw new ValidationException("Choose a room.");
        }
        if (checkIn == null || checkOut == null || checkOut.isBefore(checkIn)) {
            throw new ValidationException("Select valid check-in and check-out dates.");
        }
        if (adults <= 0) {
            throw new ValidationException("At least one adult is required.");
        }
        if (children < 0) {
            throw new ValidationException("Children count cannot be negative.");
        }

        if (room.getBooked().booleanValue()) {
            throw new ValidationException("The selected room is already occupied.");
        }
    }

    private void loadData() {
        rooms.clear();
        rooms.addAll(dataStore.loadRooms());
        int roomIndex = 0;
        while (roomIndex < rooms.size()) {
            allocationMonitor.registerRoom(rooms.get(roomIndex).getRoomNumber(), rooms.get(roomIndex).getBooked());
            roomIndex++;
        }

        bookings.clear();
        ArrayList<Booking> storedBookings = dataStore.loadBookings(rooms);
        bookings.addAll(storedBookings);

        ArrayList<Booking> serializedBookings = dataStore.deserializeBookings();
        int bookingIndex = 0;
        while (bookingIndex < serializedBookings.size()) {
            Booking serializedBooking = serializedBookings.get(bookingIndex);
            if (serializedBooking.getStatus() == BookingStatus.ACTIVE) {
                roomCustomerMap.put(serializedBooking.getRoom().getRoomNumber(), serializedBooking.getGuest());
            }
            bookingIndex++;
        }
    }

    private void persist() {
        dataStore.saveBookings(bookings);
        dataStore.saveRooms(rooms);

        ArrayList<ServiceItem> allServices = new ArrayList<ServiceItem>();
        int index = 0;
        while (index < bookings.size()) {
            allServices.addAll(bookings.get(index).getServiceItems());
            index++;
        }
        dataStore.saveServices(allServices);
        dataStore.serializeBookings(bookings);
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }

    private String nextId(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }

    public String getFeatureSummary() {
        RoomDetail<Integer, String> roomDetail = new RoomDetail<Integer, String>(Integer.valueOf(101), "Room Feature");
        Pair<Integer, String> bookingPair = new Pair<Integer, String>(roomDetail.getRoomId(), roomDetail.getRoomAttribute());
        NumberCalculator<Double> calculator = new NumberCalculator<Double>(Double.valueOf(5000.0), Double.valueOf(500.0));
        String[] roomTypes = new String[]{"STANDARD", "DELUXE", "SUITE"};
        return "Generic Pair: " + bookingPair.getFirst() + "-" + bookingPair.getSecond()
                + " | Generic Display: " + DisplayHelper.display(Boolean.TRUE)
                + " | Generic Array: " + DisplayHelper.printArray(roomTypes)
                + " | Bounded Generic Result: Rs." + String.format("%.2f", calculator.subtract().doubleValue());
    }

    private void loadServiceRates() {
        SERVICE_RATES.put("Breakfast", Double.valueOf(250.0));
        SERVICE_RATES.put("Laundry", Double.valueOf(300.0));
        SERVICE_RATES.put("Airport Pickup", Double.valueOf(900.0));
        SERVICE_RATES.put("Spa", Double.valueOf(1200.0));
    }

    private void runBackgroundTasks() {
        ServiceSimulationThread serviceSimulationThread = new ServiceSimulationThread("Room Cleaning");
        Thread paymentThread = new Thread(new PaymentProcessingTask());
        serviceSimulationThread.start();
        paymentThread.start();
        try {
            serviceSimulationThread.join();
            paymentThread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
