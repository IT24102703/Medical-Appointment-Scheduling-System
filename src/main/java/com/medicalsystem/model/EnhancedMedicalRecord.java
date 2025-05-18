package com.medicalsystem.model;

public class EnhancedMedicalRecord extends MedicalRecord {
    private Appointment appointment;
    private Doctor doctor;

    public EnhancedMedicalRecord(MedicalRecord record) {
        super(record.getRecordId(), record.getAppointmentId(), record.getPatientId(),
                record.getDoctorId(), record.getDiagnosis(), record.getTreatment(),
                record.getMedicines());
    }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
}
