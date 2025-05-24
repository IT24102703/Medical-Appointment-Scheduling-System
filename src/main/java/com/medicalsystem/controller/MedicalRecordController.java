package com.medicalsystem.controller;

import com.medicalsystem.model.Appointment;
import com.medicalsystem.model.EnhancedMedicalRecord;
import com.medicalsystem.model.MedicalRecord;
import com.medicalsystem.model.Patient;
import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.DoctorService;
import com.medicalsystem.service.MedicalRecordService;
import com.medicalsystem.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private DoctorService doctorService;

    @GetMapping("/add")
    public String showMedicalRecordForm(@RequestParam String appointmentId, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        MedicalRecord record = medicalRecordService.getRecordByAppointmentId(appointmentId);
        if (record == null) {
            record = new MedicalRecord();
            record.setAppointmentId(appointmentId);
            record.setPatientId(appointment.getPatientId());
            record.setDoctorId(appointment.getDoctorId());
        }
        model.addAttribute("record", record);
        return "records/form";
    }

    @PostMapping("/save")
    public String saveMedicalRecord(@ModelAttribute MedicalRecord record,
                                    RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Received form with Record ID: " + record.getRecordId());

            if (record.getRecordId() == null || record.getRecordId().isBlank()) {
                System.out.println("Creating new medical record...");
                medicalRecordService.createRecord(record);
            } else {
                System.out.println("Updating existing medical record...");
                medicalRecordService.updateRecord(record);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Medical record saved successfully");
            return "redirect:/appointments/doctor";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving medical record: " + e.getMessage());
            return "redirect:/records/add?appointmentId=" + record.getAppointmentId();
        }
    }

    @GetMapping("/patient")
    public String getPatientRecords(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Patient patient = patientService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<MedicalRecord> records = medicalRecordService.getRecordsByPatientId(patient.getPatientId());
        List<EnhancedMedicalRecord> enhancedRecords = records.stream().map(record -> {
            EnhancedMedicalRecord enhanced = new EnhancedMedicalRecord(record);
            enhanced.setAppointment(appointmentService.getAppointmentById(record.getAppointmentId()).orElse(null));
            enhanced.setDoctor(doctorService.findById(record.getDoctorId()).orElse(null));
            return enhanced;
        }).collect(Collectors.toList());

        model.addAttribute("patient", patient);
        model.addAttribute("records", enhancedRecords);

        return "records/patient-records";
    }

    @GetMapping("/view/{recordId}")
    public String viewMedicalRecord(@PathVariable String recordId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Patient patient = patientService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        MedicalRecord record = medicalRecordService.getRecordById(recordId);
        if (record == null || !record.getPatientId().equals(patient.getPatientId())) {
            throw new RuntimeException("Record not found or access denied");
        }
        EnhancedMedicalRecord enhancedRecord = new EnhancedMedicalRecord(record);
        enhancedRecord.setAppointment(appointmentService.getAppointmentById(record.getAppointmentId()).orElse(null));
        enhancedRecord.setDoctor(doctorService.findById(record.getDoctorId()).orElse(null));
        model.addAttribute("record", enhancedRecord);
        model.addAttribute("patient", patient);

        return "records/view_record";
    }

}