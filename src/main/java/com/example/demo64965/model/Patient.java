package com.example.demo64965.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Patient {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty firstName = new SimpleStringProperty();
    private StringProperty lastName = new SimpleStringProperty();
    private IntegerProperty age = new SimpleIntegerProperty();
    private StringProperty medicalCondition = new SimpleStringProperty();
    private StringProperty phone = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();
    private ObjectProperty<LocalDate> lastVisitDate = new SimpleObjectProperty<>();
    private ObjectProperty<Doctor> lastVisitDoctor = new SimpleObjectProperty<>();
    private List<Appointment> appointments = new ArrayList<>();

    public Patient() {
        this(0, "", "", 0, "Not specified", "", "", null, null);
    }

    public Patient(int id, String firstName, String lastName, int age, String medicalCondition,
                   String phone, String email, LocalDate lastVisitDate, Doctor lastVisitDoctor) {
        this.id.set(id);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.age.set(age);
        this.medicalCondition.set(medicalCondition);
        this.phone.set(phone);
        this.email.set(email);
        this.lastVisitDate.set(lastVisitDate);
        this.lastVisitDoctor.set(lastVisitDoctor);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }
    public StringProperty firstNameProperty() { return firstName; }

    public String getLastName() { return lastName.get(); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }
    public StringProperty lastNameProperty() { return lastName; }

    public int getAge() { return age.get(); }
    public void setAge(int age) { this.age.set(age); }
    public IntegerProperty ageProperty() { return age; }

    public String getMedicalCondition() { return medicalCondition.get(); }
    public void setMedicalCondition(String condition) { this.medicalCondition.set(condition); }
    public StringProperty medicalConditionProperty() { return medicalCondition; }

    public String getPhone() { return phone.get(); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public StringProperty phoneProperty() { return phone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    public LocalDate getLastVisitDate() { return lastVisitDate.get(); }
    public void setLastVisitDate(LocalDate date) { this.lastVisitDate.set(date); }
    public ObjectProperty<LocalDate> lastVisitDateProperty() { return lastVisitDate; }

    public Doctor getLastVisitDoctor() { return lastVisitDoctor.get(); }
    public void setLastVisitDoctor(Doctor doctor) { this.lastVisitDoctor.set(doctor); }
    public ObjectProperty<Doctor> lastVisitDoctorProperty() { return lastVisitDoctor; }

    public List<Appointment> getAppointments() { return appointments; }

    public String getFullName() { return getFirstName() + " " + getLastName(); }

    @Override
    public String toString() { return getFullName() + " (" + getAge() + " yrs)"; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Patient other = (Patient) obj;
        return getId() == other.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}