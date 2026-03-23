package com.example.demo64965;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HelloController {

    // --- Sidebar and main container ---
    @FXML private TabPane tabPane;

    // --- Dashboard components ---
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label upcomingAppointmentsLabel;
    @FXML private TableView<Appointment> upcomingTable;
    @FXML private TableColumn<Appointment, String> upcomingDateCol;
    @FXML private TableColumn<Appointment, String> upcomingTimeCol;
    @FXML private TableColumn<Appointment, String> upcomingDoctorCol;
    @FXML private TableColumn<Appointment, String> upcomingPatientCol;
    @FXML private ListView<Doctor> todayDoctorsListView;

    // --- Doctors tab ---
    @FXML private TextField newDoctorField;
    @FXML private ListView<Doctor> doctorListView;

    // --- Patients tab ---
    @FXML private TextField newPatientField;
    @FXML private ListView<Patient> patientListView;

    // --- Appointments tab ---
    @FXML private ComboBox<Doctor> doctorComboBox;
    @FXML private ComboBox<Patient> patientComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> appDoctorCol;
    @FXML private TableColumn<Appointment, String> appPatientCol;
    @FXML private TableColumn<Appointment, String> appDateCol;
    @FXML private TableColumn<Appointment, String> appTimeCol;
    @FXML private TableColumn<Appointment, Void> appActionsCol;

    // --- Filters ---
    @FXML private ComboBox<Doctor> filterDoctorComboBox;
    @FXML private ComboBox<Patient> filterPatientComboBox;
    @FXML private DatePicker filterDatePicker;

    // --- Schedule tab ---
    @FXML private ComboBox<Doctor> scheduleDoctorComboBox;
    @FXML private CheckBox monCheckBox, tueCheckBox, wedCheckBox, thuCheckBox, friCheckBox, satCheckBox, sunCheckBox;
    @FXML private Label scheduleMessageLabel;

    private ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private ObservableList<Patient> patients = FXCollections.observableArrayList();
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private FilteredList<Appointment> filteredAppointments;

    @FXML
    public void initialize() {
        // Load data from CSV
        List<Doctor> loadedDoctors = FileManager.loadDoctors();
        if (loadedDoctors.isEmpty()) {
            Collections.addAll(loadedDoctors,
                    new Doctor("Dr. Smith"), new Doctor("Dr. Brown"), new Doctor("Dr. Lee"));
            loadedDoctors.forEach(FileManager::saveDoctor);
        }
        doctors.setAll(loadedDoctors);

        List<Patient> loadedPatients = FileManager.loadPatients();
        if (loadedPatients.isEmpty()) {
            Collections.addAll(loadedPatients,
                    new Patient("Alice"), new Patient("Bob"), new Patient("Charlie"));
            loadedPatients.forEach(FileManager::savePatient);
        }
        patients.setAll(loadedPatients);

        List<Appointment> loadedAppointments = FileManager.loadAppointments();
        appointments.setAll(loadedAppointments);

        // Setup Doctors tab
        doctorListView.setItems(doctors);
        doctorListView.setEditable(true);
        doctorListView.setCellFactory(TextFieldListCell.forListView(new StringConverter<Doctor>() {
            @Override public String toString(Doctor d) { return d.getName(); }
            @Override public Doctor fromString(String s) { return new Doctor(s); }
        }));
        doctorListView.setOnEditCommit(event -> {
            Doctor doctor = doctorListView.getItems().get(event.getIndex());
            doctor.setName(event.getNewValue().getName());
            FileManager.saveDoctor(doctor);
            refreshAll();
        });
        doctorListView.setContextMenu(createDoctorContextMenu());

        // Setup Patients tab
        patientListView.setItems(patients);
        patientListView.setEditable(true);
        patientListView.setCellFactory(TextFieldListCell.forListView(new StringConverter<Patient>() {
            @Override public String toString(Patient p) { return p.getName(); }
            @Override public Patient fromString(String s) { return new Patient(s); }
        }));
        patientListView.setOnEditCommit(event -> {
            Patient patient = patientListView.getItems().get(event.getIndex());
            patient.setName(event.getNewValue().getName());
            FileManager.savePatient(patient);
            refreshAll();
        });
        patientListView.setContextMenu(createPatientContextMenu());

        // Setup Appointments tab
        doctorComboBox.setItems(doctors);
        patientComboBox.setItems(patients);
        filterDoctorComboBox.setItems(doctors);
        filterPatientComboBox.setItems(patients);
        filterDoctorComboBox.getSelectionModel().select(null);
        filterPatientComboBox.getSelectionModel().select(null);

        // Configure DatePicker to disable past dates
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        // Also for filter date picker (optional)
        filterDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        filteredAppointments = new FilteredList<>(appointments, p -> true);
        appointmentTable.setItems(filteredAppointments);

        // Configure appointment table columns
        appDoctorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctor().getName()));
        appPatientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatient().getName()));
        appDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        appTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime().toString()));

        // Delete button with confirmation
        appActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.getStyleClass().add("button-delete");
                deleteButton.setOnAction(event -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Appointment");
                    confirm.setHeaderText("Delete this appointment?");
                    confirm.setContentText("Doctor: " + app.getDoctor().getName() +
                            "\nPatient: " + app.getPatient().getName() +
                            "\nDate: " + app.getDate() +
                            "\nTime: " + app.getTime());
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            FileManager.deleteAppointment(app);
                            refreshAll();
                        }
                    });
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteButton);
            }
        });

        // Filter listeners
        filterDoctorComboBox.valueProperty().addListener((obs, old, val) -> applyFilters());
        filterPatientComboBox.valueProperty().addListener((obs, old, val) -> applyFilters());
        filterDatePicker.valueProperty().addListener((obs, old, val) -> applyFilters());

        // Setup Schedule tab
        scheduleDoctorComboBox.setItems(doctors);
        scheduleDoctorComboBox.valueProperty().addListener((obs, old, newVal) -> loadScheduleForDoctor(newVal));

        // Setup dashboard and refresh
        refreshAll();
    }

    // --- Sidebar navigation methods ---
    @FXML private void showDashboard() { tabPane.getSelectionModel().select(0); }
    @FXML private void showDoctors() { tabPane.getSelectionModel().select(1); }
    @FXML private void showPatients() { tabPane.getSelectionModel().select(2); }
    @FXML private void showAppointments() { tabPane.getSelectionModel().select(3); }
    @FXML private void showSchedule() { tabPane.getSelectionModel().select(4); }

    // --- Schedule methods ---
    private void loadScheduleForDoctor(Doctor doctor) {
        if (doctor == null) return;
        Set<DayOfWeek> days = doctor.getWorkingDays();
        monCheckBox.setSelected(days.contains(DayOfWeek.MONDAY));
        tueCheckBox.setSelected(days.contains(DayOfWeek.TUESDAY));
        wedCheckBox.setSelected(days.contains(DayOfWeek.WEDNESDAY));
        thuCheckBox.setSelected(days.contains(DayOfWeek.THURSDAY));
        friCheckBox.setSelected(days.contains(DayOfWeek.FRIDAY));
        satCheckBox.setSelected(days.contains(DayOfWeek.SATURDAY));
        sunCheckBox.setSelected(days.contains(DayOfWeek.SUNDAY));
    }

    @FXML
    private void updateDoctorSchedule() {
        Doctor selected = scheduleDoctorComboBox.getValue();
        if (selected == null) {
            scheduleMessageLabel.setText("Please select a doctor.");
            scheduleMessageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        Set<DayOfWeek> newDays = new HashSet<>();
        if (monCheckBox.isSelected()) newDays.add(DayOfWeek.MONDAY);
        if (tueCheckBox.isSelected()) newDays.add(DayOfWeek.TUESDAY);
        if (wedCheckBox.isSelected()) newDays.add(DayOfWeek.WEDNESDAY);
        if (thuCheckBox.isSelected()) newDays.add(DayOfWeek.THURSDAY);
        if (friCheckBox.isSelected()) newDays.add(DayOfWeek.FRIDAY);
        if (satCheckBox.isSelected()) newDays.add(DayOfWeek.SATURDAY);
        if (sunCheckBox.isSelected()) newDays.add(DayOfWeek.SUNDAY);

        selected.setWorkingDays(newDays);
        FileManager.saveDoctor(selected);
        scheduleMessageLabel.setText("Schedule updated.");
        scheduleMessageLabel.setStyle("-fx-text-fill: green;");
        refreshAll();
    }

    // --- Filtering logic ---
    private void applyFilters() {
        Predicate<Appointment> predicate = app -> true;
        Doctor filterDoc = filterDoctorComboBox.getValue();
        if (filterDoc != null) {
            predicate = predicate.and(app -> app.getDoctor().equals(filterDoc));
        }
        Patient filterPat = filterPatientComboBox.getValue();
        if (filterPat != null) {
            predicate = predicate.and(app -> app.getPatient().equals(filterPat));
        }
        LocalDate filterDate = filterDatePicker.getValue();
        if (filterDate != null) {
            predicate = predicate.and(app -> app.getDate().equals(filterDate));
        }
        filteredAppointments.setPredicate(predicate);
    }

    @FXML
    private void clearFilters() {
        filterDoctorComboBox.setValue(null);
        filterPatientComboBox.setValue(null);
        filterDatePicker.setValue(null);
    }

    // --- CRUD actions ---
    @FXML
    private void addDoctor() {
        String name = newDoctorField.getText().trim();
        if (!name.isEmpty()) {
            Doctor newDoc = new Doctor(name);
            doctors.add(newDoc);
            FileManager.saveDoctor(newDoc);
            newDoctorField.clear();
            refreshAll();
        }
    }

    @FXML
    private void addPatient() {
        String name = newPatientField.getText().trim();
        if (!name.isEmpty()) {
            Patient newPat = new Patient(name);
            patients.add(newPat);
            FileManager.savePatient(newPat);
            newPatientField.clear();
            refreshAll();
        }
    }

    @FXML
    private void addAppointment() {
        Doctor doctor = doctorComboBox.getValue();
        Patient patient = patientComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String timeStr = timeField.getText();

        if (doctor == null || patient == null || date == null || timeStr.isEmpty()) {
            showMessage("All fields required", Color.RED);
            return;
        }
        LocalTime time;
        try {
            time = LocalTime.parse(timeStr);
        } catch (Exception e) {
            showMessage("Invalid time format (HH:MM)", Color.RED);
            return;
        }

        // 1. Check if date is in the past
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            showMessage("Cannot schedule appointment on a past date.", Color.RED);
            return;
        }

        // 2. If date is today, check if time is in the past
        if (date.equals(today) && time.isBefore(LocalTime.now())) {
            showMessage("Cannot schedule appointment at a past time.", Color.RED);
            return;
        }

        // 3. Check if doctor works that day
        if (!doctor.getWorkingDays().contains(date.getDayOfWeek())) {
            showMessage("Doctor does not work on " + date.getDayOfWeek(), Color.RED);
            return;
        }

        // 4. Check if doctor is already booked at that time
        boolean occupied = doctor.getAppointments().stream()
                .anyMatch(a -> a.getDate().equals(date) && a.getTime().equals(time));
        if (occupied) {
            showMessage("Doctor is busy at that time", Color.RED);
            return;
        }

        Appointment app = new Appointment(doctor, patient, date, time);
        FileManager.saveAppointment(app);
        refreshAll();

        doctorComboBox.setValue(null);
        patientComboBox.setValue(null);
        datePicker.setValue(null);
        timeField.clear();
        showMessage("Appointment added", Color.GREEN);
    }

    // --- Context menus for delete ---
    private ContextMenu createDoctorContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            Doctor selected = doctorListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                FileManager.deleteDoctor(selected);
                refreshAll();
            }
        });
        menu.getItems().add(deleteItem);
        return menu;
    }

    private ContextMenu createPatientContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            Patient selected = patientListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                FileManager.deletePatient(selected);
                refreshAll();
            }
        });
        menu.getItems().add(deleteItem);
        return menu;
    }

    // --- Refresh all data and UI ---
    private void refreshAll() {
        // Reload all data from CSV
        doctors.setAll(FileManager.loadDoctors());
        patients.setAll(FileManager.loadPatients());
        appointments.setAll(FileManager.loadAppointments());

        // Update UI lists
        doctorListView.refresh();
        patientListView.refresh();
        doctorComboBox.setItems(doctors);
        patientComboBox.setItems(patients);
        filterDoctorComboBox.setItems(doctors);
        filterPatientComboBox.setItems(patients);
        scheduleDoctorComboBox.setItems(doctors);
        appointmentTable.refresh();
        applyFilters();

        // Update dashboard
        totalDoctorsLabel.setText(String.valueOf(doctors.size()));
        totalPatientsLabel.setText(String.valueOf(patients.size()));
        LocalDate today = LocalDate.now();
        long todayCount = appointments.stream().filter(a -> a.getDate().equals(today)).count();
        todayAppointmentsLabel.setText(String.valueOf(todayCount));
        LocalDate nextWeek = today.plusDays(7);
        long upcomingCount = appointments.stream()
                .filter(a -> a.getDate().isAfter(today) && a.getDate().isBefore(nextWeek))
                .count();
        upcomingAppointmentsLabel.setText(String.valueOf(upcomingCount));

        // Populate upcoming appointments table
        List<Appointment> upcomingList = appointments.stream()
                .filter(a -> a.getDate().isAfter(today) && a.getDate().isBefore(nextWeek))
                .sorted((a,b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
        upcomingTable.setItems(FXCollections.observableArrayList(upcomingList));
        upcomingDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        upcomingTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime().toString()));
        upcomingDoctorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctor().getName()));
        upcomingPatientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatient().getName()));

        // Populate today's working doctors
        List<Doctor> todayDoctors = doctors.stream()
                .filter(Doctor::isWorkingToday)
                .collect(Collectors.toList());
        todayDoctorsListView.setItems(FXCollections.observableArrayList(todayDoctors));
        todayDoctorsListView.setCellFactory(lv -> new ListCell<Doctor>() {
            @Override protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName());
            }
        });
    }

    private void showMessage(String msg, Color color) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Clinic System");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}