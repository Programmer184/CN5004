package com.example.demo64965;

import javafx.beans.property.*;

public class Patient {
    private StringProperty name = new SimpleStringProperty();
    private IntegerProperty age = new SimpleIntegerProperty();
    private StringProperty medicalCondition = new SimpleStringProperty();

    public Patient(String name) {
        this.name.set(name);
        this.age.set(0);
        this.medicalCondition.set("Not specified");
    }

    public Patient(String name, int age, String medicalCondition) {
        this.name.set(name);
        this.age.set(age);
        this.medicalCondition.set(medicalCondition);
    }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public int getAge() { return age.get(); }
    public void setAge(int age) { this.age.set(age); }
    public IntegerProperty ageProperty() { return age; }

    public String getMedicalCondition() { return medicalCondition.get(); }
    public void setMedicalCondition(String condition) { this.medicalCondition.set(condition); }
    public StringProperty medicalConditionProperty() { return medicalCondition; }

    @Override
    public String toString() { return getName() + " (" + getAge() + " yrs, " + getMedicalCondition() + ")"; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Patient other = (Patient) obj;
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}