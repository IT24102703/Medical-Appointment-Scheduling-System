package com.medicalsystem.repository;

import com.medicalsystem.model.TimeSlot;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TimeSlotRepository {
    private static final String FILE_PATH = "data/timeslots.txt";
    private static final String DELIMITER = "\\|";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int SLOT_DURATION_MINUTES = 20;

    public TimeSlotRepository() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdir();
        File file = new File(FILE_PATH);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create file", e);
        }
    }

    public List<TimeSlot> save(TimeSlot timeSlot) {
        List<TimeSlot> createdSlots = new ArrayList<>();
        long durationMinutes = java.time.Duration.between(timeSlot.getStartTime(), timeSlot.getEndTime()).toMinutes();
        int numberOfSlots = (int) (durationMinutes / SLOT_DURATION_MINUTES);

        if (numberOfSlots < 1) {
            throw new IllegalArgumentException("Time slot must be at least 20 minutes");
        }
        LocalDateTime currentStart = timeSlot.getStartTime();
        for (int i = 0; i < numberOfSlots; i++) {
            LocalDateTime currentEnd = currentStart.plusMinutes(SLOT_DURATION_MINUTES);

            TimeSlot slot = new TimeSlot();
            slot.setSlotId("SLOT-" + UUID.randomUUID().toString().substring(0, 8));
            slot.setDoctorId(timeSlot.getDoctorId());
            slot.setStartTime(currentStart);
            slot.setEndTime(currentEnd);
            slot.setBooked(false);
            saveSingleSlot(slot);
            createdSlots.add(slot);
            currentStart = currentEnd;
        }

        return createdSlots;
    }

    private void saveSingleSlot(TimeSlot timeSlot) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            writer.println(convertToFileLine(timeSlot));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save time slot", e);
        }
    }

    public List<TimeSlot> findAll() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                timeSlots.add(convertFromLine(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read time slots", e);
        }
        return timeSlots;
    }

    public List<TimeSlot> findByDoctorId(String doctorId) {
        List<TimeSlot> allSlots = findAll();
        List<TimeSlot> doctorSlots = new ArrayList<>();
        for (TimeSlot slot : allSlots) {
            if (slot.getDoctorId().equals(doctorId)) {
                doctorSlots.add(slot);
            }
        }
        return doctorSlots;
    }

    public Optional<TimeSlot> findById(String id) {
        return findAll().stream()
                .filter(slot -> slot.getSlotId().equals(id))
                .findFirst();
    }

    public List<TimeSlot> findAvailableSlotsByDoctor(String doctorId) {
        return findAll().stream()
                .filter(slot -> slot.getDoctorId().equals(doctorId) && !slot.isBooked())
                .collect(Collectors.toList());
    }

    public TimeSlot update(TimeSlot updatedSlot) {
        List<TimeSlot> slots = findAll();
        boolean found = false;

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (TimeSlot slot : slots) {
                if (slot.getSlotId().equals(updatedSlot.getSlotId())) {
                    writer.println(convertToFileLine(updatedSlot));
                    found = true;
                } else {
                    writer.println(convertToFileLine(slot));
                }
            }

            if (!found) {
                throw new RuntimeException("Time slot not found with ID: " + updatedSlot.getSlotId());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update time slot", e);
        }

        return updatedSlot;
    }

    public void delete(String id) {
        List<TimeSlot> slots = findAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (TimeSlot slot : slots) {
                if (!slot.getSlotId().equals(id)) {
                    writer.println(convertToFileLine(slot));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete time slot", e);
        }
    }

    private String convertToFileLine(TimeSlot timeSlot) {
        return String.join("|",
                timeSlot.getSlotId(),
                timeSlot.getDoctorId(),
                timeSlot.getStartTime().format(formatter),
                timeSlot.getEndTime().format(formatter),
                String.valueOf(timeSlot.isBooked()));
    }

    private TimeSlot convertFromLine(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length != 5) {
            throw new RuntimeException("Invalid time slot record: " + line);
        }
        return new TimeSlot(
                parts[0], parts[1],
                LocalDateTime.parse(parts[2], formatter),
                LocalDateTime.parse(parts[3], formatter),
                Boolean.parseBoolean(parts[4]));
    }

    public List<TimeSlot> sortByStartTime(List<TimeSlot> slots) {
        TimeSlot[] slotArray = slots.toArray(new TimeSlot[0]);
        int n = slotArray.length;
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (slotArray[j].getStartTime().isAfter(slotArray[j+1].getStartTime())) {
                    TimeSlot temp = slotArray[j];
                    slotArray[j] = slotArray[j+1];
                    slotArray[j+1] = temp;
                }
            }
        }
        return Arrays.asList(slotArray);
    }
}