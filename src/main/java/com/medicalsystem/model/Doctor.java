package com.medicalsystem.model;

public class Doctor {
    private String doctorId;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String specialization;

    public Doctor() {
    }

    public Doctor(String doctorId, String name, String email, String password, String phone, String specialization) {
        this.doctorId = doctorId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.specialization = specialization;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
