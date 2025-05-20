package com.medicalsystem.service;

import com.medicalsystem.dto.AppointmentDTO;
import com.medicalsystem.model.Appointment;
import com.medicalsystem.model.TimeSlot;
import com.medicalsystem.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medicalsystem.model.Patient;
import com.medicalsystem.model.Doctor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private TimeSlotService timeSlotService;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private PatientService patientService;

    public AppointmentService(AppointmentRepository appointmentRepository, TimeSlotService timeSlotService) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotService = timeSlotService;
    }

    public Appointment createAppointment(String patientId, String doctorId,
                                         String slotId, int urgencyLevel, String reason) {
        TimeSlot timeSlot = timeSlotService.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        if (timeSlot.isBooked()) {
            throw new RuntimeException("Time slot is already booked");
        }

        if (!timeSlot.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Time slot doesn't belong to this doctor");
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentTime(timeSlot.getStartTime());
        appointment.setStatus("SCHEDULED");
        appointment.setUrgencyLevel(urgencyLevel);
        appointment.setReason(reason);
        timeSlotService.bookTimeSlot(slotId);

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(String id) {
        return appointmentRepository.findById(id);
    }

    public Appointment updateAppointment(Appointment updatedAppointment) {
        // Get existing appointment
        Appointment existing = appointmentRepository.findById(updatedAppointment.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Validate status transition
        if (existing.getStatus().equals("COMPLETED") || existing.getStatus().equals("CANCELLED")) {
            if (!updatedAppointment.getStatus().equals(existing.getStatus())) {
                throw new IllegalStateException("Cannot change status from " + existing.getStatus());
            }
        }

        // Validate urgency level
        if (updatedAppointment.getUrgencyLevel() < 1 || updatedAppointment.getUrgencyLevel() > 5) {
            throw new IllegalArgumentException("Urgency level must be between 1 and 5");
        }

        appointmentRepository.update(updatedAppointment);
        return updatedAppointment;
    }

    public void deleteAppointment(String id) {
        appointmentRepository.delete(id);
    }

    public Appointment rescheduleAppointment(String id, LocalDateTime newTime) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setAppointmentTime(newTime);
            appointmentRepository.update(appointment);
            return appointment;
        }
        return null;
    }

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentTime))
                .collect(Collectors.toList());
    }

    public boolean cancelAppointment(String appointmentId) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus("CANCELLED");
            appointmentRepository.update(appointment);
            return true;
        }
        return false;
    }

    public List<Appointment> getAppointmentsByDoctorSortedByUrgency(String doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        PriorityQueue<Appointment> priorityQueue = new PriorityQueue<>(
                (a1, a2) -> Integer.compare(a2.getUrgencyLevel(), a1.getUrgencyLevel())
        );
        priorityQueue.addAll(appointments);
        List<Appointment> sortedAppointments = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            sortedAppointments.add(priorityQueue.poll());
        }
        return sortedAppointments;
    }


    public List<Appointment> getAppointmentsByDoctorSortedByTime(String doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);

        // Bubble sort implementation for sorting appointments by time
        Appointment[] appointmentArray = appointments.toArray(new Appointment[0]);
        int n = appointmentArray.length;
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (appointmentArray[j].getAppointmentTime().isAfter(appointmentArray[j+1].getAppointmentTime())) {
                    // Swap appointments[j+1] and appointments[j]
                    Appointment temp = appointmentArray[j];
                    appointmentArray[j] = appointmentArray[j+1];
                    appointmentArray[j+1] = temp;
                }
            }
        }
        return Arrays.asList(appointmentArray);
    }

    public List<Appointment> getCompletedAppointmentsByDoctor(String doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .filter(a -> a.getStatus().equals("COMPLETED"))
                .sorted(Comparator.comparing(Appointment::getAppointmentTime).reversed())
                .collect(Collectors.toList());
    }

    public List<Appointment> getCompletedAppointmentsByPatient(String patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .filter(a -> a.getStatus().equals("COMPLETED"))
                .sorted(Comparator.comparing(Appointment::getAppointmentTime).reversed())
                .collect(Collectors.toList());
    }

    public long getCompletedAppointmentsCount() {
        return appointmentRepository.getTotalCompletedAppointments();
    }

    public List<AppointmentDTO> getRecentAppointments(int limit) {
        return appointmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentTime).reversed())
                .limit(limit)
                .map(appointment -> {
                    AppointmentDTO dto = new AppointmentDTO();
                    dto.setAppointmentId(appointment.getAppointmentId());
                    // Fetch and set patient and doctor names
                    String patientName = patientService.findById(appointment.getPatientId())
                            .map(Patient::getName)
                            .orElse("Unknown");
                    String doctorName = doctorService.findById(appointment.getDoctorId())
                            .map(Doctor::getName)
                            .orElse("Unknown");
                    dto.setPatientName(patientName);
                    dto.setDoctorName(doctorName);
                    dto.setAppointmentTime(appointment.getAppointmentTime());
                    dto.setStatus(appointment.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }

}