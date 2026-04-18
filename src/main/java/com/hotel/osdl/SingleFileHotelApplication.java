package com.hotel.osdl;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SingleFileHotelApplication extends Application {
    private final HotelManager hotelManager = new HotelManager();

    private Label activeBookingsLabel;
    private Label availableRoomsLabel;
    private Label revenueLabel;
    private Label roomMixLabel;
    private Label featureSummaryLabel;

    private TableView<Booking> bookingTable;
    private TextField guestNameField;
    private TextField phoneField;
    private TextField emailField;
    private ComboBox<RoomType> roomTypeCombo;
    private ComboBox<Room> roomCombo;
    private DatePicker checkInPicker;
    private DatePicker checkOutPicker;
    private Spinner<Integer> adultsSpinner;
    private Spinner<Integer> childrenSpinner;
    private Spinner<Integer> breakfastSpinner;
    private Spinner<Integer> laundrySpinner;
    private Spinner<Integer> pickupSpinner;
    private Spinner<Integer> spaSpinner;
    private ComboBox<String> serviceCombo;
    private Spinner<Integer> serviceQtySpinner;
    private TextArea billPreviewArea;
    private Button checkoutButton;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f4eb, #edf2f8); -fx-font-family: 'Segoe UI';");
        root.setTop(createHeader());
        root.setCenter(createContent());

        Scene scene = new Scene(root, 1280, 760);
        stage.setTitle("OSDL Hotel Management - Single File");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();

        refreshScreen();
    }

    private HBox createHeader() {
        HBox topBar = new HBox(18);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(18, 24, 18, 24));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #17324d, #2f5d73);");

        VBox titleBox = new VBox(6);
        Label title = new Label("Hotel Management System");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitle = new Label("Single-file JavaFX version");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #d7e8f4;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button("Refresh Data");
        refreshButton.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; -fx-background-radius: 12px;");
        refreshButton.setOnAction(event -> refreshScreen());

        topBar.getChildren().addAll(titleBox, spacer, refreshButton);
        return topBar;
    }

    private HBox createContent() {
        HBox rootBox = new HBox(18);
        rootBox.setPadding(new Insets(20, 20, 20, 20));

        VBox bookingDesk = createBookingDesk();
        VBox rightSide = createRightSide();

        HBox.setHgrow(rightSide, Priority.ALWAYS);
        rootBox.getChildren().addAll(bookingDesk, rightSide);
        return rootBox;
    }

    private VBox createBookingDesk() {
        VBox box = new VBox(16);
        box.setPrefWidth(350);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.88); -fx-background-radius: 18px; -fx-border-color: #d5d9df; -fx-border-radius: 18px;");

        Label title = new Label("Booking Desk");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #17324d;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.getColumnConstraints().addAll(new ColumnConstraints(90), new ColumnConstraints(170));

        guestNameField = new TextField();
        phoneField = new TextField();
        emailField = new TextField();
        roomTypeCombo = new ComboBox<RoomType>();
        roomCombo = new ComboBox<Room>();
        checkInPicker = new DatePicker(LocalDate.now());
        checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        adultsSpinner = spinner(1, 10, 1);
        childrenSpinner = spinner(0, 10, 0);
        breakfastSpinner = spinner(0, 10, 0);
        laundrySpinner = spinner(0, 10, 0);
        pickupSpinner = spinner(0, 5, 0);
        spaSpinner = spinner(0, 5, 0);

        roomTypeCombo.setItems(FXCollections.observableArrayList(RoomType.values()));
        roomTypeCombo.getSelectionModel().select(RoomType.STANDARD);
        roomTypeCombo.setOnAction(event -> refreshAvailableRooms());

        addField(form, 0, "Guest Name", guestNameField);
        addField(form, 1, "Phone", phoneField);
        addField(form, 2, "Email", emailField);
        addField(form, 3, "Room Type", roomTypeCombo);
        addField(form, 4, "Room", roomCombo);
        addField(form, 5, "Check In", checkInPicker);
        addField(form, 6, "Check Out", checkOutPicker);
        addField(form, 7, "Adults", adultsSpinner);
        addField(form, 8, "Children", childrenSpinner);

        Label optionalTitle = new Label("Optional Services");
        optionalTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #17324d;");

        GridPane serviceGrid = new GridPane();
        serviceGrid.setHgap(12);
        serviceGrid.setVgap(10);
        serviceGrid.getColumnConstraints().addAll(new ColumnConstraints(110), new ColumnConstraints(120));

        addField(serviceGrid, 0, "Breakfast", breakfastSpinner);
        addField(serviceGrid, 1, "Laundry", laundrySpinner);
        addField(serviceGrid, 2, "Airport Pickup", pickupSpinner);
        addField(serviceGrid, 3, "Spa", spaSpinner);

        Button createBookingButton = new Button("Create Booking");
        createBookingButton.setMaxWidth(Double.MAX_VALUE);
        createBookingButton.setStyle("-fx-background-color: #d36c32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-padding: 10px 18px;");
        createBookingButton.setOnAction(event -> handleCreateBooking());

        box.getChildren().addAll(title, form, new Separator(), optionalTitle, serviceGrid, createBookingButton);
        return box;
    }

    private VBox createRightSide() {
        VBox box = new VBox(18);

        HBox stats = new HBox(14);
        activeBookingsLabel = statCard(stats, "Active Bookings", "0");
        availableRoomsLabel = statCard(stats, "Available Rooms", "0");
        revenueLabel = statCard(stats, "Total Revenue", "Rs.0.00");
        roomMixLabel = statCard(stats, "Room Mix", "Standard 0 | Deluxe 0 | Suite 0");

        featureSummaryLabel = new Label("Feature summary");
        featureSummaryLabel.setWrapText(true);
        featureSummaryLabel.setStyle("-fx-text-fill: #475867; -fx-font-size: 12px; -fx-padding: 4 8 4 8;");

        HBox contentRow = new HBox(18);
        VBox.setVgrow(contentRow, Priority.ALWAYS);

        VBox tableCard = new VBox(12);
        tableCard.setStyle("-fx-background-color: rgba(255,255,255,0.88); -fx-background-radius: 18px; -fx-border-color: #d5d9df; -fx-border-radius: 18px;");
        tableCard.setPadding(new Insets(20));
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        Label registerTitle = new Label("Booking Register");
        registerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #17324d;");

        bookingTable = new TableView<Booking>();
        VBox.setVgrow(bookingTable, Priority.ALWAYS);
        configureTable();
        bookingTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showBill(newValue));

        serviceCombo = new ComboBox<String>();
        serviceCombo.setItems(FXCollections.observableArrayList(hotelManager.getServiceRates().keySet()));
        serviceCombo.getSelectionModel().selectFirst();

        serviceQtySpinner = spinner(1, 10, 1);
        serviceQtySpinner.setPrefWidth(90);

        Button addServiceButton = new Button("Add Service");
        addServiceButton.setStyle("-fx-background-color: #2f5d73; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12px;");
        addServiceButton.setOnAction(event -> handleAddService());

        checkoutButton = new Button("Check Out");
        checkoutButton.setStyle("-fx-background-color: #ad3c2b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12px;");
        checkoutButton.setOnAction(event -> handleCheckout());

        HBox actionRow = new HBox(10, serviceCombo, serviceQtySpinner, addServiceButton, checkoutButton);
        HBox.setHgrow(serviceCombo, Priority.ALWAYS);

        tableCard.getChildren().addAll(registerTitle, bookingTable, actionRow);

        VBox billCard = new VBox(12);
        billCard.setPrefWidth(360);
        billCard.setStyle("-fx-background-color: rgba(255,255,255,0.88); -fx-background-radius: 18px; -fx-border-color: #d5d9df; -fx-border-radius: 18px;");
        billCard.setPadding(new Insets(20));

        Label billTitle = new Label("Bill Preview");
        billTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #17324d;");
        billPreviewArea = new TextArea();
        billPreviewArea.setEditable(false);
        billPreviewArea.setWrapText(true);
        VBox.setVgrow(billPreviewArea, Priority.ALWAYS);

        billCard.getChildren().addAll(billTitle, billPreviewArea);
        contentRow.getChildren().addAll(tableCard, billCard);

        box.getChildren().addAll(stats, featureSummaryLabel, contentRow);
        return box;
    }

    private Label statCard(HBox parent, String labelText, String valueText) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #f3f8ff); -fx-background-radius: 16px; -fx-border-color: #dce6f2; -fx-border-radius: 16px;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #5f6d7a; -fx-font-size: 12px;");
        Label value = new Label(valueText);
        value.setWrapText(true);
        value.setStyle("-fx-text-fill: #19324d; -fx-font-size: 22px; -fx-font-weight: bold;");

        card.getChildren().addAll(label, value);
        parent.getChildren().add(card);
        return value;
    }

    private void configureTable() {
        TableColumn<Booking, String> bookingIdColumn = new TableColumn<Booking, String>("Booking ID");
        bookingIdColumn.setPrefWidth(160);
        bookingIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));

        TableColumn<Booking, String> guestColumn = new TableColumn<Booking, String>("Guest");
        guestColumn.setPrefWidth(150);
        guestColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGuest().getName()));

        TableColumn<Booking, Integer> roomColumn = new TableColumn<Booking, Integer>("Room");
        roomColumn.setPrefWidth(90);
        roomColumn.setCellValueFactory(cell -> new SimpleObjectProperty<Integer>(cell.getValue().getRoom().getRoomNumber()));

        TableColumn<Booking, LocalDate> checkInColumn = new TableColumn<Booking, LocalDate>("Check In");
        checkInColumn.setPrefWidth(110);
        checkInColumn.setCellValueFactory(cell -> new SimpleObjectProperty<LocalDate>(cell.getValue().getCheckIn()));

        TableColumn<Booking, LocalDate> checkOutColumn = new TableColumn<Booking, LocalDate>("Check Out");
        checkOutColumn.setPrefWidth(110);
        checkOutColumn.setCellValueFactory(cell -> new SimpleObjectProperty<LocalDate>(cell.getValue().getCheckOut()));

        TableColumn<Booking, String> statusColumn = new TableColumn<Booking, String>("Status");
        statusColumn.setPrefWidth(110);
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().name()));

        bookingTable.getColumns().addAll(bookingIdColumn, guestColumn, roomColumn, checkInColumn, checkOutColumn, statusColumn);
    }

    private void addField(GridPane gridPane, int row, String labelText, javafx.scene.Node node) {
        Label label = new Label(labelText);
        gridPane.add(label, 0, row);
        gridPane.add(node, 1, row);
    }

    private Spinner<Integer> spinner(int min, int max, int initial) {
        Spinner<Integer> spinner = new Spinner<Integer>();
        spinner.setEditable(true);
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
        return spinner;
    }

    private void handleCreateBooking() {
        try {
            HashMap<String, Integer> selectedServices = new HashMap<String, Integer>();
            selectedServices.put("Breakfast", breakfastSpinner.getValue());
            selectedServices.put("Laundry", laundrySpinner.getValue());
            selectedServices.put("Airport Pickup", pickupSpinner.getValue());
            selectedServices.put("Spa", spaSpinner.getValue());

            Booking booking = hotelManager.createBooking(
                    guestNameField.getText(),
                    phoneField.getText(),
                    emailField.getText(),
                    roomCombo.getValue(),
                    checkInPicker.getValue(),
                    checkOutPicker.getValue(),
                    adultsSpinner.getValue().intValue(),
                    childrenSpinner.getValue().intValue(),
                    selectedServices
            );

            bookingTable.getSelectionModel().select(booking);
            clearBookingForm();
            showInfo("Booking created", "Booking " + booking.getId() + " was added successfully.");
            refreshScreen();
        } catch (ValidationException exception) {
            showError(exception.getMessage());
        }
    }

    private void handleAddService() {
        try {
            hotelManager.addServiceToBooking(
                    bookingTable.getSelectionModel().getSelectedItem(),
                    serviceCombo.getValue(),
                    serviceQtySpinner.getValue().intValue()
            );
            showInfo("Service added", "Service charge has been added to the selected booking.");
            refreshScreen();
        } catch (ValidationException exception) {
            showError(exception.getMessage());
        }
    }

    private void handleCheckout() {
        try {
            Booking booking = bookingTable.getSelectionModel().getSelectedItem();
            hotelManager.checkoutBooking(booking);
            showInfo("Checked out", "Invoice saved to the invoices folder for " + booking.getId() + ".");
            refreshScreen();
        } catch (ValidationException exception) {
            showError(exception.getMessage());
        }
    }

    private void refreshScreen() {
        bookingTable.setItems(FXCollections.observableArrayList(hotelManager.getBookings()));
        activeBookingsLabel.setText(String.valueOf(hotelManager.getActiveBookings().size()));
        availableRoomsLabel.setText(String.valueOf(hotelManager.getAvailableRoomCount()));
        revenueLabel.setText("Rs." + String.format("%.2f", hotelManager.getTotalRevenue()));
        roomMixLabel.setText(hotelManager.getRoomMix());
        featureSummaryLabel.setText(hotelManager.getFeatureSummary());
        refreshAvailableRooms();

        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showBill(selected);
        } else {
            billPreviewArea.setText("Select a booking to preview the bill.");
            checkoutButton.setDisable(true);
        }
    }

    private void refreshAvailableRooms() {
        RoomType selectedType = roomTypeCombo.getValue();
        if (selectedType == null) {
            selectedType = RoomType.STANDARD;
        }
        roomCombo.setItems(FXCollections.observableArrayList(hotelManager.getAvailableRooms(selectedType)));
        if (!roomCombo.getItems().isEmpty()) {
            roomCombo.getSelectionModel().selectFirst();
        }
    }

    private void showBill(Booking booking) {
        try {
            billPreviewArea.setText(hotelManager.createInvoiceText(booking));
            checkoutButton.setDisable(booking == null || booking.getStatus() == BookingStatus.CHECKED_OUT);
        } catch (ValidationException exception) {
            billPreviewArea.setText(exception.getMessage());
            checkoutButton.setDisable(true);
        }
    }

    private void clearBookingForm() {
        guestNameField.clear();
        phoneField.clear();
        emailField.clear();
        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));
        adultsSpinner.getValueFactory().setValue(Integer.valueOf(1));
        childrenSpinner.getValueFactory().setValue(Integer.valueOf(0));
        breakfastSpinner.getValueFactory().setValue(Integer.valueOf(0));
        laundrySpinner.getValueFactory().setValue(Integer.valueOf(0));
        pickupSpinner.getValueFactory().setValue(Integer.valueOf(0));
        spaSpinner.getValueFactory().setValue(Integer.valueOf(0));
        refreshAvailableRooms();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Validation error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public interface Billable {
        String getBillLabel();
        double getBillAmount();
    }

    public interface Amenities {
        String provideWifi();
        String provideBreakfast();
    }

    public static abstract class Person implements Serializable {
        private String id;
        private String name;
        private String phone;

        public Person(String id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }
    }

    public static class Guest extends Person {
        private String email;

        public Guest(String id, String name, String phone, String email) {
            super(id, name, phone);
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }

    public enum RoomType {
        STANDARD(2200.0),
        DELUXE(3800.0),
        SUITE(5600.0);

        private Double baseTariff;

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

    public enum BookingStatus {
        ACTIVE,
        CHECKED_OUT
    }

    public static abstract class Room implements Serializable, Billable {
        private Integer roomNumber;
        private RoomType roomType;
        private Double basePrice;
        private Boolean booked;
        private String guestName;

        public Room(Integer roomNumber, RoomType roomType, Double basePrice, Boolean booked, String guestName) {
            this.roomNumber = roomNumber;
            this.roomType = roomType;
            this.basePrice = basePrice;
            this.booked = booked;
            this.guestName = guestName;
        }

        public Integer getRoomNumber() {
            return roomNumber;
        }

        public RoomType getRoomType() {
            return roomType;
        }

        public Double getBasePrice() {
            return basePrice;
        }

        public Boolean getBooked() {
            return booked;
        }

        public void setBooked(Boolean booked) {
            this.booked = booked;
        }

        public String getGuestName() {
            return guestName;
        }

        public void setGuestName(String guestName) {
            this.guestName = guestName;
        }

        public abstract Double calculateTariff(Integer days);

        @Override
        public String getBillLabel() {
            return "Room " + roomNumber + " stay";
        }

        @Override
        public double getBillAmount() {
            return basePrice.doubleValue();
        }

        @Override
        public String toString() {
            return roomNumber + " - " + roomType + " (Rs." + String.format("%.0f", basePrice.doubleValue()) + "/night)";
        }
    }

    public static class StandardRoom extends Room implements Amenities {
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

    public static class DeluxeRoom extends Room implements Amenities {
        public DeluxeRoom(Integer roomNumber, Double basePrice, Boolean booked, String guestName) {
            super(roomNumber, RoomType.DELUXE, basePrice, booked, guestName);
        }

        @Override
        public Double calculateTariff(Integer days) {
            return Double.valueOf(getBasePrice().doubleValue() * days.intValue() + (400.0 * days.intValue()));
        }

        @Override
        public String provideWifi() {
            return "Free Wi-Fi";
        }

        @Override
        public String provideBreakfast() {
            return "Complimentary breakfast";
        }
    }

    public static class SuiteRoom extends Room implements Amenities {
        public SuiteRoom(Integer roomNumber, Double basePrice, Boolean booked, String guestName) {
            super(roomNumber, RoomType.SUITE, basePrice, booked, guestName);
        }

        @Override
        public Double calculateTariff(Integer days) {
            return Double.valueOf(getBasePrice().doubleValue() * days.intValue() + (900.0 * days.intValue()));
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

    public static class ServiceItem implements Billable, Serializable {
        private String bookingId;
        private String name;
        private Integer quantity;
        private Double unitPrice;

        public ServiceItem(String bookingId, String name, Integer quantity, Double unitPrice) {
            this.bookingId = bookingId;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getBookingId() {
            return bookingId;
        }

        public String getName() {
            return name;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        @Override
        public String getBillLabel() {
            return name + " x" + quantity;
        }

        @Override
        public double getBillAmount() {
            return Double.valueOf(quantity.intValue() * unitPrice.doubleValue()).doubleValue();
        }
    }

    public static class Booking implements Billable, Serializable {
        private String id;
        private Guest guest;
        private Room room;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer adults;
        private Integer children;
        private BookingStatus status;
        private ArrayList<ServiceItem> serviceItems = new ArrayList<ServiceItem>();

        public Booking(String id, Guest guest, Room room, LocalDate checkIn, LocalDate checkOut, Integer adults, Integer children, BookingStatus status) {
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

        public ArrayList<ServiceItem> getServiceItems() {
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
            int index = 0;
            while (index < serviceItems.size()) {
                total = Double.valueOf(total.doubleValue() + serviceItems.get(index).getBillAmount());
                index++;
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
            return getRoomCharge().doubleValue();
        }
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static class Pair<T, U> {
        private T first;
        private U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public U getSecond() {
            return second;
        }
    }

    public static class RoomDetail<T, U> {
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

    public static class NumberCalculator<T extends Number> {
        private T first;
        private T second;

        public NumberCalculator(T first, T second) {
            this.first = first;
            this.second = second;
        }

        public Double subtract() {
            return Double.valueOf(first.doubleValue() - second.doubleValue());
        }
    }

    public static class DisplayHelper {
        public static <T> String display(T value) {
            return String.valueOf(value);
        }

        public static <T> String printArray(T[] values) {
            StringBuilder builder = new StringBuilder();
            int index = 0;
            while (index < values.length) {
                builder.append(values[index]);
                if (index < values.length - 1) {
                    builder.append(", ");
                }
                index++;
            }
            return builder.toString();
        }
    }

    public static class ServiceSimulationThread extends Thread {
        public void run() {
            int step = 1;
            while (step <= 2) {
                Thread.yield();
                try {
                    Thread.sleep(60);
                } catch (InterruptedException exception) {
                    interrupt();
                }
                step++;
            }
        }
    }

    public static class PaymentProcessingTask implements Runnable {
        @Override
        public void run() {
            int step = 1;
            while (step <= 2) {
                try {
                    Thread.sleep(60);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
                step++;
            }
        }
    }

    public static class RoomAllocationMonitor {
        private HashMap<Integer, Boolean> roomStatusMap = new HashMap<Integer, Boolean>();

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

    public static class HotelManager {
        private static final int ROOM_TYPE_SIZE = 20;
        private static final int GUEST_NAME_SIZE = 30;
        private static final int ROOM_RECORD_SIZE = 4 + (ROOM_TYPE_SIZE * 2) + 8 + 1 + (GUEST_NAME_SIZE * 2);

        private HashMap<String, Double> serviceRates = new HashMap<String, Double>();
        private ArrayList<Room> rooms = new ArrayList<Room>();
        private ArrayList<Booking> bookings = new ArrayList<Booking>();
        private HashMap<Integer, Guest> roomCustomerMap = new HashMap<Integer, Guest>();
        private RoomAllocationMonitor allocationMonitor = new RoomAllocationMonitor();

        private File roomsFile = new File("data/single-rooms.dat");
        private File bookingsFile = new File("data/single-bookings.txt");
        private File servicesFile = new File("data/single-services.txt");
        private File bookingObjectFile = new File("data/single-bookings.ser");
        private File invoiceDirectory = new File("invoices");

        public HotelManager() {
            loadServiceRates();
            ensureSeedData();
            loadData();
        }

        public Map<String, Double> getServiceRates() {
            return serviceRates;
        }

        public ArrayList<Room> getAvailableRooms(RoomType roomType) {
            ArrayList<Room> availableRooms = new ArrayList<Room>();
            Iterator<Room> iterator = rooms.iterator();
            while (iterator.hasNext()) {
                Room room = iterator.next();
                if (room.getRoomType() == roomType && !room.getBooked().booleanValue()) {
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
            return activeBookings;
        }

        public long getAvailableRoomCount() {
            return Long.valueOf(rooms.size() - getActiveBookings().size()).longValue();
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

        public String getRoomMix() {
            HashMap<RoomType, Long> counts = new HashMap<RoomType, Long>();
            int index = 0;
            while (index < rooms.size()) {
                RoomType roomType = rooms.get(index).getRoomType();
                Long count = counts.get(roomType);
                if (count == null) {
                    counts.put(roomType, Long.valueOf(1));
                } else {
                    counts.put(roomType, Long.valueOf(count.longValue() + 1));
                }
                index++;
            }
            return "Standard " + counts.getOrDefault(RoomType.STANDARD, Long.valueOf(0))
                    + " | Deluxe " + counts.getOrDefault(RoomType.DELUXE, Long.valueOf(0))
                    + " | Suite " + counts.getOrDefault(RoomType.SUITE, Long.valueOf(0));
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
                HashMap<String, Integer> selectedServices
        ) throws ValidationException {
            validateBookingInput(guestName, phone, room, checkIn, checkOut, adults, children);
            runBackgroundTasks();

            Guest guest = new Guest(nextId("GST"), guestName.trim(), phone.trim(), email == null ? "" : email.trim());
            Booking booking = new Booking(
                    nextId("BKG"),
                    guest,
                    room,
                    checkIn,
                    checkOut,
                    Integer.valueOf(adults),
                    Integer.valueOf(children),
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
            updateRoomStatus(room.getRoomNumber(), Boolean.TRUE, guest.getName());

            for (Map.Entry<String, Integer> entry : selectedServices.entrySet()) {
                if (entry.getValue() != null && entry.getValue().intValue() > 0) {
                    booking.addServiceItem(new ServiceItem(booking.getId(), entry.getKey(), entry.getValue(), serviceRates.get(entry.getKey())));
                }
            }

            bookings.add(booking);
            persist();
            return booking;
        }

        public void addServiceToBooking(Booking booking, String serviceName, int quantity) throws ValidationException {
            if (booking == null) {
                throw new ValidationException("Select a booking before adding a service.");
            }
            if (booking.getStatus() != BookingStatus.ACTIVE) {
                throw new ValidationException("Services can only be added to active bookings.");
            }
            if (!serviceRates.containsKey(serviceName) || quantity <= 0) {
                throw new ValidationException("Choose a valid service and quantity.");
            }

            booking.addServiceItem(new ServiceItem(booking.getId(), serviceName, Integer.valueOf(quantity), serviceRates.get(serviceName)));
            persist();
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
            updateRoomStatus(booking.getRoom().getRoomNumber(), Boolean.FALSE, "");
            saveInvoice(booking.getId(), createInvoiceText(booking));
            persist();
        }

        public String createInvoiceText(Booking booking) throws ValidationException {
            if (booking == null) {
                throw new ValidationException("Select a booking to generate the bill.");
            }

            StringBuilder builder = new StringBuilder();
            builder.append("OSDL HOTEL MANAGEMENT\n");
            builder.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))).append("\n\n");
            builder.append("Booking ID: ").append(booking.getId()).append("\n");
            builder.append("Guest: ").append(booking.getGuest().getName()).append("\n");
            builder.append("Phone: ").append(booking.getGuest().getPhone()).append("\n");
            builder.append("Room: ").append(booking.getRoom().getRoomNumber()).append(" - ").append(booking.getRoom().getRoomType()).append("\n");
            builder.append("Stay: ").append(booking.getCheckIn()).append(" to ").append(booking.getCheckOut()).append(" (").append(booking.getNightCount()).append(" nights)\n\n");
            builder.append(String.format("%-30s %10s%n", "Charge Item", "Amount"));
            builder.append("---------------------------------------------\n");
            builder.append(String.format("%-30s %10.2f%n", booking.getBillLabel(), booking.getBillAmount()));
            int index = 0;
            while (index < booking.getServiceItems().size()) {
                ServiceItem item = booking.getServiceItems().get(index);
                builder.append(String.format("%-30s %10.2f%n", item.getBillLabel(), item.getBillAmount()));
                index++;
            }
            builder.append(String.format("%-30s %10.2f%n", "Tax (12%)", booking.getTaxAmount()));
            builder.append("---------------------------------------------\n");
            builder.append(String.format("%-30s %10.2f%n", "Grand Total", booking.getGrandTotal()));
            return builder.toString();
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

        private void validateBookingInput(
                String guestName,
                String phone,
                Room room,
                LocalDate checkIn,
                LocalDate checkOut,
                int adults,
                int children
        ) throws ValidationException {
            if (guestName == null || guestName.trim().isEmpty()) {
                throw new ValidationException("Guest name is required.");
            }
            if (phone == null || phone.trim().isEmpty()) {
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

        private void loadServiceRates() {
            serviceRates.put("Breakfast", Double.valueOf(250.0));
            serviceRates.put("Laundry", Double.valueOf(300.0));
            serviceRates.put("Airport Pickup", Double.valueOf(900.0));
            serviceRates.put("Spa", Double.valueOf(1200.0));
        }

        private void ensureSeedData() {
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
                seedRooms.add(new DeluxeRoom(Integer.valueOf(201), Double.valueOf(3800.0), Boolean.FALSE, ""));
                seedRooms.add(new DeluxeRoom(Integer.valueOf(202), Double.valueOf(3900.0), Boolean.FALSE, ""));
                seedRooms.add(new DeluxeRoom(Integer.valueOf(203), Double.valueOf(4100.0), Boolean.FALSE, ""));
                seedRooms.add(new SuiteRoom(Integer.valueOf(301), Double.valueOf(5600.0), Boolean.FALSE, ""));
                seedRooms.add(new SuiteRoom(Integer.valueOf(302), Double.valueOf(5900.0), Boolean.FALSE, ""));
                saveRooms(seedRooms);
            }
            createTextFileIfMissing(bookingsFile);
            createTextFileIfMissing(servicesFile);
        }

        private void loadData() {
            rooms.clear();
            rooms.addAll(loadRooms());
            int roomIndex = 0;
            while (roomIndex < rooms.size()) {
                allocationMonitor.registerRoom(rooms.get(roomIndex).getRoomNumber(), rooms.get(roomIndex).getBooked());
                roomIndex++;
            }

            bookings.clear();
            bookings.addAll(loadBookings(rooms));

            ArrayList<Booking> serializedBookings = deserializeBookings();
            int bookingIndex = 0;
            while (bookingIndex < serializedBookings.size()) {
                Booking booking = serializedBookings.get(bookingIndex);
                if (booking.getStatus() == BookingStatus.ACTIVE) {
                    roomCustomerMap.put(booking.getRoom().getRoomNumber(), booking.getGuest());
                }
                bookingIndex++;
            }
        }

        private void persist() {
            saveRooms(rooms);
            saveBookings(bookings);
            ArrayList<ServiceItem> allServices = new ArrayList<ServiceItem>();
            int index = 0;
            while (index < bookings.size()) {
                allServices.addAll(bookings.get(index).getServiceItems());
                index++;
            }
            saveServices(allServices);
            serializeBookings(bookings);
        }

        private ArrayList<Room> loadRooms() {
            ArrayList<Room> loadedRooms = new ArrayList<Room>();
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(roomsFile, "r");
                while (randomAccessFile.getFilePointer() < randomAccessFile.length()) {
                    Integer roomNumber = Integer.valueOf(randomAccessFile.readInt());
                    String roomType = readFixedString(randomAccessFile, ROOM_TYPE_SIZE).trim();
                    Double price = Double.valueOf(randomAccessFile.readDouble());
                    Boolean booked = Boolean.valueOf(randomAccessFile.readBoolean());
                    String guestName = readFixedString(randomAccessFile, GUEST_NAME_SIZE).trim();
                    loadedRooms.add(createRoom(roomNumber, RoomType.valueOf(roomType), price, booked, guestName));
                }
            } catch (EOFException ignored) {
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to read room records.", exception);
            } finally {
                closeRandomAccessFile(randomAccessFile);
            }
            return loadedRooms;
        }

        private void saveRooms(ArrayList<Room> roomList) {
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(roomsFile, "rw");
                randomAccessFile.setLength(0);
                int index = 0;
                while (index < roomList.size()) {
                    Room room = roomList.get(index);
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

        private void updateRoomStatus(Integer roomNumber, Boolean booked, String guestName) {
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

        private ArrayList<Booking> loadBookings(ArrayList<Room> roomList) {
            ArrayList<Booking> loadedBookings = new ArrayList<Booking>();
            FileReader fileReader = null;
            try {
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
                        Room room = findRoomInList(roomList, Integer.valueOf(parts[5]));
                        Booking booking = new Booking(
                                parts[0],
                                new Guest(parts[1], parts[2], parts[3], parts[4]),
                                room,
                                LocalDate.parse(parts[6]),
                                LocalDate.parse(parts[7]),
                                Integer.valueOf(parts[8]),
                                Integer.valueOf(parts[9]),
                                BookingStatus.valueOf(parts[10])
                        );
                        loadedBookings.add(booking);
                    }
                    index++;
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to read bookings file.", exception);
            } finally {
                closeFileReader(fileReader);
            }
            attachServices(loadedBookings);
            return loadedBookings;
        }

        private void saveBookings(ArrayList<Booking> bookingList) {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(bookingsFile, false);
                int index = 0;
                while (index < bookingList.size()) {
                    Booking booking = bookingList.get(index);
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
                throw new IllegalStateException("Unable to write bookings file.", exception);
            } finally {
                closeFileWriter(fileWriter);
            }
        }

        private void saveServices(ArrayList<ServiceItem> services) {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(servicesFile, false);
                int index = 0;
                while (index < services.size()) {
                    ServiceItem item = services.get(index);
                    fileWriter.write(item.getBookingId() + "|" + sanitize(item.getName()) + "|" + item.getQuantity() + "|" + item.getUnitPrice() + System.lineSeparator());
                    index++;
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to write services file.", exception);
            } finally {
                closeFileWriter(fileWriter);
            }
        }

        private void serializeBookings(ArrayList<Booking> bookingList) {
            FileOutputStream fileOutputStream = null;
            ObjectOutputStream objectOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(bookingObjectFile);
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(bookingList);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to serialize bookings.", exception);
            } finally {
                closeObjectOutputStream(objectOutputStream);
                closeFileOutputStream(fileOutputStream);
            }
        }

        @SuppressWarnings("unchecked")
        private ArrayList<Booking> deserializeBookings() {
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

        private void saveInvoice(String bookingId, String content) {
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

        private void attachServices(ArrayList<Booking> bookingList) {
            FileReader fileReader = null;
            try {
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
                        Booking booking = findBooking(bookingList, parts[0]);
                        if (booking != null) {
                            booking.addServiceItem(new ServiceItem(parts[0], parts[1], Integer.valueOf(parts[2]), Double.valueOf(parts[3])));
                        }
                    }
                    index++;
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to read services file.", exception);
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
                return new DeluxeRoom(roomNumber, price, booked, guestName);
            }
            if (roomType == RoomType.SUITE) {
                return new SuiteRoom(roomNumber, price, booked, guestName);
            }
            return new StandardRoom(roomNumber, price, booked, guestName);
        }

        private Room findRoomInList(ArrayList<Room> roomList, Integer roomNumber) {
            int index = 0;
            while (index < roomList.size()) {
                Room room = roomList.get(index);
                if (room.getRoomNumber().intValue() == roomNumber.intValue()) {
                    return room;
                }
                index++;
            }
            return null;
        }

        private Booking findBooking(ArrayList<Booking> bookingList, String bookingId) {
            int index = 0;
            while (index < bookingList.size()) {
                Booking booking = bookingList.get(index);
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

        private void runBackgroundTasks() {
            ServiceSimulationThread serviceThread = new ServiceSimulationThread();
            Thread paymentThread = new Thread(new PaymentProcessingTask());
            serviceThread.start();
            paymentThread.start();
            try {
                serviceThread.join();
                paymentThread.join();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        private String nextId(String prefix) {
            return prefix + "-" + System.currentTimeMillis();
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
}
