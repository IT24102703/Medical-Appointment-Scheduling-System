package com.medicalsystem.controller;

import com.medicalsystem.dto.AppointmentAdminDTO;
import com.medicalsystem.dto.AppointmentDTO;
import com.medicalsystem.model.*;
import com.medicalsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private com.medicalsystem.service.AppointmentService appointmentService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private TimeSlotService timeSlotService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private SystemConfigService configService;

    public AppointmentController(com.medicalsystem.service.AppointmentService appointmentService,
                                 PatientService patientService,
                                 DoctorService doctorService,
                                 TimeSlotService timeSlotService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.timeSlotService = timeSlotService;
    }

    @PostMapping("/update")
    public String updateAppointment(@ModelAttribute Appointment appointment,
                                    @RequestParam String appointmentTimeStr) {
        LocalDateTime appointmentTime = LocalDateTime.parse(appointmentTimeStr,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        appointment.setAppointmentTime(appointmentTime);
        appointmentService.updateAppointment(appointment);
        return "redirect:/appointments";
    }

    @GetMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable String id) {
        appointmentService.cancelAppointment(id);
        return "redirect:/appointments/patient";
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable String id) {
        appointmentService.deleteAppointment(id);
        return "redirect:/appointments/patient";
    }

    @GetMapping("/doctor")
    public String doctorAppointments(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Doctor doctor = doctorService.findByEmail(email).orElseThrow();
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorSortedByUrgency(doctor.getDoctorId());
        Map<String, String> patientNameMap = new HashMap<>();
        for (Appointment appointment : appointments) {
            String patientName = patientService.findById(appointment.getPatientId())
                    .map(Patient::getName)
                    .orElse("Unknown Patient");
            patientNameMap.put(appointment.getAppointmentId(), patientName);
        }
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        model.addAttribute("patientNameMap", patientNameMap);
        return "doctor/dashboard";
    }

    @PostMapping("/reschedule")
    public String rescheduleAppointment(@RequestParam String id,
                                        @RequestParam String newAppointmentTime) {
        LocalDateTime newTime = LocalDateTime.parse(newAppointmentTime,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        appointmentService.rescheduleAppointment(id, newTime);
        return "redirect:/appointments/patient";
    }

    @GetMapping("/book")
    public String showBookingForm(@RequestParam String doctorId, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Patient patient = patientService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            Doctor doctor = doctorService.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));
            List<TimeSlot> availableSlots = timeSlotService.getAvailableTimeSlotsByDoctor(doctorId);
            double appointmentFee = configService.getConfig().getAppointmentFee();
            model.addAttribute("patient", patient);
            model.addAttribute("doctor", doctor);
            model.addAttribute("appointment", new Appointment());
            model.addAttribute("availableSlots", availableSlots);
            model.addAttribute("appointmentFee", appointmentFee);

            return "appointment/book";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/patients/dashboard?error=Doctor+not+found";
        }
    }

    @PostMapping("/book")
    public String bookAppointment(@ModelAttribute Appointment appointment,
                                  @RequestParam String doctorId,
                                  @RequestParam String slotId,
                                  @RequestParam String cardNumber,
                                  @RequestParam String cardHolderName,
                                  @RequestParam String expiryDate,
                                  @RequestParam String cvv,
                                  RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Patient patient = patientService.findByEmail(email).orElseThrow();

        try {
            Appointment createdAppointment = appointmentService.createAppointment(
                    patient.getPatientId(),
                    doctorId,
                    slotId,
                    appointment.getUrgencyLevel(),
                    appointment.getReason()
            );
            double fee = configService.getConfig().getAppointmentFee();
            paymentService.processPayment(
                    createdAppointment.getAppointmentId(),
                    patient.getPatientId(),
                    fee,
                    "CREDIT_CARD",
                    cardNumber,
                    cardHolderName,
                    expiryDate,
                    cvv
            );
            redirectAttributes.addFlashAttribute("successMessage",
                    "Appointment booked and payment of " + fee + " Lkr processed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error booking appointment: " + e.getMessage());
        }
        return "redirect:/patients/dashboard";
    }

    @GetMapping("/view/{id}")
    public String viewAppointmentDetails(@PathVariable String id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        Doctor doctor = doctorService.findByEmail(email).orElse(null);
        Patient patient = patientService.findByEmail(email).orElse(null);
        if (doctor != null && !appointment.getDoctorId().equals(doctor.getDoctorId())) {
            throw new SecurityException("You can only view your own appointments");
        }
        if (patient != null && !appointment.getPatientId().equals(patient.getPatientId())) {
            throw new SecurityException("You can only view your own appointments");
        }
        if (doctor == null && patient == null) {
            throw new SecurityException("Unauthorized access");
        }
        MedicalRecord medicalRecord = medicalRecordService.getRecordByAppointmentId(id);
        model.addAttribute("medicalRecord", medicalRecord);
        model.addAttribute("appointment", appointment);
        model.addAttribute("doctor", doctor != null ? doctor : doctorService.findById(appointment.getDoctorId()).orElse(null));
        return "appointment/view";
    }

    @GetMapping("/doctor/sorted-by-time")
    public String viewDoctorAppointmentsSortedByTime(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Doctor doctor = doctorService.findByEmail(email).orElseThrow();
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorSortedByTime(doctor.getDoctorId());
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(appointment -> {
                    String patientName = patientService.findById(appointment.getPatientId())
                            .map(Patient::getName)
                            .orElse("Unknown Patient");
                    return new AppointmentDTO(
                            appointment.getAppointmentId(),
                            patientName,
                            doctor.getName(),
                            appointment.getAppointmentTime(),
                            appointment.getStatus()
                    );
                })
                .collect(Collectors.toList());
        Map<String, String> patientNameMap = new HashMap<>();
        for (AppointmentDTO dto : appointmentDTOs) {
            patientNameMap.put(dto.getAppointmentId(), dto.getPatientName());
        }
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        model.addAttribute("appointmentDTOs", appointmentDTOs);
        model.addAttribute("patientNameMap", patientNameMap);
        return "doctor/dashboard";
    }

    @GetMapping("/patient")
    public String viewPatientAppointments(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Patient patient = patientService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient.getPatientId());
        List<AppointmentAdminDTO> appointmentDTOs = appointments.stream()
                .map(appointment -> {
                    Doctor doctor = doctorService.findById(appointment.getDoctorId())
                            .orElseThrow(() -> new RuntimeException("Doctor not found"));
                    return new AppointmentAdminDTO(
                            appointment.getAppointmentId(),
                            patient.getName(),
                            doctor.getName(),
                            appointment.getAppointmentTime(),
                            appointment.getStatus(),
                            appointment.getReason(),
                            appointment.getUrgencyLevel()
                    );
                })
                .collect(Collectors.toList());

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointmentDTOs);
        return "appointment/patient_list";
    }

    @GetMapping("/reschedule/{id}")
    public String showRescheduleForm(@PathVariable String id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Patient patient = patientService.findByEmail(email).orElseThrow();
        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        model.addAttribute("patient", patient);
        model.addAttribute("appointment", appointment);

        return "appointment/reschedule";
    }

    @PostMapping("/reschedule/{id}")
    public String rescheduleAppointment(@PathVariable String id,
                                        @RequestParam String newAppointmentTime,
                                        RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime newTime = LocalDateTime.parse(newAppointmentTime,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            Appointment appointment = appointmentService.getAppointmentById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            appointment.setAppointmentTime(newTime);
            appointmentService.updateAppointment(appointment);

            redirectAttributes.addFlashAttribute("successMessage", "Appointment rescheduled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rescheduling appointment");
        }

        return "redirect:/appointments/patient";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable String id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Doctor doctor = doctorService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.getDoctorId().equals(doctor.getDoctorId())) {
            throw new SecurityException("You can only update your own appointments");
        }
        model.addAttribute("appointment", appointment);
        return "appointment/update";
    }

    @PostMapping("/update/{id}")
    public String updateAppointment(@PathVariable String id,
                                    @ModelAttribute Appointment updatedAppointment,
                                    RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Doctor doctor = doctorService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            Appointment existing = appointmentService.getAppointmentById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            if (!existing.getDoctorId().equals(doctor.getDoctorId())) {
                throw new SecurityException("You can only update your own appointments");
            }
            updatedAppointment.setAppointmentId(id);
            updatedAppointment.setPatientId(existing.getPatientId());
            updatedAppointment.setDoctorId(existing.getDoctorId());
            updatedAppointment.setAppointmentTime(existing.getAppointmentTime());
            updatedAppointment.setReason(existing.getReason());
            appointmentService.updateAppointment(updatedAppointment);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating appointment: " + e.getMessage());
        }
        return "redirect:/appointments/doctor";
    }

    @GetMapping("/doctor/history")
    public String viewDoctorAppointmentHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Doctor doctor = doctorService.findByEmail(email).orElseThrow();
        List<Appointment> completedAppointments = appointmentService.getCompletedAppointmentsByDoctor(doctor.getDoctorId());
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", completedAppointments);
        model.addAttribute("patientService", patientService);
        return "doctor/appointments_history";
    }

    @GetMapping("/patient/history")
    public String viewPatientAppointmentHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Patient patient = patientService.findByEmail(email).orElseThrow();
        List<Appointment> completedAppointments = appointmentService.getCompletedAppointmentsByPatient(patient.getPatientId());
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", completedAppointments);
        model.addAttribute("doctorService", doctorService);
        return "patient/patient_history";
    }
}