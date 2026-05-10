-- SmartCampus Database Schema
-- Spring Boot will execute this file automatically

-- 1. Authentication & Users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active CHAR(1) DEFAULT 'Y'
);

-- 2. Master Data
CREATE TABLE IF NOT EXISTS department_master (
    dept_id INT AUTO_INCREMENT PRIMARY KEY,
    dept_name VARCHAR(255) NOT NULL,
    dept_code VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS subject_master (
    sub_id INT AUTO_INCREMENT PRIMARY KEY,
    subject_name VARCHAR(255) NOT NULL,
    subject_code VARCHAR(50) NOT NULL UNIQUE,
    dept_code VARCHAR(50)
);

-- 3. Profiles
CREATE TABLE IF NOT EXISTS student_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fullname VARCHAR(255) NOT NULL,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100),
    year_of_study VARCHAR(50),
    phone VARCHAR(20),
    dob DATE,
    department VARCHAR(100),
    gender VARCHAR(20),
    nationality VARCHAR(50),
    address TEXT,
    guardian_name VARCHAR(255),
    guardian_phone VARCHAR(20),
    user_id INT
);

CREATE TABLE IF NOT EXISTS faculty_details (
    faculty_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    full_name VARCHAR(255) NOT NULL,
    designation VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    gender VARCHAR(20),
    joining_date DATE,
    dept_code VARCHAR(50),
    address TEXT,
    profile_picture LONGBLOB
);

-- 4. Academic & Operations
CREATE TABLE IF NOT EXISTS timetable (
    id INT AUTO_INCREMENT PRIMARY KEY,
    day VARCHAR(20) NOT NULL,
    subject VARCHAR(50),
    faculty VARCHAR(50),
    start_time VARCHAR(20),
    end_time VARCHAR(20),
    room VARCHAR(50),
    dept_code VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS student_grades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    subject_code VARCHAR(50) NOT NULL,
    grade VARCHAR(10),
    semester INT,
    academic_year VARCHAR(20),
    UNIQUE KEY unique_grade (student_id, subject_code)
);

-- 5. Attendance System
CREATE TABLE IF NOT EXISTS attendance_session (
    session_id VARCHAR(50) PRIMARY KEY,
    class_code VARCHAR(50),
    class_name VARCHAR(255),
    date DATE,
    start_time TIME,
    end_time TIME,
    validity_minutes INT,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reg_no VARCHAR(50),
    name VARCHAR(255),
    date DATE,
    time VARCHAR(20),
    latitude DOUBLE,
    longitude DOUBLE,
    method VARCHAR(50),
    session_id VARCHAR(50)
);

-- 6. AI Chatbot
CREATE TABLE IF NOT EXISTS chatbot_knowledge (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(100)
);


-- Admin user
INSERT INTO users (username, password, role, is_active) VALUES 
('admin', '$2a$10$b986/ruODsCMk5PSUomBd.Z2ZVEpCrK5fsD//7I74HcsSaaIHf3dy', 'ADMIN', 'Y');

INSERT IGNORE INTO chatbot_knowledge (question, answer, category) VALUES 
('What is SmartCampus?', 'SmartCampus is an AI-powered campus management system that helps students, faculty, and administrators manage academic activities efficiently.', 'General'),
('How do I reset my password?', 'You can reset your password by contacting the system administrator or using the forgot password link on the login page.', 'Support'),
('Where can I find my timetable?', 'You can find your timetable in the student dashboard under the "My Timetable" section.', 'Student'),
('How do I mark attendance?', 'You can mark attendance using the mobile app or by scanning the QR code provided by your faculty during class.', 'Attendance'),
('Where can I view my grades?', 'Your grades are available in the student dashboard under the "My Results" section.', 'Academic');
