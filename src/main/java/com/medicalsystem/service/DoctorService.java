package com.medicalsystem.service;

import com.medicalsystem.model.Doctor;
import com.medicalsystem.repository.DoctorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Doctor registerDoctor(String name, String email, String rawPassword,
                                 String phone, String specialization) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Doctor doctor = new Doctor(null, name, email, encodedPassword, phone, specialization);
        return doctorRepository.save(doctor);
    }

    public Optional<Doctor> findByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> findById(String id) {
        return doctorRepository.findById(id);
    }

    public int totalDoctors() {
        return doctorRepository.getTotalDoctors();
    }

    public void deleteDoctor(String id) {
        doctorRepository.deleteById(id);
    }

    public Doctor updateDoctor(String id, String name, String email,
                               String phone, String specialization) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setPhone(phone);
        doctor.setSpecialization(specialization);

        doctorRepository.update(doctor);
        return doctor;
    }

    public void updatePassword(String id, String newPassword) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setPassword(passwordEncoder.encode(newPassword));
        doctorRepository.update(doctor);
    }
}
