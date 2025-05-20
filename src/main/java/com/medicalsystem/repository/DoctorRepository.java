package com.medicalsystem.repository;

import com.medicalsystem.model.Doctor;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DoctorRepository {
    private static final String FILE_PATH = "data/doctors.txt";
    private static final String TEMP_FILE_PATH = "data/doctors_temp.txt";
    private static final String DELIMITER = "\\|";

    public DoctorRepository() {
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

    public Doctor save(Doctor doctor) {
        if (doctor.getDoctorId() == null) {
            doctor.setDoctorId(UUID.randomUUID().toString().substring(0, 8));
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            writer.println(convertToFileLine(doctor));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save doctor", e);
        }

        return doctor;
    }

    public Optional<Doctor> findByEmail(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Doctor doctor = convertFromLine(line);
                if (doctor.getEmail().equalsIgnoreCase(email)) {
                    return Optional.of(doctor);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to find doctor by email", e);
        }
        return Optional.empty();
    }

    private String convertToFileLine(Doctor doctor) {
        return String.join("|",
                doctor.getDoctorId(),
                doctor.getName(),
                doctor.getEmail(),
                doctor.getPassword(),
                doctor.getPhone(),
                doctor.getSpecialization());
    }

    private Doctor convertFromLine(String line) {
        String[] parts = line.split(DELIMITER);
        return new Doctor(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }

    public List<Doctor> findAll() {
        List<Doctor> doctors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    doctors.add(new Doctor(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read doctors", e);
        }
        return doctors;
    }

    public Optional<Doctor> findById(String id) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Doctor doctor = convertFromLine(line);
                if (doctor.getDoctorId().equals(id)) {
                    return Optional.of(doctor);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to find doctor by ID", e);
        }
        return Optional.empty();
    }

    public int getTotalDoctors() {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            while (reader.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to count doctors", e);
        }
        return count;
    }

    public void deleteById(String id) {
        File inputFile = new File(FILE_PATH);
        File tempFile = new File(TEMP_FILE_PATH);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (!parts[0].equals(id)) {
                    writer.println(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete doctor", e);
        }
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            throw new RuntimeException("Failed to update file after deletion");
        }
    }

    public void update(Doctor doctor) {
        File inputFile = new File(FILE_PATH);
        File tempFile = new File(TEMP_FILE_PATH);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts[0].equals(doctor.getDoctorId())) {
                    writer.println(convertToFileLine(doctor));
                } else {
                    writer.println(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update doctor", e);
        }
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            throw new RuntimeException("Failed to update file after modification");
        }
    }
}
