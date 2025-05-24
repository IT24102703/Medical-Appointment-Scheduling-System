package com.medicalsystem.controller;

import com.medicalsystem.model.Doctor;
import com.medicalsystem.model.TimeSlot;
import com.medicalsystem.service.DoctorService;
import com.medicalsystem.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/timeslots")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;
    @Autowired
    private DoctorService doctorService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping("/doctor")
    public String showDoctorTimeSlots(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Doctor doctor = doctorService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        List<TimeSlot> timeSlots = timeSlotService.getTimeSlotsByDoctor(doctor.getDoctorId());
        model.addAttribute("doctor", doctor);
        model.addAttribute("timeSlots", timeSlots);
        return "timeslots/doctor_view";
    }


    @PostMapping("/create")
    public String createTimeSlot(@RequestParam String startTimeStr,
                                 @RequestParam String endTimeStr) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Doctor> doctor = doctorService.findByEmail(email);
        String doctorId = doctor.get().getDoctorId();
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        timeSlotService.createTimeSlot(doctorId, startTime, endTime);
        return "redirect:/timeslots/doctor";
    }

    @PostMapping("/update/{id}")
    public String updateTimeSlot(@PathVariable String id,
                                 @RequestParam String startTimeStr,
                                 @RequestParam String endTimeStr) {
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        TimeSlot timeSlot = timeSlotService.findById(id)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);

        timeSlotService.updateTimeSlot(timeSlot);
        return "redirect:/timeslots/doctor";
    }

    @PostMapping("/delete/{id}")
    public String deleteTimeSlot(@PathVariable String id) {
        timeSlotService.deleteTimeSlot(id);
        return "redirect:/timeslots/doctor";
    }
}