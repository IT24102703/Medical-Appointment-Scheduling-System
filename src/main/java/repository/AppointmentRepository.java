package com.medicalsystem.repository;

import com.medicalsystem.model.Appointment;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class AppointmentRepository {
    private static final String FILE_PATH = "data/appointments.txt";
    private static final String TEMP_FILE_PATH = "data/appointments_temp.txt";
    private static final String DELIMITER = "\\|";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AppointmentRepository() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdir();
        File file = new File(FILE_PATH);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create file", e);
        }
    }

    public Appointment save(Appointment appointment) {
        if (appointment.getAppointmentId() == null) {
            appointment.setAppointmentId("APT-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            writer.println(convertToFileLine(appointment));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save appointment", e);
        }

        return appointment;
    }

    public List<Appointment> findAll() {
        List<Appointment> appointments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                appointments.add(convertFromLine(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read appointments", e);
        }
        return appointments;
    }

    public Optional<Appointment> findById(String id) {
        return findAll().stream()
                .filter(a -> a.getAppointmentId().equals(id))
                .findFirst();
    }

    public List<Appointment> findByPatientId(String patientId) {
        return findAll().stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<Appointment> findByDoctorId(String doctorId) {
        return findAll().stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public void update(Appointment updatedAppointment) {
        List<Appointment> appointments = findAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Appointment appointment : appointments) {
                if (appointment.getAppointmentId().equals(updatedAppointment.getAppointmentId())) {
                    writer.println(convertToFileLine(updatedAppointment));
                } else {
                    writer.println(convertToFileLine(appointment));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update appointment", e);
        }
    }

    public void delete(String id) {
        List<Appointment> appointments = findAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Appointment appointment : appointments) {
                if (!appointment.getAppointmentId().equals(id)) {
                    writer.println(convertToFileLine(appointment));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete appointment", e);
        }
    }

    private String convertToFileLine(Appointment appointment) {
        return String.join("|",
                appointment.getAppointmentId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getAppointmentTime().format(formatter),
                appointment.getStatus(),
                String.valueOf(appointment.getUrgencyLevel()),
                appointment.getReason());
    }

    private Appointment convertFromLine(String line) {
        String[] parts = line.split(DELIMITER);
        return new Appointment(
                parts[0], parts[1], parts[2],
                LocalDateTime.parse(parts[3], formatter),
                parts[4], Integer.parseInt(parts[5]), parts[6]);
    }

    public long getTotalCompletedAppointments() {
        return findAll().stream()
                .filter(a -> "completed".equalsIgnoreCase(a.getStatus()))
                .count();
    }

}