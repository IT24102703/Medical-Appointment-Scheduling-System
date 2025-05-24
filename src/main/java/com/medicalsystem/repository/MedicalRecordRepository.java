package com.medicalsystem.repository;

import com.medicalsystem.model.MedicalRecord;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MedicalRecordRepository {
    private static final String FILE_PATH = "data/records.txt";
    private static final String TEMP_FILE_PATH = "data/records_temp.txt";
    private static final String DELIMITER = "\\|";

    public MedicalRecordRepository() {
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

    public void save(MedicalRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            String line = String.join("|",
                    record.getRecordId(),
                    record.getAppointmentId(),
                    record.getPatientId(),
                    record.getDoctorId(),
                    record.getDiagnosis(),
                    record.getTreatment(),
                    record.getMedicines()
            );
            writer.write(line);
            writer.newLine();
            System.out.println("Saved record to file: " + line);  // ✅ debug here
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MedicalRecord findByAppointmentId(String appointmentId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 7 && parts[1].equals(appointmentId)) {
                    return new MedicalRecord(
                            parts[0], parts[1], parts[2], parts[3],
                            parts[4], parts[5], parts[6]
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MedicalRecord> findByPatientId(String patientId) {
        List<MedicalRecord> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 7 && parts[2].equals(patientId)) {
                    result.add(new MedicalRecord(
                            parts[0], parts[1], parts[2], parts[3],
                            parts[4], parts[5], parts[6]
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void updateRecord(MedicalRecord updatedRecord) {
        File inputFile = new File(FILE_PATH);
        File tempFile = new File(TEMP_FILE_PATH);
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 7 && parts[0].equals(updatedRecord.getRecordId())) {
                    String updatedLine = String.join("|",
                            updatedRecord.getRecordId(),
                            updatedRecord.getAppointmentId(),
                            updatedRecord.getPatientId(),
                            updatedRecord.getDoctorId(),
                            updatedRecord.getDiagnosis(),
                            updatedRecord.getTreatment(),
                            updatedRecord.getMedicines()
                    );
                    writer.write(updatedLine);
                    updated = true;
                    System.out.println("Updated record in temp file: " + updatedLine);
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }

            inputFile.delete();
            tempFile.renameTo(inputFile);

            if (!updated) {
                System.out.println("Warning: No record found to update with ID: " + updatedRecord.getRecordId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean existsByAppointmentId(String appointmentId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            return reader.lines()
                    .anyMatch(line -> {
                        String[] parts = line.split(DELIMITER);
                        return parts.length > 1 && parts[1].equals(appointmentId);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MedicalRecord findById(String recordId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 7 && parts[0].equals(recordId)) {
                    return new MedicalRecord(
                            parts[0], parts[1], parts[2], parts[3],
                            parts[4], parts[5], parts[6]
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
