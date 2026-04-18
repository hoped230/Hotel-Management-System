package com.hotel.osdl.controller;

import com.hotel.osdl.model.Booking;
import com.hotel.osdl.model.Room;
import com.hotel.osdl.model.RoomType;
import com.hotel.osdl.service.HotelService;
import com.hotel.osdl.service.ValidationException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainController {
    @FXML private Label activeBookingsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label revenueLabel;
    @FXML private Label roomMixLabel;
    @FXML private Label featureSummaryLabel;

    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> bookingIdColumn;
    @FXML private TableColumn<Booking, String> guestColumn;
    @FXML private TableColumn<Booking, Integer> roomColumn;
    @FXML private TableColumn<Booking, LocalDate> checkInColumn;
    @FXML private TableColumn<Booking, LocalDate> checkOutColumn;
    @FXML private TableColumn<Booking, String> statusColumn;

    @FXML private TextField guestNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<RoomType> roomTypeCombo;
    @FXML private ComboBox<Room> roomCombo;
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private Spinner<Integer> adultsSpinner;
    @FXML private Spinner<Integer> childrenSpinner;
    @FXML private Spinner<Integer> breakfastSpinner;
    @FXML private Spinner<Integer> laundrySpinner;
    @FXML private Spinner<Integer> pickupSpinner;
    @FXML private Spinner<Integer> spaSpinner;

    @FXML private ComboBox<String> serviceCombo;
    @FXML private Spinner<Integer> serviceQtySpinner;
    @FXML private TextArea billPreviewArea;
    @FXML private Button checkoutButton;

    private final HotelService hotelService = HotelService.getInstance();

    @FXML
    private void initialize() {
        configureSpinners();
        configureTable();

        roomTypeCombo.setItems(FXCollections.observableArrayList(RoomType.values()));
        roomTypeCombo.getSelectionModel().select(RoomType.STANDARD);
        roomTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> refreshAvailableRooms());

        serviceCombo.setItems(FXCollections.observableArrayList(hotelService.getServiceRates().keySet()));
        serviceCombo.getSelectionModel().selectFirst();

        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));

        bookingTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showBill(newValue));
        hotelService.addListener(this::refreshScreen);
        refreshScreen();
    }

    @FXML
    private void handleCreateBooking() {
        try {
            Map<String, Integer> selectedServices = new LinkedHashMap<>();
            selectedServices.put("Breakfast", breakfastSpinner.getValue());
            selectedServices.put("Laundry", laundrySpinner.getValue());
            selectedServices.put("Airport Pickup", pickupSpinner.getValue());
            selectedServices.put("Spa", spaSpinner.getValue());

            Booking booking = hotelService.createBooking(
                    guestNameField.getText(),
                    phoneField.getText(),
                    emailField.getText(),
                    roomCombo.getValue(),
                    checkInPicker.getValue(),
                    checkOutPicker.getValue(),
                    adultsSpinner.getValue(),
                    childrenSpinner.getValue(),
                    selectedServices
            );

            bookingTable.getSelectionModel().select(booking);
            clearBookingForm();
            showInfo("Booking created", "Booking " + booking.getId() + " was added successfully.");
        } catch (ValidationException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleAddService() {
        try {
            hotelService.addServiceToBooking(
                    bookingTable.getSelectionModel().getSelectedItem(),
                    serviceCombo.getValue(),
                    serviceQtySpinner.getValue()
            );
            showInfo("Service added", "Service charge has been added to the selected booking.");
        } catch (ValidationException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleCheckout() {
        try {
            Booking booking = bookingTable.getSelectionModel().getSelectedItem();
            hotelService.checkoutBooking(booking);
            showInfo("Checked out", "Invoice saved to the invoices folder for " + booking.getId() + ".");
        } catch (ValidationException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        refreshScreen();
    }

    private void configureSpinners() {
        adultsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        childrenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        breakfastSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        laundrySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        pickupSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        spaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        serviceQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
    }

    private void configureTable() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        guestColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGuest().getName()));
        roomColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getRoom().getRoomNumber()));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().name()));
    }

    private void refreshScreen() {
        bookingTable.setItems(FXCollections.observableArrayList(hotelService.getBookings()));
        activeBookingsLabel.setText(String.valueOf(hotelService.getActiveBookings().size()));
        availableRoomsLabel.setText(String.valueOf(hotelService.getAvailableRoomCount()));
        revenueLabel.setText("Rs." + String.format("%.2f", hotelService.getTotalRevenue()));
        roomMixLabel.setText(formatRoomMix());
        featureSummaryLabel.setText(hotelService.getFeatureSummary());
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
        RoomType selectedType = roomTypeCombo.getValue() == null ? RoomType.STANDARD : roomTypeCombo.getValue();
        roomCombo.setItems(FXCollections.observableArrayList(hotelService.getAvailableRooms(selectedType)));
        if (!roomCombo.getItems().isEmpty()) {
            roomCombo.getSelectionModel().selectFirst();
        }
    }

    private void showBill(Booking booking) {
        try {
            billPreviewArea.setText(hotelService.createInvoiceText(booking));
            checkoutButton.setDisable(booking == null || booking.getStatus().name().equals("CHECKED_OUT"));
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
        adultsSpinner.getValueFactory().setValue(1);
        childrenSpinner.getValueFactory().setValue(0);
        breakfastSpinner.getValueFactory().setValue(0);
        laundrySpinner.getValueFactory().setValue(0);
        pickupSpinner.getValueFactory().setValue(0);
        spaSpinner.getValueFactory().setValue(0);
        refreshAvailableRooms();
    }

    private String formatRoomMix() {
        Map<RoomType, Long> counts = hotelService.getRoomTypeCounts();
        return "Standard " + counts.getOrDefault(RoomType.STANDARD, 0L)
                + " | Deluxe " + counts.getOrDefault(RoomType.DELUXE, 0L)
                + " | Suite " + counts.getOrDefault(RoomType.SUITE, 0L);
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
}
