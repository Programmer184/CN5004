package com.example.demo64965.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Appointment {
    private Doctor doctor;
    private Patient patient;
    private LocalDate date;
    private LocalTime time;

    public Appointment(Doctor doctor, Patient patient, LocalDate date, LocalTime time) {
        this.doctor = doctor;
        this.patient = patient;
        this.date = date;
        this.time = time;
    }

    public Doctor getDoctor() { return doctor; }
    public Patient getPatient() { return patient; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(doctor, that.doctor) &&
                Objects.equals(patient, that.patient) &&
                Objects.equals(date, that.date) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctor, patient, date, time);
    }
}