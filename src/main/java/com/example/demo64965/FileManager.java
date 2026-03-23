package com.example.demo64965;

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
        if (!doctors.contains(doctor)) {
            doctors.add(doctor);
        } else {
            doctors.set(doctors.indexOf(doctor), doctor);
        }
        writeDoctors(doctors);
    }

    public static void deleteDoctor(Doctor doctor) {
        List<Doctor> doctors = loadDoctors();
        doctors.remove(doctor);
        writeDoctors(doctors);
        // remove all appointments of this doctor
        List<Appointment> appointments = loadAppointments();
        appointments.removeIf(a -> a.getDoctor().equals(doctor));
        writeAppointments(appointments);
    }

    private static void writeDoctors(List<Doctor> doctors) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOCTORS_FILE))) {
            for (Doctor d : doctors) {
                StringBuilder sb = new StringBuilder(d.getName());
                for (DayOfWeek day : d.getWorkingDays()) {
                    sb.append(',').append(day.toString().substring(0, 3));
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
                String[] parts = line.split(",");
                String name = parts[0].trim();
                Doctor doctor = new Doctor(name);
                if (parts.length > 1) {
                    Set<DayOfWeek> days = new HashSet<>();
                    for (int i = 1; i < parts.length; i++) {
                        String dayAbbr = parts[i].trim().toUpperCase();
                        try {
                            DayOfWeek day = DayOfWeek.valueOf(dayAbbr);
                            days.add(day);
                        } catch (IllegalArgumentException e) { /* ignore */ }
                    }
                    if (!days.isEmpty()) doctor.setWorkingDays(days);
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
        if (!patients.contains(patient)) {
            patients.add(patient);
        } else {
            patients.set(patients.indexOf(patient), patient);
        }
        writePatients(patients);
    }

    public static void deletePatient(Patient patient) {
        List<Patient> patients = loadPatients();
        patients.remove(patient);
        writePatients(patients);
        List<Appointment> appointments = loadAppointments();
        appointments.removeIf(a -> a.getPatient().equals(patient));
        writeAppointments(appointments);
    }

    private static void writePatients(List<Patient> patients) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATIENTS_FILE))) {
            for (Patient p : patients) {
                writer.write(p.getName());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Patient> loadPatients() {
        List<Patient> patients = new ArrayList<>();
        if (!Files.exists(Paths.get(PATIENTS_FILE))) return patients;
        try (BufferedReader reader = new BufferedReader(new FileReader(PATIENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    patients.add(new Patient(line.trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return patients;
    }

    // ========== APPOINTMENTS ==========
    public static void saveAppointment(Appointment appointment) {
        List<Appointment> appointments = loadAppointments();
        appointments.removeIf(a -> a.getDoctor().equals(appointment.getDoctor()) &&
                a.getDate().equals(appointment.getDate()) &&
                a.getTime().equals(appointment.getTime()));
        appointments.add(appointment);
        writeAppointments(appointments);
    }

    public static void deleteAppointment(Appointment appointment) {
        List<Appointment> appointments = loadAppointments();
        appointments.remove(appointment);
        writeAppointments(appointments);
    }

    private static void writeAppointments(List<Appointment> appointments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (Appointment a : appointments) {
                writer.write(String.format("%s,%s,%s,%s",
                        a.getDoctor().getName(),
                        a.getPatient().getName(),
                        a.getDate(),
                        a.getTime()));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Appointment> loadAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        if (!Files.exists(Paths.get(APPOINTMENTS_FILE))) return appointments;
        List<Doctor> doctors = loadDoctors();
        List<Patient> patients = loadPatients();

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;
                String doctorName = parts[0].trim();
                String patientName = parts[1].trim();
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
                        .filter(p -> p.getName().equals(patientName))
                        .findFirst().orElse(null);
                if (doctor != null && patient != null) {
                    Appointment app = new Appointment(doctor, patient, date, time);
                    appointments.add(app);
                    doctor.getAppointments().add(app);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appointments;
    }
}