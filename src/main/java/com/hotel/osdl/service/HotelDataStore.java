package com.hotel.osdl.service;

import com.hotel.osdl.model.Booking;
import com.hotel.osdl.model.BookingStatus;
import com.hotel.osdl.model.DeluxeRoom;
import com.hotel.osdl.model.Guest;
import com.hotel.osdl.model.Room;
import com.hotel.osdl.model.RoomType;
import com.hotel.osdl.model.ServiceItem;
import com.hotel.osdl.model.StandardRoom;
import com.hotel.osdl.model.SuiteRoom;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.util.ArrayList;

// Feature used: character streams, byte streams, random access file, serialization and deserialization.
public class HotelDataStore {
    private static final int ROOM_TYPE_SIZE = 20;
    private static final int GUEST_NAME_SIZE = 30;
    private static final int ROOM_RECORD_SIZE = 4 + (ROOM_TYPE_SIZE * 2) + 8 + 1 + (GUEST_NAME_SIZE * 2);

    private final File roomsFile = new File("data/rooms.dat");
    private final File bookingsFile = new File("data/bookings.txt");
    private final File servicesFile = new File("data/services.txt");
    private final File bookingObjectFile = new File("data/bookings.ser");
    private final File invoiceDirectory = new File("invoices");

    public HotelDataStore() {
        ensureSeedData();
    }

    public ArrayList<Room> loadRooms() {
        ArrayList<Room> rooms = new ArrayList<Room>();
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(roomsFile, "r");
            while (randomAccessFile.getFilePointer() < randomAccessFile.length()) {
                Integer roomNumber = Integer.valueOf(randomAccessFile.readInt());
                String roomType = readFixedString(randomAccessFile, ROOM_TYPE_SIZE).trim();
                Double price = Double.valueOf(randomAccessFile.readDouble());
                Boolean booked = Boolean.valueOf(randomAccessFile.readBoolean());
                String guestName = readFixedString(randomAccessFile, GUEST_NAME_SIZE).trim();
                rooms.add(createRoom(roomNumber, RoomType.valueOf(roomType), price, booked, guestName));
            }
        } catch (EOFException exception) {
            return rooms;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read room records.", exception);
        } finally {
            closeRandomAccessFile(randomAccessFile);
        }
        return rooms;
    }

    public void saveRooms(ArrayList<Room> rooms) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(roomsFile, "rw");
            randomAccessFile.setLength(0);
            int index = 0;
            while (index < rooms.size()) {
                Room room = rooms.get(index);
                randomAccessFile.writeInt(room.getRoomNumber().intValue());
                writeFixedString(randomAccessFile, room.getRoomType().name(), ROOM_TYPE_SIZE);
                randomAccessFile.writeDouble(room.getBasePrice().doubleValue());
                randomAccessFile.writeBoolean(room.getBooked().booleanValue());
                writeFixedString(randomAccessFile, room.getGuestName(), GUEST_NAME_SIZE);
                index++;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write room records.", exception);
        } finally {
            closeRandomAccessFile(randomAccessFile);
        }
    }

    public void updateRoomStatus(Integer roomNumber, Boolean booked, String guestName) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(roomsFile, "rw");
            long position = findRoomPosition(randomAccessFile, roomNumber);
            if (position >= 0) {
                randomAccessFile.seek(position + 4 + (ROOM_TYPE_SIZE * 2) + 8);
                randomAccessFile.writeBoolean(booked.booleanValue());
                writeFixedString(randomAccessFile, guestName, GUEST_NAME_SIZE);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to update room status.", exception);
        } finally {
            closeRandomAccessFile(randomAccessFile);
        }
    }

    public ArrayList<Booking> loadBookings(ArrayList<Room> rooms) {
        ArrayList<Booking> bookings = new ArrayList<Booking>();
        FileReader fileReader = null;
        try {
            if (!bookingsFile.exists()) {
                return bookings;
            }
            fileReader = new FileReader(bookingsFile);
            StringBuilder builder = new StringBuilder();
            int data;
            while ((data = fileReader.read()) != -1) {
                builder.append((char) data);
            }

            String[] lines = builder.toString().split("\\r?\\n");
            int index = 0;
            while (index < lines.length) {
                if (!lines[index].trim().isEmpty()) {
                    String[] parts = lines[index].split("\\|");
                    Room room = findRoomInList(rooms, Integer.valueOf(parts[5]));
                    bookings.add(new Booking(
                            parts[0],
                            new Guest(parts[1], parts[2], parts[3], parts[4]),
                            room,
                            LocalDate.parse(parts[6]),
                            LocalDate.parse(parts[7]),
                            Integer.valueOf(parts[8]),
                            Integer.valueOf(parts[9]),
                            BookingStatus.valueOf(parts[10])
                    ));
                }
                index++;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read bookings text file.", exception);
        } finally {
            closeFileReader(fileReader);
        }
        attachServices(bookings);
        return bookings;
    }

    public void saveBookings(ArrayList<Booking> bookings) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(bookingsFile, false);
            int index = 0;
            while (index < bookings.size()) {
                Booking booking = bookings.get(index);
                fileWriter.write(booking.getId() + "|"
                        + booking.getGuest().getId() + "|"
                        + sanitize(booking.getGuest().getName()) + "|"
                        + sanitize(booking.getGuest().getPhone()) + "|"
                        + sanitize(booking.getGuest().getEmail()) + "|"
                        + booking.getRoom().getRoomNumber() + "|"
                        + booking.getCheckIn() + "|"
                        + booking.getCheckOut() + "|"
                        + booking.getAdults() + "|"
                        + booking.getChildren() + "|"
                        + booking.getStatus().name() + System.lineSeparator());
                index++;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write bookings text file.", exception);
        } finally {
            closeFileWriter(fileWriter);
        }
    }

    public void saveServices(ArrayList<ServiceItem> services) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(servicesFile, false);
            int index = 0;
            while (index < services.size()) {
                ServiceItem item = services.get(index);
                fileWriter.write(item.getBookingId() + "|"
                        + sanitize(item.getName()) + "|"
                        + item.getQuantity() + "|"
                        + item.getUnitPrice() + System.lineSeparator());
                index++;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write services text file.", exception);
        } finally {
            closeFileWriter(fileWriter);
        }
    }

    public void serializeBookings(ArrayList<Booking> bookings) {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(bookingObjectFile);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(bookings);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to serialize bookings.", exception);
        } finally {
            closeObjectOutputStream(objectOutputStream);
            closeFileOutputStream(fileOutputStream);
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Booking> deserializeBookings() {
        if (!bookingObjectFile.exists()) {
            return new ArrayList<Booking>();
        }

        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(bookingObjectFile);
            objectInputStream = new ObjectInputStream(fileInputStream);
            return (ArrayList<Booking>) objectInputStream.readObject();
        } catch (IOException exception) {
            return new ArrayList<Booking>();
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to deserialize bookings.", exception);
        } finally {
            closeObjectInputStream(objectInputStream);
            closeFileInputStream(fileInputStream);
        }
    }

    public void saveInvoice(String bookingId, String content) {
        if (!invoiceDirectory.exists()) {
            invoiceDirectory.mkdirs();
        }

        File invoiceFile = new File(invoiceDirectory, bookingId + "-invoice.txt");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(invoiceFile, false);
            fileWriter.write(content);
            copyFileUsingByteStreams(invoiceFile, new File(invoiceDirectory, bookingId + "-invoice-copy.txt"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save invoice.", exception);
        } finally {
            closeFileWriter(fileWriter);
        }
    }

    public void ensureSeedData() {
        File dataDirectory = new File("data");
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        if (!invoiceDirectory.exists()) {
            invoiceDirectory.mkdirs();
        }
        if (!roomsFile.exists()) {
            ArrayList<Room> seedRooms = new ArrayList<Room>();
            seedRooms.add(new StandardRoom(Integer.valueOf(101), Double.valueOf(2200.0), Boolean.FALSE, ""));
            seedRooms.add(new StandardRoom(Integer.valueOf(102), Double.valueOf(2300.0), Boolean.FALSE, ""));
            seedRooms.add(new StandardRoom(Integer.valueOf(103), Double.valueOf(2400.0), Boolean.FALSE, ""));
            seedRooms.add(new DeluxeRoom(Integer.valueOf(201), Double.valueOf(3800.0), Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, ""));
            seedRooms.add(new DeluxeRoom(Integer.valueOf(202), Double.valueOf(3900.0), Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, ""));
            seedRooms.add(new DeluxeRoom(Integer.valueOf(203), Double.valueOf(4100.0), Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, ""));
            seedRooms.add(new SuiteRoom(Integer.valueOf(301), Double.valueOf(5600.0), Boolean.FALSE, ""));
            seedRooms.add(new SuiteRoom(Integer.valueOf(302), Double.valueOf(5900.0), Boolean.FALSE, ""));
            saveRooms(seedRooms);
        }
        createTextFileIfMissing(bookingsFile);
        createTextFileIfMissing(servicesFile);
    }

    private void attachServices(ArrayList<Booking> bookings) {
        FileReader fileReader = null;
        try {
            if (!servicesFile.exists()) {
                return;
            }
            fileReader = new FileReader(servicesFile);
            StringBuilder builder = new StringBuilder();
            int data;
            while ((data = fileReader.read()) != -1) {
                builder.append((char) data);
            }

            String[] lines = builder.toString().split("\\r?\\n");
            int index = 0;
            while (index < lines.length) {
                if (!lines[index].trim().isEmpty()) {
                    String[] parts = lines[index].split("\\|");
                    Booking booking = findBooking(bookings, parts[0]);
                    if (booking != null) {
                        booking.addServiceItem(new ServiceItem(
                                parts[0],
                                parts[1],
                                Integer.valueOf(parts[2]),
                                Double.valueOf(parts[3])
                        ));
                    }
                }
                index++;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read services text file.", exception);
        } finally {
            closeFileReader(fileReader);
        }
    }

    private void copyFileUsingByteStreams(File source, File destination) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(source);
            fileOutputStream = new FileOutputStream(destination);
            int data;
            while ((data = fileInputStream.read()) != -1) {
                fileOutputStream.write(data);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to copy invoice file.", exception);
        } finally {
            closeFileInputStream(fileInputStream);
            closeFileOutputStream(fileOutputStream);
        }
    }

    private long findRoomPosition(RandomAccessFile randomAccessFile, Integer roomNumber) throws IOException {
        long position = 0;
        while (position < randomAccessFile.length()) {
            randomAccessFile.seek(position);
            Integer currentRoomNumber = Integer.valueOf(randomAccessFile.readInt());
            if (currentRoomNumber.intValue() == roomNumber.intValue()) {
                return position;
            }
            position = position + ROOM_RECORD_SIZE;
        }
        return -1;
    }

    private Room createRoom(Integer roomNumber, RoomType roomType, Double price, Boolean booked, String guestName) {
        if (roomType == RoomType.DELUXE) {
            return new DeluxeRoom(roomNumber, price, Boolean.TRUE, Boolean.TRUE, booked, guestName);
        }
        if (roomType == RoomType.SUITE) {
            return new SuiteRoom(roomNumber, price, booked, guestName);
        }
        return new StandardRoom(roomNumber, price, booked, guestName);
    }

    private Room findRoomInList(ArrayList<Room> rooms, Integer roomNumber) {
        int index = 0;
        while (index < rooms.size()) {
            Room room = rooms.get(index);
            if (room.getRoomNumber().intValue() == roomNumber.intValue()) {
                return room;
            }
            index++;
        }
        return null;
    }

    private Booking findBooking(ArrayList<Booking> bookings, String bookingId) {
        int index = 0;
        while (index < bookings.size()) {
            Booking booking = bookings.get(index);
            if (booking.getId().equals(bookingId)) {
                return booking;
            }
            index++;
        }
        return null;
    }

    private void writeFixedString(RandomAccessFile randomAccessFile, String value, int size) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (value != null) {
            builder.append(value);
        }
        while (builder.length() < size) {
            builder.append(' ');
        }
        randomAccessFile.writeChars(builder.substring(0, size));
    }

    private String readFixedString(RandomAccessFile randomAccessFile, int size) throws IOException {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        while (index < size) {
            builder.append(randomAccessFile.readChar());
            index++;
        }
        return builder.toString();
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "/");
    }

    private void createTextFileIfMissing(File file) {
        FileWriter fileWriter = null;
        try {
            if (!file.exists()) {
                fileWriter = new FileWriter(file, false);
                fileWriter.write("");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to prepare file.", exception);
        } finally {
            closeFileWriter(fileWriter);
        }
    }

    private void closeRandomAccessFile(RandomAccessFile randomAccessFile) {
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeFileReader(FileReader fileReader) {
        if (fileReader != null) {
            try {
                fileReader.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeFileWriter(FileWriter fileWriter) {
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeFileInputStream(FileInputStream fileInputStream) {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeFileOutputStream(FileOutputStream fileOutputStream) {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeObjectInputStream(ObjectInputStream objectInputStream) {
        if (objectInputStream != null) {
            try {
                objectInputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeObjectOutputStream(ObjectOutputStream objectOutputStream) {
        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
            } catch (IOException ignored) {
            }
        }
    }
}
