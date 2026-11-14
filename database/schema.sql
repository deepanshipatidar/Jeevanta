-- =====================================================================
-- Jeevanta - Emergency Healthcare & Hospital Bed Management System
-- Database Schema & Seed Data
-- =====================================================================

CREATE DATABASE IF NOT EXISTS hackathon_db;
USE hackathon_db;

-- ---------------------------------------------------------------------
-- Table: users
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NULL,
    medical_profile_json TEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: hospitals
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hospitals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    lat DECIMAL(10, 8) NOT NULL,
    lng DECIMAL(11, 8) NOT NULL,
    available_beds INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: doctors
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    hospital VARCHAR(255) NOT NULL,
    experience INT NOT NULL,
    rating DECIMAL(3, 1) DEFAULT 0.0,
    phone VARCHAR(20) DEFAULT NULL,
    fee DECIMAL(10, 2) NOT NULL,
    available TINYINT(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: bed_bookings
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bed_bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    hospital_id INT NOT NULL,
    booking_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('CONFIRMED', 'ADMITTED', 'DISCHARGED') DEFAULT 'CONFIRMED',
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Seed Data
-- =====================================================================

-- Seed Users
INSERT INTO users (id, name, email, password, phone, medical_profile_json) VALUES
(1, 'Prem Wadhwani', 'premwadhwani114@gmail.com', 'password123', '9876543210', '{"pastCases": ["flu 2024"], "allergies": ["Penicillin"], "chronicConditions": ["Asthma"]}'),
(2, 'Shantanu Joshi', 'shantanu14@gmail.com', 'password123', '9876543211', '{"pastCases": [], "allergies": ["Sulfa"], "chronicConditions": []}')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Seed Hospitals (Bhopal & surrounding demo coordinates)
INSERT INTO hospitals (id, name, lat, lng, available_beds) VALUES
(1, 'AIIMS Bhopal', 23.20433200, 77.46083800, 11),
(2, 'Bansal Hospital', 23.21045000, 77.43321500, 4),
(3, 'Hamidia Hospital', 23.26251200, 77.39758700, 0),
(4, 'Chirayu Medical College', 23.27351000, 77.31828300, 8),
(5, 'Navodaya Hospital', 23.22014500, 77.44123000, 2),
(6, 'Care Plus Hospital', 22.62710000, 75.78940000, 9),
(7, 'Unique Super Specialty Hospital', 22.69760000, 75.84350000, 14),
(8, 'Arihant Hospital & Research Centre', 22.70420000, 75.83280000, 21),
(9, 'Choithram Hospital & Research Centre', 22.68980000, 75.85210000, 45),
(10, 'Apple Hospital', 22.69150000, 75.86790000, 21)
ON DUPLICATE KEY UPDATE available_beds=VALUES(available_beds);

-- Seed Doctors
INSERT INTO doctors (id, name, specialization, hospital, experience, rating, phone, fee, available) VALUES
(1, 'Dr. John Doe', 'Cardiologist', 'City Heart Hospital', 15, 4.8, '+91-9876543210', 1500.00, 1),
(2, 'Dr. Sarah Smith', 'Dermatologist', 'Skin Care Clinic', 8, 4.5, '+91-9876543211', 800.00, 1),
(3, 'Dr. Rajesh Kumar', 'Neurologist', 'Apollo Brain Center', 20, 4.9, '+91-9876543212', 2000.00, 0),
(4, 'Dr. Emily White', 'Pediatrician', 'Happy Kids Hospital', 5, 4.2, '+91-9876543213', 600.00, 1)
ON DUPLICATE KEY UPDATE available=VALUES(available);

