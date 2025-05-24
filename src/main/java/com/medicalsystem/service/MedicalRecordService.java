package com.medicalsystem.service;

import com.medicalsystem.model.EnhancedMedicalRecord;
import com.medicalsystem.model.MedicalRecord;
import com.medicalsystem.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {
    @Autowired
    private MedicalRecordRepository recordRepository;

    public void createRecord(MedicalRecord record) {
        String newId = UUID.randomUUID().toString().substring(0, 6);
        record.setRecordId(newId);
        System.out.println("Generated Record ID: " + newId);
        recordRepository.save(record);
    }

    public MedicalRecord getRecordByAppointmentId(String appointmentId) {
        return recordRepository.findByAppointmentId(appointmentId);
    }

    public List<MedicalRecord> getRecordsByPatientId(String patientId) {
        return recordRepository.findByPatientId(patientId);
    }

    public void updateRecord(MedicalRecord record) {
        System.out.println("Updating record in file: " + record.getRecordId());
        recordRepository.updateRecord(record);
    }

    public MedicalRecord getRecordById(String recordId) {
        return recordRepository.findById(recordId);
    }
}