package com.medicalsystem.controller;

import com.medicalsystem.model.Doctor;
import com.medicalsystem.model.Patient;
import com.medicalsystem.model.Payment;
import com.medicalsystem.service.DoctorService;
import com.medicalsystem.service.PatientService;
import com.medicalsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private  DoctorService doctorService;
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/dashboard")
    public String patientDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Patient patient = patientService.findByEmail(email).orElseThrow();
        model.addAttribute("patient", patient);

        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);

        return "patient/dashboard";
    }

    @Autowired
    public PatientController(PatientService service, PasswordEncoder passwordEncoder) {
        this.patientService = service;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/save")
    public String savePatient(@ModelAttribute Patient patient) {
        String encryptedPassword = passwordEncoder.encode(patient.getPassword());
        patient.setPassword(encryptedPassword);

        patientService.registerPatient(
                patient.getName(),
                patient.getEmail(),
                patient.getPassword(),
                patient.getPhone(),
                patient.getAge(),
                patient.getGender()
        );
        return "redirect:/patients";
    }

    @GetMapping("/edit/{id}")
    public String editPatient(@PathVariable String id, Model model) {
        Optional<Patient> patient = patientService.findById(id);
        if (patient.isPresent()) {
            model.addAttribute("patient", patient.get());
            return "patient/edit";
        } else {
            return "redirect:/patients";
        }
    }

    @PostMapping("/update")
    public String updatePatient(@ModelAttribute Patient patient) {
        if (patient.getPassword() != null && !patient.getPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(patient.getPassword());
            patient.setPassword(encryptedPassword);
        }
        patientService.updatePatient(patient);
        return "redirect:/patients/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deletePatient(@PathVariable String id, Authentication authentication) {
        patientService.deletePatient(id);
        SecurityContextHolder.clearContext();

        return "redirect:/login?deleted";
    }

    @GetMapping("/payments/history")
    public String viewPaymentHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Patient patient = patientService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Payment> payments = paymentService.getByPatientId(patient.getPatientId());

        model.addAttribute("patient", patient);
        model.addAttribute("payments", payments);
        return "patient/payment_history";
    }
}