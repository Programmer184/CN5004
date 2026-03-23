package com.example.demo64965;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class Doctor {
    private StringProperty name = new SimpleStringProperty();
    private List<Appointment> appointments = new ArrayList<>();
    private Set<DayOfWeek> workingDays = new HashSet<>();

    public Doctor(String name) {
        this.name.set(name);
        // Default: Monday to Friday
        workingDays.addAll(Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
    }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }
    public List<Appointment> getAppointments() { return appointments; }
    public Set<DayOfWeek> getWorkingDays() { return workingDays; }
    public void setWorkingDays(Set<DayOfWeek> days) { this.workingDays = days; }

    public boolean isWorkingToday() {
        return workingDays.contains(LocalDate.now().getDayOfWeek());
    }

    @Override
    public String toString() { return getName(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Doctor other = (Doctor) obj;
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}