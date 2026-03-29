package com.example.demo64965;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Doctor {
    private StringProperty name = new SimpleStringProperty();
    private StringProperty specialty = new SimpleStringProperty();
    private List<Appointment> appointments = new ArrayList<>();
    private Set<DayOfWeek> workingDays = new HashSet<>();
    private StringProperty workingDaysDisplay = new SimpleStringProperty();

    public Doctor(String name) {
        this(name, "General Practitioner");
    }

    public Doctor(String name, String specialty) {
        this.name.set(name);
        this.specialty.set(specialty);
        workingDays.addAll(Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        updateWorkingDaysDisplay();
    }

    private void updateWorkingDaysDisplay() {
        String daysStr = workingDays.stream()
                .sorted()
                .map(d -> {
                    switch(d) {
                        case MONDAY: return "Mon";
                        case TUESDAY: return "Tue";
                        case WEDNESDAY: return "Wed";
                        case THURSDAY: return "Thu";
                        case FRIDAY: return "Fri";
                        case SATURDAY: return "Sat";
                        case SUNDAY: return "Sun";
                        default: return d.toString().substring(0, 3);
                    }
                })
                .collect(Collectors.joining(", "));
        workingDaysDisplay.set(daysStr.isEmpty() ? "None" : daysStr);
    }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getSpecialty() { return specialty.get(); }
    public void setSpecialty(String specialty) { this.specialty.set(specialty); }
    public StringProperty specialtyProperty() { return specialty; }

    public List<Appointment> getAppointments() { return appointments; }
    public Set<DayOfWeek> getWorkingDays() { return workingDays; }
    public void setWorkingDays(Set<DayOfWeek> days) {
        this.workingDays.clear();
        this.workingDays.addAll(days);
        updateWorkingDaysDisplay();
    }

    public String getWorkingDaysDisplay() { return workingDaysDisplay.get(); }
    public StringProperty workingDaysDisplayProperty() { return workingDaysDisplay; }

    public boolean isWorkingToday() {
        return workingDays.contains(LocalDate.now().getDayOfWeek());
    }

    @Override
    public String toString() { return getName() + " (" + getSpecialty() + ")"; }

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