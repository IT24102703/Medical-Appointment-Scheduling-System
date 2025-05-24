package com.medicalsystem.controller;

import com.medicalsystem.dto.AppointmentAdminDTO;
import com.medicalsystem.model.Patient;
import com.medicalsystem.model.Payment;
import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.DoctorService;
import com.medicalsystem.service.PatientService;
import com.medicalsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.medicalsystem.model.Doctor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DoctorService doctorService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/doctors")
    public String allDoctors(Model model){
        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);
        return "admin/doctors";
    }

    @GetMapping("/patients")
    public String allPatients(Model model){
        List<Patient> patients = patientService.getAllPatients();
        model.addAttribute("patients", patients);
        return "admin/patients";
    }

    @GetMapping("/appointments")
    public String allAppointments(Model model) {
        List<AppointmentAdminDTO> dtos = appointmentService.getAllAppointments().stream()
                .map(appointment -> {
                    String patientName = patientService.findById(appointment.getPatientId())
                            .map(Patient::getName)
                            .orElse("Unknown Patient");

                    String doctorName = doctorService.findById(appointment.getDoctorId())
                            .map(Doctor::getName)
                            .orElse("Unknown Doctor");

                    return new AppointmentAdminDTO(
                            appointment.getAppointmentId(),
                            patientName,
                            doctorName,
                            appointment.getAppointmentTime(),
                            appointment.getStatus(),
                            appointment.getReason(),
                            appointment.getUrgencyLevel()
                    );
                })
                .collect(Collectors.toList());

        model.addAttribute("appointments", dtos);
        return "admin/appointments";
    }

    @GetMapping("/payments")
    public String allPayments(Model model){
        List<Payment> payments = paymentService.getAllPayments();
        model.addAttribute("payments", payments);
        return "admin/payments";
    }

    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deleteDoctor(id);
            redirectAttributes.addFlashAttribute("message",
                    new Message("SUCCESS", "Doctor deleted successfully"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    new Message("ERROR", "Failed to delete doctor: " + e.getMessage()));
        }
        return "redirect:/admin/doctors";
    }

    @PostMapping("/patients/delete/{id}")
    public String deletePatient(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            patientService.deletePatient(id);
            redirectAttributes.addFlashAttribute("message",
                    new Message("SUCCESS", "Patient deleted successfully"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    new Message("ERROR", "Failed to delete patient: " + e.getMessage()));
        }
        return "redirect:/admin/patients";
    }

    @PostMapping("/appointments/delete/{id}")
    public String deleteAppointment(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.deleteAppointment(id);
            redirectAttributes.addFlashAttribute("message",
                    new Message("SUCCESS", "Appointment deleted successfully"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    new Message("ERROR", "Failed to delete appointment: " + e.getMessage()));
        }
        return "redirect:/admin/appointments";
    }

    public static class Message {
        private String type;
        private String content;

        public Message(String type, String content) {
            this.type = type;
            this.content = content;
        }
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}
