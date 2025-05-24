package com.medicalsystem.service;

import com.medicalsystem.model.TimeSlot;
import com.medicalsystem.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimeSlotService {
    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<TimeSlot> createTimeSlot(String doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setDoctorId(doctorId);
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        timeSlot.setBooked(false);

        return timeSlotRepository.save(timeSlot);
    }

    public List<TimeSlot> getTimeSlotsByDoctor(String doctorId) {
        return timeSlotRepository.findByDoctorId(doctorId);
    }

    public List<TimeSlot> getAvailableTimeSlotsByDoctor(String doctorId) {
        return timeSlotRepository.findAvailableSlotsByDoctor(doctorId);
    }

    public Optional<TimeSlot> findById(String slotId) {
        return timeSlotRepository.findById(slotId);
    }

    public TimeSlot bookTimeSlot(String slotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + slotId));

        if (timeSlot.isBooked()) {
            throw new IllegalStateException("Time slot is already booked");
        }

        timeSlot.setBooked(true);
        return timeSlotRepository.update(timeSlot);
    }

    public TimeSlot updateTimeSlot(TimeSlot timeSlot) {
        timeSlotRepository.findById(timeSlot.getSlotId())
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + timeSlot.getSlotId()));

        return timeSlotRepository.update(timeSlot);
    }

    public void deleteTimeSlot(String slotId) {
        timeSlotRepository.delete(slotId);
    }

}