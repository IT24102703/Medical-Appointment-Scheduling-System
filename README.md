# Medical Management System (Backend)

A Spring Boot-based backend system for managing healthcare services including patients, doctors, appointments, timeslots, medical records, and payments. This system is designed for use by hospital staff, doctors, and administrators.

## Features
- Patient Management
- Doctor Management
- Appointment Scheduling
- Time Slot Management
- Medical Record Management
- Payment Handling
- User Authentication & Role-Based Access Control (Admin, Doctor, Patient)

## Tech Stack
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL (or H2 for testing)
- Maven
- Lombok
- IntelliJ IDEA

## Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL

### Clone the Repository
```bash
git clone https://github.com/IT24102703/Medical-Appointment-Scheduling-System.git
cd Medical-Appointment-Scheduling-System
```

### Configure the Database
Edit `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/medical_db
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### Run the Application
```bash
mvn spring-boot:run
```

## Key API Endpoints

### Patients
- `GET /api/patients`
- `POST /api/patients`

### Doctors
- `GET /api/doctors`
- `POST /api/doctors`

### Appointments
- `POST /api/appointments`
- `GET /api/appointments/{id}`

### TimeSlots
- `GET /api/timeslots`
- `POST /api/timeslots`

### Medical Records
- `GET /api/records`
- `POST /api/records`

### Payments
- `GET /api/payments`
- `POST /api/payments`

## Team

- Saadh – Patient Management
- Shakeeb – Doctor Management
- Dilmith – Time Slot Management
- Aadhil – Appointment Management
- Roshan – Medical Record Management
- Dinith – Payment Management

