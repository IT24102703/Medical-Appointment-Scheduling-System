package com.medicalsystem.controller;

import com.medicalsystem.model.Doctor;
import com.medicalsystem.model.Patient;
import com.medicalsystem.model.UserType;
import com.medicalsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private PatientService patientService;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private SystemConfigService configService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private PaymentService paymentService;

    public LoginController(PatientService patientService,
                           DoctorService doctorService,
                           SystemConfigService configService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.configService = configService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            model.addAttribute("user", doctorService.findByEmail(email).orElseThrow());
            return "redirect:/appointments/doctor";
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            model.addAttribute("user", patientService.findByEmail(email).orElseThrow());
            return "redirect:patients/dashboard";
        }

        return "redirect:/login?error";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userType", UserType.values());
        return "register";
    }

    @GetMapping("/register/patient")
    public String showPatientRegisterForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "patient/register-patient";
    }

    @GetMapping("/register/doctor")
    public String showDoctorRegisterForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        model.addAttribute("specializations", List.of(
                "Cardiology", "Dermatology", "Neurology",
                "Pediatrics", "Orthopedics", "General Practice",
                "Oncology", "Psychiatry", "Surgery"
        ));
        return "doctor/register-doctor";
    }

    @PostMapping("/register/patient")
    public String registerPatient(@ModelAttribute Patient patient) {
        patientService.registerPatient(
                patient.getName(),
                patient.getEmail(),
                patient.getPassword(),
                patient.getPhone(),
                patient.getAge(),
                patient.getGender()
        );
        return "redirect:/login";
    }

    @PostMapping("/register/doctor")
    public String registerDoctor(@ModelAttribute Doctor doctor) {
        doctorService.registerDoctor(
                doctor.getName(),
                doctor.getEmail(),
                doctor.getPassword(),
                doctor.getPhone(),
                doctor.getSpecialization()
        );
        return "redirect:/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("config", configService.getConfig());
        model.addAttribute("patientCount", patientService.countPatients());
        model.addAttribute("doctorCount", doctorService.totalDoctors());
        model.addAttribute("appointmentCount", appointmentService.getCompletedAppointmentsCount());
        model.addAttribute("paymentTotal", paymentService.getTotal());
        model.addAttribute("recentAppointments", appointmentService.getRecentAppointments(5));
        model.addAttribute("recentPayments", paymentService.getRecentPayments(5));
        return "admin/dashboard";
    }

    @PostMapping("/admin/update-fee")
    public String updateAppointmentFee(@RequestParam double fee) {
        configService.updateAppointmentFee(fee);
        return "redirect:/admin/dashboard?success";
    }
}