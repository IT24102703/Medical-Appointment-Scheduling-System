package com.medicalsystem.controller;

import com.medicalsystem.model.Doctor;
import com.medicalsystem.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "doctors/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute Doctor doctor) {
        doctorService.registerDoctor(
                doctor.getName(),
                doctor.getEmail(),
                doctor.getPassword(),
                doctor.getPhone(),
                doctor.getSpecialization()
        );
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        try {
            Doctor doctor = doctorService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            model.addAttribute("doctor", doctor);
            model.addAttribute("specializations", Arrays.asList(
                    "Cardiology", "Dermatology", "Neurology",
                    "Pediatrics", "Orthopedics", "General Practice"
            ));

            return "doctor/edit";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load doctor data");
            return "doctor/edit";
        }
    }

    @PostMapping("/update/{id}")
    public String updateDoctor(@PathVariable String id, @ModelAttribute Doctor doctor) {
        doctorService.updateDoctor(
                id,
                doctor.getName(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.getSpecialization()
        );
        return "redirect:/appointments/doctor";
    }

    @PostMapping("/delete/{id}")
    public String deleteDoctor(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deleteDoctor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Doctor account deleted successfully");
            return "redirect:/login?accountDeleted";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete doctor: " + e.getMessage());
            return "redirect:/doctors/edit/" + id;
        }
    }

    @PostMapping("/change-password/{id}")
    public String changePassword(@PathVariable String id,
                                 @RequestParam String newPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            doctorService.updatePassword(id, newPassword);
            redirectAttributes.addFlashAttribute("passwordChanged", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordChanged", false);
            redirectAttributes.addFlashAttribute("error", "Failed to change password");
        }
        return "redirect:/doctors/edit/" + id;
    }
}

