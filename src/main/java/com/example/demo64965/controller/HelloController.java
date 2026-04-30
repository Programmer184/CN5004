package com.example.demo64965.controller;

import com.example.demo64965.model.Appointment;
import com.example.demo64965.model.Doctor;
import com.example.demo64965.model.Patient;
import com.example.demo64965.util.FileManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
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

    // --- Age distribution components ---
    @FXML private ProgressBar ageBar0_18;
    @FXML private ProgressBar ageBar19_35;
    @FXML private ProgressBar ageBar36_50;
    @FXML private ProgressBar ageBar51_65;
    @FXML private ProgressBar ageBar66plus;
    @FXML private Label ageCount0_18;
    @FXML private Label ageCount19_35;
    @FXML private Label ageCount36_50;
    @FXML private Label ageCount51_65;
    @FXML private Label ageCount66plus;

    // --- Doctors tab with TableView ---
    @FXML private TableView<Doctor> doctorTableView;
    @FXML private TableColumn<Doctor, String> doctorNameCol;
    @FXML private TableColumn<Doctor, String> doctorSpecialtyCol;
    @FXML private TableColumn<Doctor, String> doctorPhoneCol;
    @FXML private TableColumn<Doctor, String> doctorEmailCol;
    @FXML private TableColumn<Doctor, Number> doctorExperienceCol;
    @FXML private TableColumn<Doctor, String> doctorBioCol;
    @FXML private TableColumn<Doctor, String> doctorWorkingDaysCol;
    @FXML private TableColumn<Doctor, Void> doctorActionsCol;

    // --- Patients tab with TableView ---
    @FXML private TableView<Patient> patientTableView;
    @FXML private TableColumn<Patient, Number> patientIdCol;
    @FXML private TableColumn<Patient, String> patientFirstNameCol;
    @FXML private TableColumn<Patient, String> patientLastNameCol;
    @FXML private TableColumn<Patient, Number> patientAgeCol;
    @FXML private TableColumn<Patient, String> patientConditionCol;
    @FXML private TableColumn<Patient, String> patientPhoneCol;
    @FXML private TableColumn<Patient, String> patientEmailCol;
    @FXML private TableColumn<Patient, LocalDate> patientLastVisitDateCol;
    @FXML private TableColumn<Patient, String> patientLastVisitDoctorCol;
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

    private ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private ObservableList<Patient> patients = FXCollections.observableArrayList();
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    private ObservableList<String> conditions = FXCollections.observableArrayList(
            "Not specified", "Hypertension", "Diabetes", "Asthma", "Arthritis",
            "Migraine", "Allergy", "Heart Disease", "Flu", "Fever", "Cough", "Headache", "Back Pain"
    );

    @FXML
    public void initialize() {
        // Φόρτωση δεδομένων
        List<Doctor> loadedDoctors = FileManager.loadDoctors();
        if (loadedDoctors.isEmpty()) {
            Collections.addAll(loadedDoctors,
                    new Doctor("Dr. Smith", "Cardiologist", "2101234567", "smith@clinic.com", 12, "Experienced cardiologist"),
                    new Doctor("Dr. Brown", "Pathologist", "2109876543", "brown@clinic.com", 8, "Specializes in clinical pathology"),
                    new Doctor("Dr. Lee", "Neurologist", "2105551234", "lee@clinic.com", 15, "Neurology expert"));
            loadedDoctors.forEach(FileManager::saveDoctor);
        }
        doctors.setAll(loadedDoctors);

        List<Patient> loadedPatients = FileManager.loadPatients();
        patients.setAll(loadedPatients);

        // Κλήση της νέας loadAppointments με τις λίστες
        List<Appointment> loadedAppointments = FileManager.loadAppointments(doctors, patients);
        appointments.setAll(loadedAppointments);

        // Λίστα ειδικοτήτων (χρησιμοποιείται σε ComboBoxes)
        ObservableList<String> specialties = FXCollections.observableArrayList(
                "General Practitioner", "Cardiologist", "Pathologist", "Neurologist",
                "Pediatrician", "Dermatologist", "Orthopedist", "Ophthalmologist", "Psychiatrist", "Radiologist");

        // ========== Ρύθμιση Πίνακα Γιατρών ==========
        doctorTableView.setItems(doctors);
        doctorTableView.setEditable(true);

        doctorNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        doctorNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorNameCol.setOnEditCommit(event -> {
            Doctor doctor = event.getRowValue();
            String oldName = doctor.getName();
            String newName = event.getNewValue();
            if (!oldName.equals(newName)) {
                boolean exists = doctors.stream().anyMatch(d -> d.getName().equals(newName));
                if (exists) { showMessage("A doctor with that name already exists.", Color.RED); refreshAll(); return; }
                FileManager.renameDoctor(doctor, newName);
            }
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

        doctorPhoneCol.setCellValueFactory(data -> data.getValue().phoneProperty());
        doctorPhoneCol.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorPhoneCol.setOnEditCommit(event -> {
            Doctor doctor = event.getRowValue();
            doctor.setPhone(event.getNewValue());
            FileManager.saveDoctor(doctor);
        });

        doctorEmailCol.setCellValueFactory(data -> data.getValue().emailProperty());
        doctorEmailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorEmailCol.setOnEditCommit(event -> {
            Doctor doctor = event.getRowValue();
            doctor.setEmail(event.getNewValue());
            FileManager.saveDoctor(doctor);
        });

        doctorExperienceCol.setCellValueFactory(data -> data.getValue().yearsOfExperienceProperty());
        doctorExperienceCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override public String toString(Number object) { return object != null ? String.valueOf(object.intValue()) : ""; }
            @Override public Number fromString(String string) {
                try { return Integer.parseInt(string); } catch (NumberFormatException e) { return 0; }
            }
        }));
        doctorExperienceCol.setOnEditCommit(event -> {
            Doctor doctor = event.getRowValue();
            doctor.setYearsOfExperience(event.getNewValue().intValue());
            FileManager.saveDoctor(doctor);
        });

        doctorBioCol.setCellValueFactory(data -> data.getValue().bioProperty());
        doctorBioCol.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorBioCol.setOnEditCommit(event -> {
            Doctor doctor = event.getRowValue();
            doctor.setBio(event.getNewValue());
            FileManager.saveDoctor(doctor);
        });

        // Στήλη Working Days – με cell factory που ανανεώνεται σωστά
        doctorWorkingDaysCol.setCellValueFactory(data -> data.getValue().workingDaysDisplayProperty());
        doctorWorkingDaysCol.setCellFactory(column -> new TableCell<Doctor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-cursor: hand;");
                }
            }
        });

        doctorTableView.setRowFactory(tv -> {
            TableRow<Doctor> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Doctor doctor = row.getItem();
                    TableCell<?, ?> cell = getCellAtRowColumn(row, doctorWorkingDaysCol);
                    if (cell != null && cell.getBoundsInParent().contains(event.getX(), event.getY())) {
                        editWorkingDays(doctor);
                    }
                }
            });
            return row;
        });

        doctorActionsCol.setCellFactory(param -> new TableCell<>() {
            private final HBox buttons = new HBox(5);
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            {
                editButton.getStyleClass().add("button-edit");
                deleteButton.getStyleClass().add("button-delete");
                editButton.setOnAction(event -> {
                    Doctor doctor = getTableView().getItems().get(getIndex());
                    editDoctor(doctor);
                });
                deleteButton.setOnAction(event -> {
                    Doctor doctor = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Doctor");
                    confirm.setHeaderText("Delete this doctor?");
                    confirm.setContentText("Doctor: " + doctor.getName() + "\nSpecialty: " + doctor.getSpecialty() +
                            "\nWorking Days: " + doctorWorkingDaysCol.getCellData(doctor) +
                            "\n\nAll appointments with this doctor will also be deleted.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) { FileManager.deleteDoctor(doctor); refreshAll(); }
                    });
                });
                buttons.getChildren().addAll(editButton, deleteButton);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(buttons);
            }
        });

        // ========== Ρύθμιση Πίνακα Ασθενών ==========
        patientTableView.setItems(patients);
        patientTableView.setEditable(true);

        patientIdCol.setCellValueFactory(data -> data.getValue().idProperty());

        patientFirstNameCol.setCellValueFactory(data -> data.getValue().firstNameProperty());
        patientFirstNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        patientFirstNameCol.setOnEditCommit(event -> {
            Patient patient = event.getRowValue();
            patient.setFirstName(event.getNewValue());
            FileManager.savePatient(patient);
            refreshAll();
        });

        patientLastNameCol.setCellValueFactory(data -> data.getValue().lastNameProperty());
        patientLastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        patientLastNameCol.setOnEditCommit(event -> {
            Patient patient = event.getRowValue();
            patient.setLastName(event.getNewValue());
            FileManager.savePatient(patient);
            refreshAll();
        });

        patientAgeCol.setCellValueFactory(data -> data.getValue().ageProperty());
        patientAgeCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override public String toString(Number object) { return object != null ? String.valueOf(object.intValue()) : ""; }
            @Override public Number fromString(String string) {
                try { return Integer.parseInt(string); } catch (NumberFormatException e) { return 0; }
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

        patientPhoneCol.setCellValueFactory(data -> data.getValue().phoneProperty());
        patientPhoneCol.setCellFactory(TextFieldTableCell.forTableColumn());
        patientPhoneCol.setOnEditCommit(event -> {
            Patient patient = event.getRowValue();
            patient.setPhone(event.getNewValue());
            FileManager.savePatient(patient);
            refreshAll();
        });

        patientEmailCol.setCellValueFactory(data -> data.getValue().emailProperty());
        patientEmailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        patientEmailCol.setOnEditCommit(event -> {
            Patient patient = event.getRowValue();
            patient.setEmail(event.getNewValue());
            FileManager.savePatient(patient);
            refreshAll();
        });

        patientLastVisitDateCol.setCellValueFactory(data -> data.getValue().lastVisitDateProperty());
        patientLastVisitDateCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate object) { return object != null ? object.toString() : ""; }
            @Override public LocalDate fromString(String string) { return string.isEmpty() ? null : LocalDate.parse(string); }
        }));
        patientLastVisitDateCol.setOnEditCommit(event -> {
            Patient patient = event.getRowValue();
            patient.setLastVisitDate(event.getNewValue());
            FileManager.savePatient(patient);
            refreshAll();
        });

        patientLastVisitDoctorCol.setCellValueFactory(data -> {
            Doctor d = data.getValue().getLastVisitDoctor();
            return new SimpleStringProperty(d != null ? d.getName() : "");
        });
        patientLastVisitDoctorCol.setCellFactory(TextFieldTableCell.forTableColumn());
        patientLastVisitDoctorCol.setOnEditCommit(event -> {
            Patient patient = event.getRowValue();
            String docName = event.getNewValue();
            Doctor newDoc = doctors.stream().filter(d -> d.getName().equals(docName)).findFirst().orElse(null);
            patient.setLastVisitDoctor(newDoc);
            FileManager.savePatient(patient);
            refreshAll();
        });

        patientActionsCol.setCellFactory(param -> new TableCell<>() {
            private final HBox buttons = new HBox(5);
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            {
                editButton.getStyleClass().add("button-edit");
                deleteButton.getStyleClass().add("button-delete");
                editButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    editPatient(patient);
                });
                deleteButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Patient");
                    confirm.setHeaderText("Delete this patient?");
                    confirm.setContentText("Patient: " + patient.getFullName() + "\nAge: " + patient.getAge() +
                            "\nMedical Condition: " + patient.getMedicalCondition() +
                            "\n\nAll appointments with this patient will also be deleted.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) { FileManager.deletePatient(patient); refreshAll(); }
                    });
                });
                buttons.getChildren().addAll(editButton, deleteButton);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(buttons);
            }
        });

        // ========== Ρύθμιση καρτέλας Appointments ==========
        doctorComboBox.setItems(doctors);
        patientComboBox.setItems(patients);
        patientComboBox.setCellFactory(lv -> new ListCell<Patient>() {
            @Override protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) setText(null);
                else setText(patient.getFullName() + " (" + patient.getAge() + " yrs, " + patient.getMedicalCondition() + ")");
            }
        });
        patientComboBox.setButtonCell(new ListCell<Patient>() {
            @Override protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) setText(null);
                else setText(patient.getFullName() + " (" + patient.getAge() + " yrs)");
            }
        });

        doctorComboBox.setCellFactory(lv -> new ListCell<Doctor>() {
            @Override protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });
        doctorComboBox.setButtonCell(new ListCell<Doctor>() {
            @Override protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        appPatientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatient().getFullName() + " (" + data.getValue().getPatient().getAge() + " yrs)"));
        appDoctorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctor().getName() + " (" + data.getValue().getDoctor().getSpecialty() + ")"));
        appDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        appTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime().toString()));

        appActionsCol.setCellFactory(param -> new TableCell<>() {
            private final HBox buttons = new HBox(5);
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            {
                editButton.getStyleClass().add("button-edit");
                deleteButton.getStyleClass().add("button-delete");
                editButton.setOnAction(event -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    editAppointment(app);
                });
                deleteButton.setOnAction(event -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Appointment");
                    confirm.setHeaderText("Delete this appointment?");
                    confirm.setContentText("Doctor: " + app.getDoctor().getName() +
                            "\nPatient: " + app.getPatient().getFullName() +
                            "\nDate: " + app.getDate() + "\nTime: " + app.getTime());
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) { FileManager.deleteAppointment(app); refreshAll(); }
                    });
                });
                buttons.getChildren().addAll(editButton, deleteButton);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(buttons);
            }
        });
        appointmentTable.setItems(appointments);

        refreshAll();
    }

    private TableCell<?, ?> getCellAtRowColumn(TableRow<Doctor> row, TableColumn<Doctor, ?> column) {
        int colIndex = doctorTableView.getColumns().indexOf(column);
        if (colIndex < 0) return null;
        for (Node n : row.getChildrenUnmodifiable()) {
            if (n instanceof TableCell) {
                TableCell<?, ?> cell = (TableCell<?, ?>) n;
                if (cell.getTableColumn() == column) return cell;
            }
        }
        return null;
    }

    @FXML private void showDashboard() { tabPane.getSelectionModel().select(0); }
    @FXML private void showDoctors() { tabPane.getSelectionModel().select(1); }
    @FXML private void showPatients() { tabPane.getSelectionModel().select(2); }
    @FXML private void showAppointments() { tabPane.getSelectionModel().select(3); }

    @FXML
    private void addPatient() {
        showPatientDialog(null);
    }

    private void showPatientDialog(Patient existingPatient) {
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle(existingPatient == null ? "Add Patient" : "Edit Patient");
        dialog.setHeaderText(existingPatient == null ? "Enter patient details" : "Edit patient details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField ageField = new TextField();
        ComboBox<String> conditionCombo = new ComboBox<>(conditions);
        TextField phoneField = new TextField();
        TextField emailField = new TextField();

        if (existingPatient != null) {
            firstNameField.setText(existingPatient.getFirstName());
            lastNameField.setText(existingPatient.getLastName());
            ageField.setText(String.valueOf(existingPatient.getAge()));
            conditionCombo.setValue(existingPatient.getMedicalCondition());
            phoneField.setText(existingPatient.getPhone());
            emailField.setText(existingPatient.getEmail());
        } else {
            conditionCombo.setValue("Not specified");
        }

        grid.add(new Label("First Name:"), 0, 0); grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1); grid.add(lastNameField, 1, 1);
        grid.add(new Label("Age:"), 0, 2); grid.add(ageField, 1, 2);
        grid.add(new Label("Medical Condition:"), 0, 3); grid.add(conditionCombo, 1, 3);
        grid.add(new Label("Phone:"), 0, 4); grid.add(phoneField, 1, 4);
        grid.add(new Label("Email:"), 0, 5); grid.add(emailField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    showMessage("First name and last name are required.", Color.RED); return null;
                }
                int age;
                try {
                    age = Integer.parseInt(ageField.getText().trim());
                    if (age < 0 || age > 150) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    showMessage("Please enter a valid age (0-150).", Color.RED); return null;
                }
                String condition = conditionCombo.getValue();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();

                String phoneDigits = phone.replaceAll("[^0-9]", "");
                if (phoneDigits.length() < 10) {
                    showMessage("Phone number must contain at least 10 digits.", Color.RED); return null;
                }
                if (!email.contains("@")) {
                    showMessage("Email must contain '@' symbol.", Color.RED); return null;
                }

                if (existingPatient != null) {
                    existingPatient.setFirstName(firstName);
                    existingPatient.setLastName(lastName);
                    existingPatient.setAge(age);
                    existingPatient.setMedicalCondition(condition);
                    existingPatient.setPhone(phone);
                    existingPatient.setEmail(email);
                    return existingPatient;
                } else {
                    int newId = patients.stream().mapToInt(Patient::getId).max().orElse(0) + 1;
                    return new Patient(newId, firstName, lastName, age, condition, phone, email, null, null);
                }
            }
            return null;
        });

        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> { FileManager.savePatient(patient); refreshAll(); });
    }

    @FXML
    private void addDoctor() {
        Dialog<Doctor> dialog = new Dialog<>();
        dialog.setTitle("Add Doctor");
        dialog.setHeaderText("Enter doctor details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        ComboBox<String> specialtyCombo = new ComboBox<>();
        specialtyCombo.setItems(FXCollections.observableArrayList(
                "General Practitioner", "Cardiologist", "Pathologist", "Neurologist",
                "Pediatrician", "Dermatologist", "Orthopedist", "Ophthalmologist", "Psychiatrist", "Radiologist"));
        specialtyCombo.setValue("General Practitioner");

        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField experienceField = new TextField();
        TextArea bioArea = new TextArea();
        bioArea.setPrefRowCount(3);
        bioArea.setWrapText(true);

        CheckBox monCheck = new CheckBox("Monday");
        CheckBox tueCheck = new CheckBox("Tuesday");
        CheckBox wedCheck = new CheckBox("Wednesday");
        CheckBox thuCheck = new CheckBox("Thursday");
        CheckBox friCheck = new CheckBox("Friday");
        CheckBox satCheck = new CheckBox("Saturday");
        CheckBox sunCheck = new CheckBox("Sunday");

        monCheck.setSelected(true);
        tueCheck.setSelected(true);
        wedCheck.setSelected(true);
        thuCheck.setSelected(true);
        friCheck.setSelected(true);

        HBox daysBox = new HBox(10, monCheck, tueCheck, wedCheck, thuCheck, friCheck, satCheck, sunCheck);

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Specialty:"), 0, 1); grid.add(specialtyCombo, 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("Email:"), 0, 3); grid.add(emailField, 1, 3);
        grid.add(new Label("Years of Experience:"), 0, 4); grid.add(experienceField, 1, 4);
        grid.add(new Label("Bio / Notes:"), 0, 5); grid.add(bioArea, 1, 5);
        grid.add(new Label("Working Days:"), 0, 6); grid.add(daysBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String specialty = specialtyCombo.getValue();
                if (name.isEmpty() || specialty == null) {
                    showMessage("Name and specialty are required.", Color.RED); return null;
                }
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                int experience = 0;
                try {
                    experience = Integer.parseInt(experienceField.getText().trim());
                } catch (NumberFormatException e) {}
                String bio = bioArea.getText().trim();

                Doctor newDoctor = new Doctor(name, specialty, phone, email, experience, bio);

                Set<DayOfWeek> days = new HashSet<>();
                if (monCheck.isSelected()) days.add(DayOfWeek.MONDAY);
                if (tueCheck.isSelected()) days.add(DayOfWeek.TUESDAY);
                if (wedCheck.isSelected()) days.add(DayOfWeek.WEDNESDAY);
                if (thuCheck.isSelected()) days.add(DayOfWeek.THURSDAY);
                if (friCheck.isSelected()) days.add(DayOfWeek.FRIDAY);
                if (satCheck.isSelected()) days.add(DayOfWeek.SATURDAY);
                if (sunCheck.isSelected()) days.add(DayOfWeek.SUNDAY);
                if (days.isEmpty()) {
                    days.addAll(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
                }
                newDoctor.setWorkingDays(days);

                return newDoctor;
            }
            return null;
        });

        Optional<Doctor> result = dialog.showAndWait();
        result.ifPresent(doctor -> {
            doctors.add(doctor);
            FileManager.saveDoctor(doctor);
            refreshAll();
            showMessage("Doctor added successfully.", Color.GREEN);
        });
    }

    private void editDoctor(Doctor doctor) {
        Dialog<Doctor> dialog = new Dialog<>();
        dialog.setTitle("Edit Doctor");
        dialog.setHeaderText("Edit doctor details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(doctor.getName());
        ComboBox<String> specialtyCombo = new ComboBox<>();
        specialtyCombo.setItems(FXCollections.observableArrayList(
                "General Practitioner", "Cardiologist", "Pathologist", "Neurologist",
                "Pediatrician", "Dermatologist", "Orthopedist", "Ophthalmologist", "Psychiatrist", "Radiologist"));
        specialtyCombo.setValue(doctor.getSpecialty());

        TextField phoneField = new TextField(doctor.getPhone());
        TextField emailField = new TextField(doctor.getEmail());
        TextField experienceField = new TextField(String.valueOf(doctor.getYearsOfExperience()));
        TextArea bioArea = new TextArea(doctor.getBio());
        bioArea.setPrefRowCount(3);
        bioArea.setWrapText(true);

        CheckBox monCheck = new CheckBox("Monday");
        CheckBox tueCheck = new CheckBox("Tuesday");
        CheckBox wedCheck = new CheckBox("Wednesday");
        CheckBox thuCheck = new CheckBox("Thursday");
        CheckBox friCheck = new CheckBox("Friday");
        CheckBox satCheck = new CheckBox("Saturday");
        CheckBox sunCheck = new CheckBox("Sunday");

        Set<DayOfWeek> currentDays = doctor.getWorkingDays();
        monCheck.setSelected(currentDays.contains(DayOfWeek.MONDAY));
        tueCheck.setSelected(currentDays.contains(DayOfWeek.TUESDAY));
        wedCheck.setSelected(currentDays.contains(DayOfWeek.WEDNESDAY));
        thuCheck.setSelected(currentDays.contains(DayOfWeek.THURSDAY));
        friCheck.setSelected(currentDays.contains(DayOfWeek.FRIDAY));
        satCheck.setSelected(currentDays.contains(DayOfWeek.SATURDAY));
        sunCheck.setSelected(currentDays.contains(DayOfWeek.SUNDAY));

        HBox daysBox = new HBox(10, monCheck, tueCheck, wedCheck, thuCheck, friCheck, satCheck, sunCheck);

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Specialty:"), 0, 1); grid.add(specialtyCombo, 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("Email:"), 0, 3); grid.add(emailField, 1, 3);
        grid.add(new Label("Years of Experience:"), 0, 4); grid.add(experienceField, 1, 4);
        grid.add(new Label("Bio / Notes:"), 0, 5); grid.add(bioArea, 1, 5);
        grid.add(new Label("Working Days:"), 0, 6); grid.add(daysBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newName = nameField.getText().trim();
                String newSpecialty = specialtyCombo.getValue();
                if (newName.isEmpty() || newSpecialty == null) {
                    showMessage("Name and specialty cannot be empty.", Color.RED); return null;
                }

                if (!newName.equals(doctor.getName())) {
                    boolean exists = doctors.stream().anyMatch(d -> d.getName().equals(newName) && d != doctor);
                    if (exists) {
                        showMessage("A doctor with that name already exists.", Color.RED); return null;
                    }
                    FileManager.renameDoctor(doctor, newName);
                }

                doctor.setSpecialty(newSpecialty);
                doctor.setPhone(phoneField.getText().trim());
                doctor.setEmail(emailField.getText().trim());
                try {
                    doctor.setYearsOfExperience(Integer.parseInt(experienceField.getText().trim()));
                } catch (NumberFormatException e) {
                    doctor.setYearsOfExperience(0);
                }
                doctor.setBio(bioArea.getText().trim());

                Set<DayOfWeek> newDays = new HashSet<>();
                if (monCheck.isSelected()) newDays.add(DayOfWeek.MONDAY);
                if (tueCheck.isSelected()) newDays.add(DayOfWeek.TUESDAY);
                if (wedCheck.isSelected()) newDays.add(DayOfWeek.WEDNESDAY);
                if (thuCheck.isSelected()) newDays.add(DayOfWeek.THURSDAY);
                if (friCheck.isSelected()) newDays.add(DayOfWeek.FRIDAY);
                if (satCheck.isSelected()) newDays.add(DayOfWeek.SATURDAY);
                if (sunCheck.isSelected()) newDays.add(DayOfWeek.SUNDAY);

                if (newDays.isEmpty()) {
                    showMessage("At least one working day must be selected.", Color.RED); return null;
                }
                doctor.setWorkingDays(newDays);

                FileManager.saveDoctor(doctor);
                return doctor;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(saved -> {
            refreshAll();
            showMessage("Doctor updated successfully.", Color.GREEN);
        });
    }

    private void editPatient(Patient patient) {
        showPatientDialog(patient);
    }

    private void editWorkingDays(Doctor doctor) {
        Dialog<Set<DayOfWeek>> dialog = new Dialog<>();
        dialog.setTitle("Edit Working Days");
        dialog.setHeaderText("Set working days for " + doctor.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        CheckBox monCheck = new CheckBox("Monday");
        CheckBox tueCheck = new CheckBox("Tuesday");
        CheckBox wedCheck = new CheckBox("Wednesday");
        CheckBox thuCheck = new CheckBox("Thursday");
        CheckBox friCheck = new CheckBox("Friday");
        CheckBox satCheck = new CheckBox("Saturday");
        CheckBox sunCheck = new CheckBox("Sunday");

        Set<DayOfWeek> currentDays = doctor.getWorkingDays();
        monCheck.setSelected(currentDays.contains(DayOfWeek.MONDAY));
        tueCheck.setSelected(currentDays.contains(DayOfWeek.TUESDAY));
        wedCheck.setSelected(currentDays.contains(DayOfWeek.WEDNESDAY));
        thuCheck.setSelected(currentDays.contains(DayOfWeek.THURSDAY));
        friCheck.setSelected(currentDays.contains(DayOfWeek.FRIDAY));
        satCheck.setSelected(currentDays.contains(DayOfWeek.SATURDAY));
        sunCheck.setSelected(currentDays.contains(DayOfWeek.SUNDAY));

        grid.add(monCheck, 0, 0); grid.add(tueCheck, 1, 0); grid.add(wedCheck, 2, 0);
        grid.add(thuCheck, 3, 0); grid.add(friCheck, 0, 1); grid.add(satCheck, 1, 1); grid.add(sunCheck, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Set<DayOfWeek> newDays = new HashSet<>();
                if (monCheck.isSelected()) newDays.add(DayOfWeek.MONDAY);
                if (tueCheck.isSelected()) newDays.add(DayOfWeek.TUESDAY);
                if (wedCheck.isSelected()) newDays.add(DayOfWeek.WEDNESDAY);
                if (thuCheck.isSelected()) newDays.add(DayOfWeek.THURSDAY);
                if (friCheck.isSelected()) newDays.add(DayOfWeek.FRIDAY);
                if (satCheck.isSelected()) newDays.add(DayOfWeek.SATURDAY);
                if (sunCheck.isSelected()) newDays.add(DayOfWeek.SUNDAY);
                return newDays;
            }
            return null;
        });

        Optional<Set<DayOfWeek>> result = dialog.showAndWait();
        result.ifPresent(newDays -> {
            if (!newDays.isEmpty()) {
                doctor.setWorkingDays(newDays);
                FileManager.saveDoctor(doctor);
                refreshAll();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("No days selected");
                alert.setContentText("A doctor must work at least one day. No changes were saved.");
                alert.showAndWait();
            }
        });
    }

    private void editAppointment(Appointment oldApp) {
        Dialog<Appointment> dialog = new Dialog<>();
        dialog.setTitle("Edit Appointment");
        dialog.setHeaderText("Edit appointment details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Doctor> doctorCombo = new ComboBox<>(doctors);
        doctorCombo.setCellFactory(lv -> new ListCell<Doctor>() {
            @Override protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });
        doctorCombo.setButtonCell(new ListCell<Doctor>() {
            @Override protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });
        doctorCombo.setValue(oldApp.getDoctor());

        ComboBox<Patient> patientCombo = new ComboBox<>(patients);
        patientCombo.setCellFactory(lv -> new ListCell<Patient>() {
            @Override protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) setText(null);
                else setText(patient.getFullName() + " (" + patient.getAge() + " yrs, " + patient.getMedicalCondition() + ")");
            }
        });
        patientCombo.setButtonCell(new ListCell<Patient>() {
            @Override protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) setText(null);
                else setText(patient.getFullName() + " (" + patient.getAge() + " yrs)");
            }
        });
        patientCombo.setValue(oldApp.getPatient());

        DatePicker datePicker = new DatePicker(oldApp.getDate());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        TextField timeField = new TextField(oldApp.getTime().toString());

        grid.add(new Label("Patient:"), 0, 0); grid.add(patientCombo, 1, 0);
        grid.add(new Label("Doctor:"), 0, 1); grid.add(doctorCombo, 1, 1);
        grid.add(new Label("Date:"), 0, 2); grid.add(datePicker, 1, 2);
        grid.add(new Label("Time (HH:MM):"), 0, 3); grid.add(timeField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Doctor selectedDoctor = doctorCombo.getValue();
                Patient selectedPatient = patientCombo.getValue();
                LocalDate selectedDate = datePicker.getValue();
                String timeStr = timeField.getText().trim();
                if (selectedDoctor == null || selectedPatient == null || selectedDate == null || timeStr.isEmpty()) {
                    showMessage("All fields required", Color.RED); return null;
                }
                LocalTime selectedTime;
                try { selectedTime = LocalTime.parse(timeStr); } catch (Exception e) {
                    showMessage("Invalid time format (HH:MM)", Color.RED); return null;
                }
                return new Appointment(selectedDoctor, selectedPatient, selectedDate, selectedTime);
            }
            return null;
        });

        Optional<Appointment> result = dialog.showAndWait();
        result.ifPresent(newApp -> {
            LocalDate today = LocalDate.now();
            if (newApp.getDate().isBefore(today)) {
                showMessage("Cannot schedule appointment on a past date.", Color.RED); return;
            }
            if (newApp.getDate().equals(today) && newApp.getTime().isBefore(LocalTime.now())) {
                showMessage("Cannot schedule appointment at a past time.", Color.RED); return;
            }
            if (!newApp.getDoctor().getWorkingDays().contains(newApp.getDate().getDayOfWeek())) {
                showMessage("Doctor does not work on " + newApp.getDate().getDayOfWeek(), Color.RED); return;
            }

            // Έλεγχος αν ο γιατρός είναι απασχολημένος
            boolean conflict = newApp.getDoctor().getAppointments().stream()
                    .filter(a -> a != oldApp)
                    .anyMatch(a -> a.getDate().equals(newApp.getDate()) && a.getTime().equals(newApp.getTime()));
            if (conflict) {
                showMessage("Doctor is busy at that time", Color.RED); return;
            }

            // Έλεγχος αν ο ασθενής έχει ήδη ραντεβού
            boolean patientConflict = newApp.getPatient().getAppointments().stream()
                    .filter(a -> a != oldApp)
                    .anyMatch(a -> a.getDate().equals(newApp.getDate()) && a.getTime().equals(newApp.getTime()));
            if (patientConflict) {
                showMessage("Patient already has an appointment at that time.", Color.RED);
                return;
            }

            FileManager.deleteAppointment(oldApp);
            FileManager.saveAppointment(newApp);
            refreshAll();
        });
    }

    @FXML
    private void addAppointment() {
        Doctor doctor = doctorComboBox.getValue();
        Patient patient = patientComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String timeStr = timeField.getText();

        if (doctor == null || patient == null || date == null || timeStr.isEmpty()) {
            showMessage("All fields required", Color.RED); return;
        }
        LocalTime time;
        try { time = LocalTime.parse(timeStr); } catch (Exception e) {
            showMessage("Invalid time format (HH:MM)", Color.RED); return;
        }

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            showMessage("Cannot schedule appointment on a past date.", Color.RED); return;
        }
        if (date.equals(today) && time.isBefore(LocalTime.now())) {
            showMessage("Cannot schedule appointment at a past time.", Color.RED); return;
        }
        if (!doctor.getWorkingDays().contains(date.getDayOfWeek())) {
            showMessage("Doctor does not work on " + date.getDayOfWeek(), Color.RED); return;
        }

        // Έλεγχος αν ο γιατρός είναι απασχολημένος
        boolean occupied = doctor.getAppointments().stream()
                .anyMatch(a -> a.getDate().equals(date) && a.getTime().equals(time));
        if (occupied) {
            showMessage("Doctor is busy at that time", Color.RED); return;
        }

        // Έλεγχος αν ο ασθενής έχει ήδη ραντεβού
        boolean patientOccupied = patient.getAppointments().stream()
                .anyMatch(a -> a.getDate().equals(date) && a.getTime().equals(time));
        if (patientOccupied) {
            showMessage("Patient already has an appointment at that time.", Color.RED);
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

    private void removePastAppointments() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<Appointment> allAppointments = FileManager.loadAppointments(doctors, patients);
        List<Appointment> futureAppointments = allAppointments.stream()
                .filter(app -> !app.getDate().isBefore(today) && !(app.getDate().equals(today) && app.getTime().isBefore(now)))
                .collect(Collectors.toList());
        if (futureAppointments.size() != allAppointments.size()) {
            FileManager.saveAllAppointments(futureAppointments);
        }
    }

    private void updateAgeDistribution() {
        int[] counts = new int[5];
        for (Patient p : patients) {
            int age = p.getAge();
            if (age <= 18) counts[0]++;
            else if (age <= 35) counts[1]++;
            else if (age <= 50) counts[2]++;
            else if (age <= 65) counts[3]++;
            else counts[4]++;
        }
        int total = patients.size();
        if (total == 0) total = 1;
        ageCount0_18.setText(String.valueOf(counts[0])); ageBar0_18.setProgress((double) counts[0] / total);
        ageCount19_35.setText(String.valueOf(counts[1])); ageBar19_35.setProgress((double) counts[1] / total);
        ageCount36_50.setText(String.valueOf(counts[2])); ageBar36_50.setProgress((double) counts[2] / total);
        ageCount51_65.setText(String.valueOf(counts[3])); ageBar51_65.setProgress((double) counts[3] / total);
        ageCount66plus.setText(String.valueOf(counts[4])); ageBar66plus.setProgress((double) counts[4] / total);
    }

    private void refreshAll() {
        removePastAppointments();
        doctors.setAll(FileManager.loadDoctors());
        patients.setAll(FileManager.loadPatients());
        appointments.setAll(FileManager.loadAppointments(doctors, patients));

        doctorComboBox.setItems(doctors);
        patientComboBox.setItems(patients);
        appointmentTable.setItems(appointments);

        totalDoctorsLabel.setText(String.valueOf(doctors.size()));
        totalPatientsLabel.setText(String.valueOf(patients.size()));
        LocalDate today = LocalDate.now();
        long todayCount = appointments.stream().filter(a -> a.getDate().equals(today)).count();
        todayAppointmentsLabel.setText(String.valueOf(todayCount));
        LocalDate nextWeek = today.plusDays(7);
        long upcomingCount = appointments.stream().filter(a -> a.getDate().isAfter(today) && a.getDate().isBefore(nextWeek)).count();
        upcomingAppointmentsLabel.setText(String.valueOf(upcomingCount));

        List<Appointment> upcomingList = appointments.stream()
                .filter(a -> (a.getDate().isEqual(today) || a.getDate().isAfter(today)) && a.getDate().isBefore(nextWeek))
                .sorted((a,b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
        upcomingTable.setItems(FXCollections.observableArrayList(upcomingList));
        upcomingDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        upcomingTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime().toString()));
        upcomingDoctorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctor().getName() + " (" + data.getValue().getDoctor().getSpecialty() + ")"));
        upcomingPatientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatient().getFullName() + " (" + data.getValue().getPatient().getAge() + " yrs)"));

        List<Doctor> todayDoctors = doctors.stream().filter(Doctor::isWorkingToday).collect(Collectors.toList());
        todayDoctorsListView.setItems(FXCollections.observableArrayList(todayDoctors));
        todayDoctorsListView.setCellFactory(lv -> new ListCell<Doctor>() {
            @Override protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) setText(null);
                else setText(doctor.getName() + " (" + doctor.getSpecialty() + ")");
            }
        });
        updateAgeDistribution();

        // Αναγκαστική ανανέωση του πίνακα των γιατρών ώστε να εμφανιστούν οι νέες ημέρες
        doctorTableView.setItems(null);
        doctorTableView.setItems(doctors);
        patientTableView.refresh();
    }

    private void showMessage(String msg, Color color) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Clinic System");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}