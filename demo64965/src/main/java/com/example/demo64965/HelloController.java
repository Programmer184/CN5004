package com.example.demo64965;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
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

    // --- Doctors tab with TableView ---
    @FXML private TextField newDoctorField;
    @FXML private ComboBox<String> newDoctorSpecialtyComboBox;
    @FXML private TableView<Doctor> doctorTableView;
    @FXML private TableColumn<Doctor, String> doctorNameCol;
    @FXML private TableColumn<Doctor, String> doctorSpecialtyCol;
    @FXML private TableColumn<Doctor, String> doctorWorkingDaysCol;
    @FXML private TableColumn<Doctor, Void> doctorActionsCol;

    // --- Patients tab with TableView ---
    @FXML private TextField newPatientNameField;
    @FXML private TextField newPatientAgeField;
    @FXML private ComboBox<String> newPatientConditionComboBox;
    @FXML private TableView<Patient> patientTableView;
    @FXML private TableColumn<Patient, String> patientNameCol;
    @FXML private TableColumn<Patient, Number> patientAgeCol;
    @FXML private TableColumn<Patient, String> patientConditionCol;
    @FXML private TableColumn<Patient, Void> patientActionsCol;

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
                    new Doctor("Dr. Smith", "Cardiologist"),
                    new Doctor("Dr. Brown", "Pathologist"),
                    new Doctor("Dr. Lee", "Neurologist"));
            loadedDoctors.forEach(FileManager::saveDoctor);
        }
        doctors.setAll(loadedDoctors);

        // Ensure every doctor has working days (migration for old data)
        FileManager.migrateOldDoctors();
        doctors.setAll(FileManager.loadDoctors()); // reload after migration

        List<Patient> loadedPatients = FileManager.loadPatients();
        if (loadedPatients.isEmpty()) {
            Collections.addAll(loadedPatients,
                    new Patient("Alice", 32, "Hypertension"),
                    new Patient("Bob", 45, "Diabetes"),
                    new Patient("Charlie", 28, "Asthma"));
            loadedPatients.forEach(FileManager::savePatient);
        }
        patients.setAll(loadedPatients);

        List<Appointment> loadedAppointments = FileManager.loadAppointments();
        appointments.setAll(loadedAppointments);

        // Setup specialties dropdown
        ObservableList<String> specialties = FXCollections.observableArrayList(
                "General Practitioner",
                "Cardiologist",
                "Pathologist",
                "Neurologist",
                "Pediatrician",
                "Dermatologist",
                "Orthopedist",
                "Ophthalmologist",
                "Psychiatrist",
                "Radiologist"
        );
        newDoctorSpecialtyComboBox.setItems(specialties);
        newDoctorSpecialtyComboBox.setValue("General Practitioner");

        // Setup medical conditions dropdown
        ObservableList<String> conditions = FXCollections.observableArrayList(
                "Not specified",
                "Hypertension",
                "Diabetes",
                "Asthma",
                "Arthritis",
                "Migraine",
                "Allergy",
                "Heart Disease",
                "Flu",
                "Fever",
                "Cough",
                "Headache",
                "Back Pain"
        );
        newPatientConditionComboBox.setItems(conditions);
        newPatientConditionComboBox.setValue("Not specified");

        // Setup Doctors TableView
        doctorTableView.setItems(doctors);

        doctorNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        doctorNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorNameCol.setOnEditCommit(event -> {
            Doctor doctor = event.getRowValue();
            doctor.setName(event.getNewValue());
            FileManager.saveDoctor(doctor);
            refreshAll();
        });

        doctorSpecialtyCol.setCellValueFactory(data -> data.getValue().specialtyProperty());
        doctorSpecialtyCol.setCellFactory(ComboBoxTableCell.forTableColumn(specialties));
        doctorSpecialtyCol.setOnEditCommit(event -> {
            Doctor doctor = event.getRowValue();
            doctor.setSpecialty(event.getNewValue());
            FileManager.saveDoctor(doctor);
            refreshAll();
        });

        doctorWorkingDaysCol.setCellValueFactory(data -> data.getValue().workingDaysDisplayProperty());

        doctorActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.getStyleClass().add("button-delete");
                deleteButton.setOnAction(event -> {
                    Doctor doctor = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Doctor");
                    confirm.setHeaderText("Delete this doctor?");
                    confirm.setContentText("Doctor: " + doctor.getName() +
                            "\nSpecialty: " + doctor.getSpecialty() +
                            "\nWorking Days: " + doctorWorkingDaysCol.getCellData(doctor) +
                            "\n\nAll appointments with this doctor will also be deleted.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            FileManager.deleteDoctor(doctor);
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

        doctorTableView.setEditable(true);

        // Setup Patients TableView
        patientTableView.setItems(patients);

        patientNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        patientNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        patientNameCol.setOnEditCommit(event -> {
            Patient patient = event.getRowValue();
            patient.setName(event.getNewValue());
            FileManager.savePatient(patient);
            refreshAll();
        });

        patientAgeCol.setCellValueFactory(data -> data.getValue().ageProperty());
        patientAgeCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return object != null ? String.valueOf(object.intValue()) : "";
            }
            @Override
            public Number fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }));
        patientAgeCol.setOnEditCommit(event -> {
            Patient patient = event.getRowValue();
            patient.setAge(event.getNewValue().intValue());
            FileManager.savePatient(patient);
            refreshAll();
        });

        patientConditionCol.setCellValueFactory(data -> data.getValue().medicalConditionProperty());
        patientConditionCol.setCellFactory(ComboBoxTableCell.forTableColumn(conditions));
        patientConditionCol.setOnEditCommit(event -> {
            Patient patient = event.getRowValue();
            patient.setMedicalCondition(event.getNewValue());
            FileManager.savePatient(patient);
            refreshAll();
        });

        patientActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.getStyleClass().add("button-delete");
                deleteButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Patient");
                    confirm.setHeaderText("Delete this patient?");
                    confirm.setContentText("Patient: " + patient.getName() +
                            "\nAge: " + patient.getAge() +
                            "\nMedical Condition: " + patient.getMedicalCondition() +
                            "\n\nAll appointments with this patient will also be deleted.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            FileManager.deletePatient(patient);
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

        patientTableView.setEditable(true);

        // Setup Appointments tab
        doctorComboBox.setItems(doctors);
        patientComboBox.setItems(patients);

        patientComboBox.setCellFactory(lv -> new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) setText(null);
                else setText(patient.getName() + " (" + patient.getAge() + " yrs, " + patient.getMedicalCondition() + ")");
            }
        });
        patientComboBox.setButtonCell(new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) setText(null);
                else setText(patient.getName() + " (" + patient.getAge() + " yrs)");
            }
        });

        filterPatientComboBox.setItems(patients);
        filterPatientComboBox.setCellFactory(lv -> new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) setText(null);
                else setText(patient.getName() + " (" + patient.getAge() + " yrs)");
            }
        });

        filterDoctorComboBox.setItems(doctors);
        filterDoctorComboBox.getSelectionModel().select(null);
        filterPatientComboBox.getSelectionModel().select(null);

        filterDoctorComboBox.setCellFactory(lv -> new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });

        doctorComboBox.setCellFactory(lv -> new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });
        doctorComboBox.setButtonCell(new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });

        scheduleDoctorComboBox.setCellFactory(lv -> new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });
        scheduleDoctorComboBox.setButtonCell(new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });

        // Disable past dates in DatePicker
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
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
        appDoctorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctor().getName() + " (" + data.getValue().getDoctor().getSpecialty() + ")"));
        appPatientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatient().getName() + " (" + data.getValue().getPatient().getAge() + " yrs)"));
        appDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        appTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime().toString()));

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

        if (newDays.isEmpty()) {
            scheduleMessageLabel.setText("At least one working day must be selected.");
            scheduleMessageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        selected.setWorkingDays(newDays);
        FileManager.saveDoctor(selected); // this will update the CSV
        refreshAll(); // reload everything
        scheduleMessageLabel.setText("Schedule updated successfully.");
        scheduleMessageLabel.setStyle("-fx-text-fill: green;");
        // Keep the same doctor selected
        scheduleDoctorComboBox.setValue(selected);
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
        String specialty = newDoctorSpecialtyComboBox.getValue();
        if (!name.isEmpty() && specialty != null) {
            Doctor newDoc = new Doctor(name, specialty);
            doctors.add(newDoc);
            FileManager.saveDoctor(newDoc);
            newDoctorField.clear();
            newDoctorSpecialtyComboBox.setValue("General Practitioner");
            refreshAll();
        } else if (name.isEmpty()) {
            showMessage("Please enter doctor name", Color.RED);
        }
    }

    @FXML
    private void addPatient() {
        String name = newPatientNameField.getText().trim();
        String ageText = newPatientAgeField.getText().trim();
        String condition = newPatientConditionComboBox.getValue();

        if (name.isEmpty()) {
            showMessage("Please enter patient name", Color.RED);
            return;
        }

        int age = 0;
        if (!ageText.isEmpty()) {
            try {
                age = Integer.parseInt(ageText);
                if (age < 0 || age > 150) {
                    showMessage("Please enter a valid age (0-150)", Color.RED);
                    return;
                }
            } catch (NumberFormatException e) {
                showMessage("Please enter a valid age", Color.RED);
                return;
            }
        }

        Patient newPat = new Patient(name, age, condition);
        patients.add(newPat);
        FileManager.savePatient(newPat);
        newPatientNameField.clear();
        newPatientAgeField.clear();
        newPatientConditionComboBox.setValue("Not specified");
        refreshAll();
        showMessage("Patient added successfully", Color.GREEN);
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

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            showMessage("Cannot schedule appointment on a past date.", Color.RED);
            return;
        }
        if (date.equals(today) && time.isBefore(LocalTime.now())) {
            showMessage("Cannot schedule appointment at a past time.", Color.RED);
            return;
        }

        if (!doctor.getWorkingDays().contains(date.getDayOfWeek())) {
            showMessage("Doctor does not work on " + date.getDayOfWeek(), Color.RED);
            return;
        }

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

    // --- Auto-cleanup of past appointments ---
    private void removePastAppointments() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<Appointment> allAppointments = FileManager.loadAppointments();
        List<Appointment> futureAppointments = allAppointments.stream()
                .filter(app -> {
                    if (app.getDate().isBefore(today)) return false;
                    if (app.getDate().isEqual(today) && app.getTime().isBefore(now)) return false;
                    return true;
                })
                .collect(Collectors.toList());
        if (futureAppointments.size() != allAppointments.size()) {
            FileManager.saveAllAppointments(futureAppointments);
        }
    }

    // --- Refresh all data and UI ---
    private void refreshAll() {
        removePastAppointments();
        doctors.setAll(FileManager.loadDoctors());
        patients.setAll(FileManager.loadPatients());
        appointments.setAll(FileManager.loadAppointments());

        doctorTableView.refresh();
        patientTableView.refresh();
        doctorComboBox.setItems(doctors);
        patientComboBox.setItems(patients);
        filterDoctorComboBox.setItems(doctors);
        filterPatientComboBox.setItems(patients);
        scheduleDoctorComboBox.setItems(doctors);
        appointmentTable.refresh();
        applyFilters();

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

        List<Appointment> upcomingList = appointments.stream()
                .filter(a -> (a.getDate().isEqual(today) || a.getDate().isAfter(today)) && a.getDate().isBefore(nextWeek))
                .sorted((a,b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
        upcomingTable.setItems(FXCollections.observableArrayList(upcomingList));
        upcomingDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        upcomingTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime().toString()));
        upcomingDoctorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctor().getName() + " (" + data.getValue().getDoctor().getSpecialty() + ")"));
        upcomingPatientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatient().getName() + " (" + data.getValue().getPatient().getAge() + " yrs)"));

        // Update the Doctors Working Today list
        List<Doctor> todayDoctors = doctors.stream()
                .filter(Doctor::isWorkingToday)
                .collect(Collectors.toList());
        todayDoctorsListView.setItems(FXCollections.observableArrayList(todayDoctors));
        todayDoctorsListView.setCellFactory(lv -> new ListCell<Doctor>() {
            @Override protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
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