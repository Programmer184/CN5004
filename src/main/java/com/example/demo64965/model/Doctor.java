package com.example.demo64965.model;

import javafx.beans.property.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Doctor {
    private StringProperty name = new SimpleStringProperty();
    private StringProperty specialty = new SimpleStringProperty();
    private StringProperty phone = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();
    private IntegerProperty yearsOfExperience = new SimpleIntegerProperty();
    private StringProperty bio = new SimpleStringProperty();
    private List<Appointment> appointments = new ArrayList<>();
    private Set<DayOfWeek> workingDays = new HashSet<>();
    private StringProperty workingDaysDisplay = new SimpleStringProperty();

    public Doctor(String name) {
        this(name, "General Practitioner", "", "", 0, "");
    }

    public Doctor(String name, String specialty) {
        this(name, specialty, "", "", 0, "");
    }

    public Doctor(String name, String specialty, String phone, String email, int yearsOfExperience, String bio) {
        this.name.set(name);
        this.specialty.set(specialty);
        this.phone.set(phone);
        this.email.set(email);
        this.yearsOfExperience.set(yearsOfExperience);
        this.bio.set(bio);
        // Αρχικοποίηση με Δευτέρα-Παρασκευή
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

    public String getPhone() { return phone.get(); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public StringProperty phoneProperty() { return phone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    public int getYearsOfExperience() { return yearsOfExperience.get(); }
    public void setYearsOfExperience(int years) { this.yearsOfExperience.set(years); }
    public IntegerProperty yearsOfExperienceProperty() { return yearsOfExperience; }

    public String getBio() { return bio.get(); }
    public void setBio(String bio) { this.bio.set(bio); }
    public StringProperty bioProperty() { return bio; }

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