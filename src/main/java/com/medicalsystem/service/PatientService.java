package com.medicalsystem.service;

import com.medicalsystem.model.Patient;
import com.medicalsystem.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PatientService {
    @Autowired
    private PatientRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PatientRepository patientRepository;

    public PatientService(PatientRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Patient registerPatient(String name, String email, String rawPassword,
                                   String phone, int age, String gender) {
        String id = "P-" + UUID.randomUUID().toString().substring(0, 6);
        String hashedPassword = passwordEncoder.encode(rawPassword);
        Patient patient = new Patient(id, name, email, hashedPassword, phone, age, gender);
        repository.save(patient);
        return patient;
    }

    public Optional<Patient> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<Patient> getAllPatients() {
        return repository.getAllPatients();
    }

    public Optional<Patient> findById(String id) {
        return repository.findById(id);
    }

    public void updatePatient(Patient patient) {
        repository.update(patient);
    }

    public void deletePatient(String id) {
        repository.deleteById(id);
    }

    public int countPatients() {
        return patientRepository.getTotalPatients();
    }
}