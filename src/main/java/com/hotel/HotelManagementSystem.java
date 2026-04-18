package com.hotel;

import java.io.*;
import java.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

// ==================== ENUMS  ====================

enum RoomType {
    STANDARD("Standard", 1500.0),
    DELUXE("Deluxe", 3000.0),
    SUITE("Suite", 5000.0);

    private final String displayName;
    private final double pricePerDay;

    RoomType(String displayName, double pricePerDay) {
        this.displayName = displayName;
        this.pricePerDay = pricePerDay;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }
}

enum RoomStatus {
    AVAILABLE,
    OCCUPIED,
}

// ==================== INTERFACES ====================

interface Billable {
    double calculateBill();
    String getBillDetails();
}

interface Displayable {
    String getDisplayInfo();
}

// ==================== ABSTRACT CLASS  ====================

abstract class Room implements Serializable, Billable, Displayable {

    private static final long serialVersionUID = 1L;

    private int roomNumber;
    private RoomType roomType;
    private RoomStatus status;
    private String guestName;
    private String guestPhone;
    private int daysBooked;

    Room(int roomNumber, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.status = RoomStatus.AVAILABLE;
        this.guestName = "";
        this.guestPhone = "";
        this.daysBooked = 0;
    }

    Room(
        int roomNumber,
        RoomType roomType,
        String guestName,
        String guestPhone,
        int days
    ) {
        this(roomNumber, roomType);
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.daysBooked = days;
        if (!guestName.isEmpty()) this.status = RoomStatus.OCCUPIED;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public RoomStatus getRoomStatus() {
        return status;
    }

    public String getStatus() {
        return status.name();
    }

    public boolean isOccupied() {
        return status == RoomStatus.OCCUPIED;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public int getDaysBooked() {
        return daysBooked;
    }

    public void bookRoom(String name, String phone) {
        bookRoom(name, phone, 1);
    }

    public void bookRoom(String name, String phone, int days) {
        this.status = RoomStatus.OCCUPIED;
        this.guestName = name;
        this.guestPhone = phone;
        this.daysBooked = days;
    }

    public void clearRoom() {
        this.status = RoomStatus.AVAILABLE;
        this.guestName = "";
        this.guestPhone = "";
        this.daysBooked = 0;
    }

    abstract double getServiceCharge();

    @Override
    public double calculateBill() {
        return roomType.getPricePerDay() * daysBooked + getServiceCharge();
    }

    @Override
    public String getBillDetails() {
        return String.format(
            "Room %d | %s | %d days | Rs. %.2f",
            roomNumber,
            roomType.getDisplayName(),
            daysBooked,
            calculateBill()
        );
    }

    @Override
    public String getDisplayInfo() {
        return String.format(
            "Room %d - %s [%s]",
            roomNumber,
            roomType.getDisplayName(),
            status
        );
    }
}

// ==================== CONCRETE CLASSES  ====================

class StandardRoom extends Room {

    private static final long serialVersionUID = 1L;

    StandardRoom(int roomNumber) {
        super(roomNumber, RoomType.STANDARD);
    }

    StandardRoom(int roomNumber, String guest, String phone, int days) {
        super(roomNumber, RoomType.STANDARD, guest, phone, days);
    }

    @Override
    double getServiceCharge() {
        return 0;
    }

    @Override
    public String getDisplayInfo() {
        return super.getDisplayInfo() + " - Basic";
    }
}

class DeluxeRoom extends Room {

    private static final long serialVersionUID = 1L;
    private double extraCharge = 500.0;

    DeluxeRoom(int roomNumber) {
        super(roomNumber, RoomType.DELUXE);
    }

    @Override
    double getServiceCharge() {
        return extraCharge;
    }
}

class SuiteRoom extends Room {

    private static final long serialVersionUID = 1L;
    private double discountRate = 0.10;

    SuiteRoom(int roomNumber) {
        super(roomNumber, RoomType.SUITE);
    }

    @Override
    double getServiceCharge() {
        return -(
            getRoomType().getPricePerDay() * getDaysBooked() * discountRate
        );
    }

    @Override
    public String getBillDetails() {
        return super.getBillDetails() + " (10% discount)";
    }
}

// ==================== GENERIC CLASS ====================

class DataStore<T extends Serializable> {

    private List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(item);
    }

    public T get(int index) {
        return items.get(index);
    }

    public List<T> getAll() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean contains(T item) {
        return items.contains(item);
    }

    public void remove(T item) {
        items.remove(item);
    }

    public void set(int index, T item) {
        items.set(index, item);
    }

    public static <U extends Number> double sum(List<U> numbers) {
        double total = 0;
        for (U num : numbers) {
            total += num.doubleValue();
        }
        return total;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray(T[] array) {
        return items.toArray(array);
    }
}

// ==================== BILL CLASS  ====================

class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    // Wrapper classes
    private Integer roomNumber;
    private Double baseAmount;
    private Double serviceCharge;
    private Double taxAmount;
    private Double totalAmount;
    private Boolean isPaid;
    private Character roomCategory;

    Bill(Room room) {
        this.roomNumber = room.getRoomNumber();
        this.baseAmount =
            room.getRoomType().getPricePerDay() * room.getDaysBooked();
        this.serviceCharge = room.getServiceCharge();
        this.taxAmount = baseAmount * 0.12; // 12% GST
        this.totalAmount = baseAmount + serviceCharge + taxAmount;
        this.isPaid = false;
        this.roomCategory = room.getRoomType().getDisplayName().charAt(0);
    }

    // Unboxing
    public int getRoomNum() {
        return roomNumber;
    }

    public double getBase() {
        return baseAmount;
    }

    public double getService() {
        return serviceCharge;
    }

    public double getTax() {
        return taxAmount;
    }

    public double getTotal() {
        return totalAmount;
    }

    public boolean paid() {
        return isPaid;
    }

    public char getCategory() {
        return roomCategory;
    }

    // Boxing
    public void markPaid() {
        isPaid = Boolean.TRUE;
    }

    public String getFormattedBill() {
        int rn = roomNumber;
        char cat = roomCategory;
        double base = baseAmount;
        double svc = serviceCharge;
        double tax = taxAmount;
        double total = totalAmount;
        boolean paid = isPaid;

        return String.format(
            "Room: %d (%c)\nBase: Rs. %.2f\nService Charge: Rs. %.2f\nTax (12%% GST): Rs. %.2f\n\nTotal: Rs. %.2f\nPaid: %s",
            rn,
            cat,
            base,
            svc,
            tax,
            total,
            paid ? "Yes" : "No"
        );
    }
}

// ==================== BOOKING REQUEST ====================

class BookingRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    String guestName, guestPhone;
    int roomNumber, days;

    BookingRequest(String name, String phone, int room, int days) {
        this.guestName = name;
        this.guestPhone = phone;
        this.roomNumber = room;
        this.days = days;
    }
}

// ==================== BOOKING QUEUE  ====================

class BookingQueue {

    private final Queue<BookingRequest> queue = new LinkedList<>();
    private final int MAX_SIZE = 10;

    // Synchronized block
    public void addRequest(BookingRequest request) throws InterruptedException {
        synchronized (this) {
            while (queue.size() >= MAX_SIZE) {
                wait();
            }
            queue.add(request);
            notify();
        }
    }

    public synchronized BookingRequest getRequest()
        throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        BookingRequest request = queue.poll();
        notify();
        return request;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}

// ==================== FILE MANAGER  ====================

class FileManager {

    private static final String DATA_FILE = "hotel_data.dat";
    private static final String LOG_FILE = "hotel_log.txt";
    private static final String RECORD_FILE = "hotel_records.dat";

    // Serialization
    public static void saveData(
        ArrayList<Room> rooms,
        HashMap<String, Room> map
    ) {
        try (
            ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE)
            )
        ) {
            oos.writeObject(rooms);
            oos.writeObject(map);
        } catch (IOException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    // Deserialization
    @SuppressWarnings("unchecked")
    public static Object[] loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return null;
        try (
            ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file)
            )
        ) {
            ArrayList<Room> rooms = (ArrayList<Room>) ois.readObject();
            HashMap<String, Room> map = (HashMap<
                String,
                Room
            >) ois.readObject();
            return new Object[] { rooms, map };
        } catch (Exception e) {
            System.out.println("Load error: " + e.getMessage());
            return null;
        }
    }

    // FileWriter
    public static void appendLog(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(java.time.LocalDateTime.now() + " | " + message + "\n");
        } catch (IOException e) {
            System.out.println("Log write error: " + e.getMessage());
        }
    }

    // FileReader
    public static String readLog() {
        File file = new File(LOG_FILE);
        if (!file.exists()) return "No logs found.";
        StringBuilder sb = new StringBuilder();
        try (FileReader fr = new FileReader(file)) {
            int ch;
            while ((ch = fr.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException e) {
            sb.append("Error reading log.");
        }
        return sb.toString();
    }

    // RandomAccessFile + seek()
    public static void writeRecord(String record) {
        try (RandomAccessFile raf = new RandomAccessFile(RECORD_FILE, "rw")) {
            raf.seek(raf.length()); // seek to end
            raf.writeUTF(record);
        } catch (IOException e) {
            System.out.println("Record write error: " + e.getMessage());
        }
    }

    public static String readRecords() {
        File file = new File(RECORD_FILE);
        if (!file.exists()) return "No records.";
        StringBuilder sb = new StringBuilder();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(0); // seek to beginning
            while (raf.getFilePointer() < raf.length()) {
                sb.append(raf.readUTF()).append("\n");
            }
        } catch (IOException e) {
            sb.append("Error reading records.");
        }
        return sb.toString();
    }

    // FileOutputStream
    public static void saveBytes(String filename, byte[] data) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filename);
            fos.write(data);
        } catch (IOException e) {
            System.out.println("Byte write error: " + e.getMessage());
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {}
        }
    }

    // FileInputStream (Byte Stream)
    public static byte[] loadBytes(String filename) {
        File file = new File(filename);
        if (!file.exists()) return new byte[0];
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            return new byte[0];
        }
    }
}

// ==================== HOTEL  ====================

class Hotel {

    private final ArrayList<Room> rooms = new ArrayList<>(); // ArrayList
    private final HashMap<String, Room> phoneToRoom = new HashMap<>(); // HashMap
    private final BookingQueue bookingQueue = new BookingQueue();
    private final DataStore<Bill> billHistory = new DataStore<>(); // Generic class usage

    Hotel() {
        loadData();
    }

    public synchronized String bookRoom(
        int roomNumber,
        String name,
        String phone,
        int days
    ) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                if (room.isOccupied()) return "Room is already occupied!";
                if (
                    phoneToRoom.containsKey(phone)
                ) return "Guest already has a booking!";

                room.bookRoom(name, phone, days);
                phoneToRoom.put(phone, room);

                if (room instanceof DeluxeRoom) {
                    DeluxeRoom dr = (DeluxeRoom) room;
                    FileManager.appendLog(
                        "Deluxe room " +
                            dr.getRoomNumber() +
                            " booked by " +
                            name
                    );
                } else if (room instanceof SuiteRoom) {
                    SuiteRoom sr = (SuiteRoom) room;
                    FileManager.appendLog(
                        "Suite room " +
                            sr.getRoomNumber() +
                            " booked by " +
                            name
                    );
                } else {
                    FileManager.appendLog(
                        "Standard room " +
                            room.getRoomNumber() +
                            " booked by " +
                            name
                    );
                }

                FileManager.writeRecord(
                    "BOOKING|Room " + roomNumber + "|" + name + "|" + phone
                );
                saveData();
                return "Room " + roomNumber + " booked successfully!";
            }
        }
        return "Room not found!";
    }

    public synchronized String checkoutRoom(int roomNumber) {
        Iterator<Room> iterator = rooms.iterator();
        while (iterator.hasNext()) {
            Room room = iterator.next();
            if (room.getRoomNumber() == roomNumber) {
                if (!room.isOccupied()) return "Room is already available!";

                Bill bill = new Bill(room);
                bill.markPaid();
                billHistory.add(bill);

                String phone = room.getGuestPhone();
                String result =
                    "Checkout successful!\n\n" + bill.getFormattedBill();

                room.clearRoom();
                phoneToRoom.remove(phone);

                FileManager.appendLog("Checkout: Room " + roomNumber);
                FileManager.writeRecord("CHECKOUT|Room " + roomNumber);
                saveData();
                return result;
            }
        }
        return "Room not found!";
    }

    public String checkoutByPhone(String phone) {
        Room room = phoneToRoom.get(phone);
        if (room == null) return "No booking found!";
        return checkoutRoom(room.getRoomNumber());
    }

    public Bill getBill(String phone) {
        Room room = phoneToRoom.get(phone);
        if (room == null) return null;
        return new Bill(room);
    }

    public Room getRoomByPhone(String phone) {
        return phoneToRoom.get(phone);
    }

    public String addRoom(int roomNumber, RoomType type) {
        for (int i = 0; i < rooms.size(); i++) {
            if (
                rooms.get(i).getRoomNumber() == roomNumber
            ) return "Room already exists!";
        }

        Room room;
        switch (type) {
            case DELUXE:
                room = new DeluxeRoom(roomNumber);
                break;
            case SUITE:
                room = new SuiteRoom(roomNumber);
                break;
            default:
                room = new StandardRoom(roomNumber);
                break;
        }

        rooms.add(room);
        Collections.sort(rooms, Comparator.comparingInt(Room::getRoomNumber));

        FileManager.appendLog("Room " + roomNumber + " (" + type + ") added");
        saveData();
        return "Room " + roomNumber + " added!";
    }

    // Collections.reverseOrder()
    public List<Room> getRoomsSortedReverse() {
        ArrayList<Room> sorted = new ArrayList<>(rooms);
        Collections.sort(
            sorted,
            Collections.reverseOrder(
                Comparator.comparingInt(Room::getRoomNumber)
            )
        );
        return sorted;
    }

    public double getTotalRevenue() {
        List<Double> amounts = new ArrayList<>();
        for (int i = 0; i < billHistory.size(); i++) {
            amounts.add(billHistory.get(i).getTotal());
        }
        return DataStore.sum(amounts);
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    public List<Room> getAvailableRooms() {
        List<Room> list = new ArrayList<>();
        for (Room r : rooms) if (!r.isOccupied()) list.add(r);
        return list;
    }

    public List<Room> getOccupiedRooms() {
        List<Room> list = new ArrayList<>();
        for (Room r : rooms) if (r.isOccupied()) list.add(r);
        return list;
    }

    public BookingQueue getBookingQueue() {
        return bookingQueue;
    }

    public DataStore<Bill> getBillHistory() {
        return billHistory;
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        Object[] data = FileManager.loadData();
        if (data != null) {
            rooms.clear();
            rooms.addAll((ArrayList<Room>) data[0]);
            phoneToRoom.clear();
            phoneToRoom.putAll((HashMap<String, Room>) data[1]);
        }
    }

    private void saveData() {
        FileManager.saveData(rooms, phoneToRoom);
    }
}

// ==================== MAIN APPLICATION  ====================

public class HotelManagementSystem extends Application {

    private final Hotel hotel = new Hotel();
    private Stage primaryStage;
    private String cssUrl;

    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "admin123";
    private String loggedGuestName = "";
    private String loggedGuestPhone = "";

    private Thread bookingProcessor;
    private volatile boolean running = true;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.cssUrl = getClass().getResource("styles.css").toExternalForm();

        if (hotel.getAllRooms().isEmpty()) {
            hotel.addRoom(101, RoomType.STANDARD);
            hotel.addRoom(102, RoomType.STANDARD);
            hotel.addRoom(103, RoomType.STANDARD);
            hotel.addRoom(201, RoomType.DELUXE);
            hotel.addRoom(202, RoomType.DELUXE);
            hotel.addRoom(301, RoomType.SUITE);
            hotel.addRoom(302, RoomType.SUITE);
        }

        startBookingProcessor();

        stage.setTitle("Hotel Oasis - Management System");
        showLoginScreen();

        stage.setOnCloseRequest(e -> {
            running = false;
            if (bookingProcessor != null) {
                bookingProcessor.interrupt();
                try {
                    bookingProcessor.join(2000);
                } catch (InterruptedException ex) {}
            }
        });

        stage.show();
    }

    // Runnable interface
    private void startBookingProcessor() {
        Runnable processor = () -> {
            while (running) {
                try {
                    if (!hotel.getBookingQueue().isEmpty()) {
                        BookingRequest req = hotel
                            .getBookingQueue()
                            .getRequest();
                        hotel.bookRoom(
                            req.roomNumber,
                            req.guestName,
                            req.guestPhone,
                            req.days
                        );
                        Thread.yield();
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        };

        bookingProcessor = new Thread(processor);
        bookingProcessor.setDaemon(true);
        bookingProcessor.start();
    }

    // Utility: show alerts
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Utility: create a styled header bar
    private HBox createHeaderBar(String titleText, Button... rightButtons) {
        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 25, 16, 25));
        bar.setStyle("-fx-background-color: #1a237e;");
        bar.getChildren().add(title);
        bar.getChildren().add(spacer);
        for (Button b : rightButtons) {
            b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-padding: 6 18; -fx-background-radius: 4; -fx-cursor: hand;"
            );
            bar.getChildren().add(b);
        }
        return bar;
    }

    // Utility: styled section label
    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        l.setStyle("-fx-text-fill: #37474f;");
        return l;
    }

    // ==================== LOGIN SCREEN ====================
    private void showLoginScreen() {
        Label hotelName = new Label("HOTEL OASIS");
        hotelName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        hotelName.setStyle("-fx-text-fill: #1a237e;");

        Label subtitle = new Label("Management System");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitle.setStyle("-fx-text-fill: #546e7a;");

        VBox titleBox = new VBox(4, hotelName, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // --- Admin Login Card ---
        Label adminHeader = new Label("Admin Login");
        adminHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        adminHeader.setStyle("-fx-text-fill: #1a237e;");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setPrefWidth(240);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setPrefWidth(240);

        Button adminBtn = new Button("Login as Admin");
        adminBtn.getStyleClass().add("primary-btn");
        adminBtn.setPrefWidth(240);

        adminBtn.setOnAction(e -> {
            if (
                userField.getText().equals(ADMIN_USER) &&
                passField.getText().equals(ADMIN_PASS)
            ) {
                showAdminPanel();
            } else {
                showAlert(
                    Alert.AlertType.ERROR,
                    "Login Failed",
                    "Invalid admin credentials!"
                );
            }
        });

        VBox adminCard = new VBox(
            12,
            adminHeader,
            userField,
            passField,
            adminBtn
        );
        adminCard.setAlignment(Pos.CENTER);
        adminCard.setPadding(new Insets(25));
        adminCard.setStyle(
            "-fx-background-color: #e8eaf6; -fx-background-radius: 12; " +
                "-fx-border-color: #9fa8da; -fx-border-radius: 12;"
        );

        // --- Guest Login Card ---
        Label guestHeader = new Label("Guest Login");
        guestHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        guestHeader.setStyle("-fx-text-fill: #2e7d32;");

        TextField nameField = new TextField();
        nameField.setPromptText("Your Name");
        nameField.setPrefWidth(240);

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        phoneField.setPrefWidth(240);

        Button guestBtn = new Button("Login as Guest");
        guestBtn.getStyleClass().add("success-btn");
        guestBtn.setPrefWidth(240);

        guestBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                showAlert(
                    Alert.AlertType.WARNING,
                    "Input Error",
                    "Please enter both name and phone number!"
                );
                return;
            }
            loggedGuestName = name;
            loggedGuestPhone = phone;
            showGuestPanel();
        });

        VBox guestCard = new VBox(
            12,
            guestHeader,
            nameField,
            phoneField,
            guestBtn
        );
        guestCard.setAlignment(Pos.CENTER);
        guestCard.setPadding(new Insets(25));
        guestCard.setStyle(
            "-fx-background-color: #e8f5e9; -fx-background-radius: 12; " +
                "-fx-border-color: #a5d6a7; -fx-border-radius: 12;"
        );

        // Layout
        HBox cardsBox = new HBox(35, adminCard, guestCard);
        cardsBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(35, titleBox, cardsBox);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(50));

        StackPane root = new StackPane(content); // StackPane
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #e8eaf6, #ffffff);"
        );

        Scene scene = new Scene(root, 720, 480);
        scene.getStylesheets().add(cssUrl);
        primaryStage.setScene(scene);
    }

    // ==================== ADMIN PANEL ====================
    private void showAdminPanel() {
        // Header bar
        Button logoutBtn = new Button("Logout");
        HBox headerBar = createHeaderBar(
            "Hotel OASIS  |  Admin Panel",
            logoutBtn
        );
        logoutBtn.setOnAction(e -> showLoginScreen());

        // Filter bar
        Label filterLabel = sectionLabel("Filter Rooms:");
        ComboBox<String> filterCombo = new ComboBox<>(
            FXCollections.observableArrayList(
                "All Rooms",
                "Available",
                "Occupied"
            )
        );
        filterCombo.setValue("All Rooms");
        filterCombo.setStyle("-fx-pref-width: 160;");

        HBox filterBar = new HBox(12, filterLabel, filterCombo);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(10, 0, 5, 0));

        // Room Table — using SimpleStringProperty (avoids reflection issues)
        TableView<Room> table = new TableView<>();

        TableColumn<Room, String> colNum = new TableColumn<>("Room #");
        colNum.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.valueOf(c.getValue().getRoomNumber())
            )
        );
        colNum.setPrefWidth(80);
        colNum.setStyle("-fx-alignment: CENTER;");

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getRoomType().getDisplayName()
            )
        );
        colType.setPrefWidth(110);

        TableColumn<Room, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStatus())
        );
        colStatus.setPrefWidth(100);
        colStatus.setStyle("-fx-alignment: CENTER;");

        TableColumn<Room, String> colGuest = new TableColumn<>("Guest Name");
        colGuest.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getGuestName())
        );
        colGuest.setPrefWidth(170);

        TableColumn<Room, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getGuestPhone())
        );
        colPhone.setPrefWidth(140);

        table
            .getColumns()
            .addAll(colNum, colType, colStatus, colGuest, colPhone);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No rooms to display"));

        Runnable refreshTable = () -> {
            List<Room> list;
            switch (filterCombo.getValue()) {
                case "Available":
                    list = hotel.getAvailableRooms();
                    break;
                case "Occupied":
                    list = hotel.getOccupiedRooms();
                    break;
                default:
                    list = hotel.getAllRooms();
                    break;
            }
            table.setItems(FXCollections.observableArrayList(list));
        };

        filterCombo.setOnAction(e -> refreshTable.run()); // Event handling + Lambda
        refreshTable.run();

        // --- Add Room Form  ---
        Label addLabel = sectionLabel("Add New Room");

        TextField roomNumField = new TextField();
        roomNumField.setPromptText("Room Number");
        roomNumField.setPrefWidth(130);

        ComboBox<RoomType> typeCombo = new ComboBox<>(
            FXCollections.observableArrayList(RoomType.values())
        );
        typeCombo.setPromptText("Room Type");
        typeCombo.setPrefWidth(140);

        Button addBtn = new Button("Add Room");
        addBtn.getStyleClass().add("success-btn");

        addBtn.setOnAction(e -> {
            try {
                int num = Integer.parseInt(roomNumField.getText().trim()); // Integer.parseInt
                RoomType type = typeCombo.getValue();
                if (type == null) {
                    showAlert(
                        Alert.AlertType.WARNING,
                        "Error",
                        "Please select a room type!"
                    );
                    return;
                }
                showAlert(
                    Alert.AlertType.INFORMATION,
                    "Add Room",
                    hotel.addRoom(num, type)
                );
                roomNumField.clear();
                typeCombo.setValue(null);
                refreshTable.run();
            } catch (NumberFormatException ex) {
                showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Enter a valid room number!"
                );
            }
        });

        // --- Checkout Form ---
        Label checkoutLabel = sectionLabel("Checkout Room");

        TextField checkoutField = new TextField();
        checkoutField.setPromptText("Room Number");
        checkoutField.setPrefWidth(130);

        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.getStyleClass().add("danger-btn");

        checkoutBtn.setOnAction(e -> {
            try {
                int num = Integer.parseInt(checkoutField.getText().trim());
                showAlert(
                    Alert.AlertType.INFORMATION,
                    "Checkout",
                    hotel.checkoutRoom(num)
                );
                checkoutField.clear();
                refreshTable.run();
            } catch (NumberFormatException ex) {
                showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Enter a valid room number!"
                );
            }
        });

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(14);
        formGrid.setPadding(new Insets(10, 0, 0, 0));

        formGrid.add(addLabel, 0, 0);
        formGrid.add(roomNumField, 1, 0);
        formGrid.add(typeCombo, 2, 0);
        formGrid.add(addBtn, 3, 0);

        formGrid.add(checkoutLabel, 0, 1);
        formGrid.add(checkoutField, 1, 1);
        formGrid.add(checkoutBtn, 2, 1);

        // Main layout
        VBox body = new VBox(12, filterBar, table, formGrid);
        body.setPadding(new Insets(20, 25, 25, 25));
        body.setStyle("-fx-background-color: #f5f5f5;");
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox root = new VBox(headerBar, body);
        VBox.setVgrow(body, Priority.ALWAYS);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(cssUrl);
        primaryStage.setScene(scene);
    }

    // ==================== GUEST PANEL ====================
    private void showGuestPanel() {
        // Header bar
        Button logoutBtn = new Button("Logout");
        HBox headerBar = createHeaderBar(
            "Hotel Oasis  |  Welcome, " + loggedGuestName,
            logoutBtn
        );
        logoutBtn.setOnAction(e -> {
            loggedGuestName = "";
            loggedGuestPhone = "";
            showLoginScreen();
        });

        // Available rooms label
        Label roomsLabel = sectionLabel("Available Rooms");
        roomsLabel.setPadding(new Insets(5, 0, 0, 0));

        // Available rooms table
        TableView<Room> table = new TableView<>();

        TableColumn<Room, String> colNum = new TableColumn<>("Room #");
        colNum.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.valueOf(c.getValue().getRoomNumber())
            )
        );
        colNum.setPrefWidth(100);
        colNum.setStyle("-fx-alignment: CENTER;");

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getRoomType().getDisplayName()
            )
        );
        colType.setPrefWidth(150);

        TableColumn<Room, String> colPrice = new TableColumn<>("Price / Day");
        colPrice.setCellValueFactory(c ->
            new SimpleStringProperty(
                String.format(
                    "Rs. %.0f",
                    c.getValue().getRoomType().getPricePerDay()
                )
            )
        );
        colPrice.setPrefWidth(150);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(colNum, colType, colPrice);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No rooms available"));

        Runnable refreshTable = () -> {
            table.setItems(
                FXCollections.observableArrayList(hotel.getAvailableRooms())
            );
        };
        refreshTable.run();

        // --- Book Room Form ---
        Label bookLabel = sectionLabel("Book a Room");

        TextField roomField = new TextField();
        roomField.setPromptText("Room Number");
        roomField.setPrefWidth(130);

        TextField daysField = new TextField();
        daysField.setPromptText("Number of Days");
        daysField.setPrefWidth(140);

        Button bookBtn = new Button("Book Room");
        bookBtn.getStyleClass().add("success-btn");

        Label statusLabel = new Label();

        // Multithreading — background booking
        bookBtn.setOnAction(e -> {
            try {
                int num = Integer.parseInt(roomField.getText().trim());
                int days = Integer.parseInt(daysField.getText().trim());
                if (days <= 0) {
                    showAlert(
                        Alert.AlertType.WARNING,
                        "Error",
                        "Days must be at least 1!"
                    );
                    return;
                }

                statusLabel.setText("Processing booking...");
                statusLabel.setStyle(
                    "-fx-text-fill: #e65100; -fx-font-weight: bold;"
                );

                Thread bookThread = new Thread(() -> {
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException ignored) {}
                    String result = hotel.bookRoom(
                        num,
                        loggedGuestName,
                        loggedGuestPhone,
                        days
                    );

                    Platform.runLater(() -> {
                        showAlert(
                            Alert.AlertType.INFORMATION,
                            "Booking",
                            result
                        );
                        roomField.clear();
                        daysField.clear();
                        statusLabel.setText("");
                        refreshTable.run();
                    });
                });
                bookThread.setDaemon(true);
                bookThread.start();
            } catch (NumberFormatException ex) {
                showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Please enter valid numbers!"
                );
            }
        });

        HBox bookForm = new HBox(
            12,
            bookLabel,
            roomField,
            daysField,
            bookBtn,
            statusLabel
        );
        bookForm.setAlignment(Pos.CENTER_LEFT);
        bookForm.setPadding(new Insets(8, 0, 0, 0));

        // --- Action Buttons ---
        Button billBtn = new Button("View My Bill");
        billBtn.getStyleClass().add("primary-btn");
        billBtn.setPrefWidth(150);

        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.getStyleClass().add("danger-btn");
        checkoutBtn.setPrefWidth(150);

        billBtn.setOnAction(e -> {
            Bill bill = hotel.getBill(loggedGuestPhone);
            if (bill == null) {
                showAlert(
                    Alert.AlertType.INFORMATION,
                    "Bill",
                    "You have no active booking."
                );
            } else {
                showBillingDialog(bill);
            }
        });

        checkoutBtn.setOnAction(e -> {
            Bill bill = hotel.getBill(loggedGuestPhone);
            if (bill == null) {
                showAlert(
                    Alert.AlertType.INFORMATION,
                    "Checkout",
                    "You have no active booking."
                );
                return;
            }
            String result = hotel.checkoutByPhone(loggedGuestPhone);
            showAlert(Alert.AlertType.INFORMATION, "Checkout", result);
            refreshTable.run();
        });

        HBox actionBar = new HBox(15, billBtn, checkoutBtn);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(5, 0, 0, 0));

        // Main layout
        VBox body = new VBox(12, roomsLabel, table, bookForm, actionBar);
        body.setPadding(new Insets(20, 25, 25, 25));
        body.setStyle("-fx-background-color: #f5f5f5;");
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox root = new VBox(headerBar, body);
        VBox.setVgrow(body, Priority.ALWAYS);

        Scene scene = new Scene(root, 720, 540);
        scene.getStylesheets().add(cssUrl);
        primaryStage.setScene(scene);
    }

    // ==================== BILLING DIALOG  ====================
    private void showBillingDialog(Bill bill) {
        try {
            Parent fxmlRoot = FXMLLoader.load(
                getClass().getResource("billing.fxml")
            );
            Label check = (Label) fxmlRoot.lookup("#billRoomNumber");

            if (check != null) {
                check.setText(String.valueOf(bill.getRoomNum()));
                ((Label) fxmlRoot.lookup("#billCategory")).setText(
                    String.valueOf(bill.getCategory())
                );
                ((Label) fxmlRoot.lookup("#billBase")).setText(
                    String.format("Rs. %.2f", bill.getBase())
                );
                ((Label) fxmlRoot.lookup("#billService")).setText(
                    String.format("Rs. %.2f", bill.getService())
                );
                ((Label) fxmlRoot.lookup("#billTax")).setText(
                    String.format("Rs. %.2f", bill.getTax())
                );
                ((Label) fxmlRoot.lookup("#billTotal")).setText(
                    String.format("Rs. %.2f", bill.getTotal())
                );

                Dialog<Void> dialog = new Dialog<>();
                dialog.setTitle("Hotel Oasis - Bill");
                dialog.getDialogPane().setContent(fxmlRoot);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                dialog.getDialogPane().getStylesheets().add(cssUrl);
                dialog.showAndWait();
                return;
            }
        } catch (Exception ex) {}

        showBillingFallback(bill);
    }

    private void showBillingFallback(Bill bill) {
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));
        grid.setStyle(
            "-fx-background-color: #fffde7; -fx-border-color: #fbc02d; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;"
        );

        Label title = new Label("BILL - Hotel Oasis");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #1a237e;");
        grid.add(title, 0, 0, 2, 1);

        String[][] rows = {
            { "Room Number", String.valueOf(bill.getRoomNum()) },
            { "Category", String.valueOf(bill.getCategory()) },
            { "Base Amount", String.format("Rs. %.2f", bill.getBase()) },
            { "Service Charge", String.format("Rs. %.2f", bill.getService()) },
            { "Tax (12% GST)", String.format("Rs. %.2f", bill.getTax()) },
        };

        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0] + ":");
            key.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
            key.setStyle("-fx-text-fill: #424242;");

            Label val = new Label(rows[i][1]);
            val.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            val.setStyle("-fx-text-fill: #1565c0;");

            grid.add(key, 0, i + 1);
            grid.add(val, 1, i + 1);
        }

        Separator sep = new Separator();
        grid.add(sep, 0, rows.length + 1, 2, 1);

        Label totalKey = new Label("TOTAL:");
        totalKey.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        totalKey.setStyle("-fx-text-fill: #1b5e20;");

        Label totalVal = new Label(String.format("Rs. %.2f", bill.getTotal()));
        totalVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        totalVal.setStyle("-fx-text-fill: #1b5e20;");

        grid.add(totalKey, 0, rows.length + 2);
        grid.add(totalVal, 1, rows.length + 2);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Hotel Oasis - Bill");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
