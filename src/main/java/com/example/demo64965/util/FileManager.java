package com.example.demo64965.util;

import com.example.demo64965.model.Appointment;
import com.example.demo64965.model.Doctor;
import com.example.demo64965.model.Patient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class FileManager {

    private static final String DOCTORS_FILE = "doctors.csv";
    private static final String PATIENTS_FILE = "patients.csv";
    private static final String APPOINTMENTS_FILE = "appointments.csv";

    // ========== DOCTORS ==========
    public static void saveDoctor(Doctor doctor) {
        List<Doctor> doctors = loadDoctors();
        doctors.removeIf(d -> d.getName().equals(doctor.getName()));
        doctors.add(doctor);
        writeDoctors(doctors);
    }

    public static void renameDoctor(Doctor doctor, String newName) {
        String oldName = doctor.getName();
        if (oldName.equals(newName)) return;
        List<Doctor> doctors = loadDoctors();
        if (doctors.stream().anyMatch(d -> d.getName().equals(newName))) {
            throw new IllegalArgumentException("Doctor with name " + newName + " already exists.");
        }
        doctors.removeIf(d -> d.getName().equals(oldName));
        doctor.setName(newName);
        doctors.add(doctor);
        writeDoctors(doctors);
        List<Appointment> appointments = loadAppointments(loadDoctors(), loadPatients());
        boolean changed = false;
        for (Appointment a : appointments) {
            if (a.getDoctor().getName().equals(oldName)) {
                a.getDoctor().setName(newName);
                changed = true;
            }
        }
        if (changed) writeAppointments(appointments);
    }

    public static void deleteDoctor(Doctor doctor) {
        List<Doctor> doctors = loadDoctors();
        doctors.removeIf(d -> d.getName().equals(doctor.getName()));
        writeDoctors(doctors);
        List<Appointment> appointments = loadAppointments(loadDoctors(), loadPatients());
        appointments.removeIf(a -> a.getDoctor().getName().equals(doctor.getName()));
        writeAppointments(appointments);
    }

    private static void writeDoctors(List<Doctor> doctors) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOCTORS_FILE))) {
            for (Doctor d : doctors) {
                StringBuilder sb = new StringBuilder();
                sb.append(d.getName()).append(",")
                        .append(d.getSpecialty()).append(",")
                        .append(d.getPhone()).append(",")
                        .append(d.getEmail()).append(",")
                        .append(d.getYearsOfExperience()).append(",")
                        .append(d.getBio().replace(",", " "));
                List<DayOfWeek> sortedDays = new ArrayList<>(d.getWorkingDays());
                Collections.sort(sortedDays);
                for (DayOfWeek day : sortedDays) {
                    sb.append(",").append(day.toString().substring(0, 3));
                }
                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Doctor> loadDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        if (!Files.exists(Paths.get(DOCTORS_FILE))) return doctors;

        try (BufferedReader reader = new BufferedReader(new FileReader(DOCTORS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                String name = parts[0].trim();
                String specialty = parts.length > 1 ? parts[1].trim() : "General Practitioner";
                String phone = parts.length > 2 ? parts[2].trim() : "";
                String email = parts.length > 3 ? parts[3].trim() : "";
                int years = 0;
                if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                    try { years = Integer.parseInt(parts[4].trim()); } catch (NumberFormatException e) {}
                }
                String bio = parts.length > 5 ? parts[5].trim() : "";

                Doctor doctor = new Doctor(name, specialty, phone, email, years, bio);

                Set<DayOfWeek> days = new HashSet<>();
                for (int i = 6; i < parts.length; i++) {
                    String token = parts[i].trim().toUpperCase();
                    if (token.isEmpty()) continue;
                    String abbr = token.length() >= 3 ? token.substring(0, 3) : token;
                    try {
                        days.add(DayOfWeek.valueOf(abbr));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (!days.isEmpty()) {
                    doctor.setWorkingDays(days);
                } else {
                    doctor.setWorkingDays(new HashSet<>(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)));
                }
                doctors.add(doctor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    // ========== PATIENTS ==========
    public static void savePatient(Patient patient) {
        List<Patient> patients = loadPatients();
        patients.removeIf(p -> p.getId() == patient.getId());
        patients.add(patient);
        writePatients(patients);
    }

    public static void deletePatient(Patient patient) {
        List<Patient> patients = loadPatients();
        patients.removeIf(p -> p.getId() == patient.getId());
        writePatients(patients);
        List<Appointment> appointments = loadAppointments(loadDoctors(), loadPatients());
        appointments.removeIf(a -> a.getPatient().getId() == patient.getId());
        writeAppointments(appointments);
    }

    private static void writePatients(List<Patient> patients) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATIENTS_FILE))) {
            for (Patient p : patients) {
                writer.write(String.format("%d,%s,%s,%d,%s,%s,%s,%s,%s",
                        p.getId(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getAge(),
                        p.getMedicalCondition(),
                        p.getPhone(),
                        p.getEmail(),
                        p.getLastVisitDate() != null ? p.getLastVisitDate().toString() : "",
                        p.getLastVisitDoctor() != null ? p.getLastVisitDoctor().getName() : ""));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Patient> loadPatients() {
        List<Patient> patients = new ArrayList<>();
        if (!Files.exists(Paths.get(PATIENTS_FILE))) return patients;

        boolean needRewrite = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(PATIENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String firstName = parts[1].trim();
                        String lastName = parts[2].trim();
                        int age = Integer.parseInt(parts[3].trim());
                        String condition = parts[4].trim();
                        String phone = parts[5].trim();
                        String email = parts[6].trim();
                        LocalDate lastVisit = parts[7].trim().isEmpty() ? null : LocalDate.parse(parts[7].trim());
                        String lastDocName = parts[8].trim();
                        Doctor lastDoc = lastDocName.isEmpty() ? null : new Doctor(lastDocName);
                        patients.add(new Patient(id, firstName, lastName, age, condition, phone, email, lastVisit, lastDoc));
                    } catch (Exception e) {
                        needRewrite = true;
                        processOldPatient(parts, patients);
                    }
                } else {
                    needRewrite = true;
                    processOldPatient(parts, patients);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (needRewrite && !patients.isEmpty()) {
            writePatients(patients);
        }
        return patients;
    }

    private static void processOldPatient(String[] parts, List<Patient> patients) {
        if (parts.length == 0) return;
        String fullName = parts[0].trim();
        int age = 0;
        String condition = "Not specified";
        String phone = "";
        String email = "";

        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            try {
                age = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                condition = parts[1].trim();
            }
        }
        if (parts.length > 2 && !parts[2].trim().isEmpty()) {
            condition = parts[2].trim();
        }
        if (parts.length > 3 && !parts[3].trim().isEmpty()) {
            phone = parts[3].trim();
        }
        if (parts.length > 4 && !parts[4].trim().isEmpty()) {
            email = parts[4].trim();
        }

        String firstName = fullName;
        String lastName = "";
        int spaceIdx = fullName.indexOf(' ');
        if (spaceIdx > 0) {
            firstName = fullName.substring(0, spaceIdx);
            lastName = fullName.substring(spaceIdx + 1);
        }

        int nextId = patients.stream().mapToInt(Patient::getId).max().orElse(0) + 1;
        patients.add(new Patient(nextId, firstName, lastName, age, condition, phone, email, null, null));
    }

    // ========== APPOINTMENTS ==========
    public static void saveAppointment(Appointment appointment) {
        List<Appointment> appointments = loadAppointments(loadDoctors(), loadPatients());
        appointments.removeIf(a -> a.getDoctor().getName().equals(appointment.getDoctor().getName()) &&
                a.getDate().equals(appointment.getDate()) &&
                a.getTime().equals(appointment.getTime()));
        appointments.add(appointment);
        writeAppointments(appointments);
    }

    public static void deleteAppointment(Appointment appointment) {
        List<Appointment> appointments = loadAppointments(loadDoctors(), loadPatients());
        appointments.removeIf(a -> a.equals(appointment));
        writeAppointments(appointments);
    }

    public static void saveAllAppointments(List<Appointment> appointments) {
        writeAppointments(appointments);
    }

    private static void writeAppointments(List<Appointment> appointments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (Appointment a : appointments) {
                writer.write(String.format("%s,%s,%s,%s",
                        a.getDoctor().getName(),
                        a.getPatient().getFullName(),
                        a.getDate(),
                        a.getTime()));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Φορτώνει τα ραντεβού από το CSV και ενημερώνει ΤΑΥΤΟΧΡΟΝΑ τις λίστες
     * των γιατρών και των ασθενών που περνιούνται ως παράμετροι.
     */
    public static List<Appointment> loadAppointments(List<Doctor> doctors, List<Patient> patients) {
        List<Appointment> appointments = new ArrayList<>();
        if (!Files.exists(Paths.get(APPOINTMENTS_FILE))) return appointments;

        // Καθαρίζουμε προηγούμενα ραντεβού από τις λίστες γιατρών/ασθενών
        for (Doctor d : doctors) d.getAppointments().clear();
        for (Patient p : patients) p.getAppointments().clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;
                String doctorName = parts[0].trim();
                String patientFullName = parts[1].trim();
                LocalDate date;
                LocalTime time;
                try {
                    date = LocalDate.parse(parts[2].trim());
                    time = LocalTime.parse(parts[3].trim());
                } catch (DateTimeParseException e) {
                    continue;
                }
                Doctor doctor = doctors.stream()
                        .filter(d -> d.getName().equals(doctorName))
                        .findFirst().orElse(null);
                Patient patient = patients.stream()
                        .filter(p -> p.getFullName().equals(patientFullName))
                        .findFirst().orElse(null);
                if (doctor != null && patient != null) {
                    Appointment app = new Appointment(doctor, patient, date, time);
                    appointments.add(app);
                    doctor.getAppointments().add(app);
                    patient.getAppointments().add(app);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appointments;
    }
}