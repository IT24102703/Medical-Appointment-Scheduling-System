package com.medicalsystem.repository;

import com.medicalsystem.model.Patient;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
public class PatientRepository {
    private static final String FILE_PATH = "data/patients.txt";

    public PatientRepository() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create patients.txt file", e);
            }
        }
    }

    public void save(Patient patient) {
        ensureFileExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(String.join(",",
                    patient.getPatientId(),
                    patient.getName(),
                    patient.getEmail(),
                    patient.getPassword(),
                    patient.getPhone(),
                    patient.getAge().toString(),
                    patient.getGender()));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save patient", e);
        }
    }

    public Optional<Patient> findByEmail(String email) {
        return getAllPatients().stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<Patient> findById(String id) {
        return getAllPatients().stream()
                .filter(p -> p.getPatientId().equals(id))
                .findFirst();
    }

    public List<Patient> getAllPatients() {
        ensureFileExists();
        List<Patient> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length == 7) {
                    list.add(new Patient(
                            tokens[0], tokens[1], tokens[2], tokens[3],
                            tokens[4], Integer.parseInt(tokens[5]), tokens[6]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read patients", e);
        }
        return list;
    }

    public void update(Patient updatedPatient) {
        ensureFileExists();
        List<Patient> patients = getAllPatients();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Patient patient : patients) {
                if (patient.getPatientId().equals(updatedPatient.getPatientId())) {
                    writer.write(String.join(",",
                            updatedPatient.getPatientId(),
                            updatedPatient.getName(),
                            updatedPatient.getEmail(),
                            updatedPatient.getPassword(),
                            updatedPatient.getPhone(),
                            updatedPatient.getAge().toString(),
                            updatedPatient.getGender()));
                } else {
                    writer.write(String.join(",",
                            patient.getPatientId(),
                            patient.getName(),
                            patient.getEmail(),
                            patient.getPassword(),
                            patient.getPhone(),
                            patient.getAge().toString(),
                            patient.getGender()));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update patient", e);
        }
    }

    public void deleteById(String id) {
        ensureFileExists();
        List<Patient> patients = getAllPatients();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Patient patient : patients) {
                if (!patient.getPatientId().equals(id)) {
                    writer.write(String.join(",",
                            patient.getPatientId(),
                            patient.getName(),
                            patient.getEmail(),
                            patient.getPassword(),
                            patient.getPhone(),
                            patient.getAge().toString(),
                            patient.getGender()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete patient", e);
        }
    }

    public int getTotalPatients() {
        ensureFileExists();
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            while (reader.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to count patients", e);
        }
        return count;
    }
}