DROP DATABASE IF EXISTS erp_db;
CREATE DATABASE erp_db;
USE erp_db;

DROP TABLE IF EXISTS grades;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS sections;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS instructors;
DROP TABLE IF EXISTS students;

CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    roll_no VARCHAR(20) UNIQUE,
    program VARCHAR(50),
    year INT,
    FOREIGN KEY (user_id) REFERENCES auth_db.users_auth(user_id)
);

CREATE TABLE instructors (
    instructor_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    department VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES auth_db.users_auth(user_id)
);

CREATE TABLE courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) UNIQUE,
    title VARCHAR(100),
    credits INT DEFAULT 3
);

CREATE TABLE sections (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT,
    instructor_id INT,
    day_time VARCHAR(30),
    room VARCHAR(30),
    capacity INT DEFAULT 40,
    semester VARCHAR(10),
    year INT,
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
);

CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    section_id INT,
    status ENUM('ENROLLED','DROPPED') DEFAULT 'ENROLLED',
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
);

CREATE TABLE grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT,
    component ENUM('quiz','midterm','endsem','FINAL'),
    score DOUBLE,
    final_score DOUBLE,
    final_grade CHAR(2),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS system_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    maintenance ENUM('ON','OFF') DEFAULT 'OFF'
);

INSERT INTO system_settings (maintenance) VALUES ('OFF');

INSERT INTO students (user_id, roll_no, program, year) VALUES
(3, '2024001', 'B.Tech CSE', 2),
(4, '2024002', 'B.Tech CSE', 2);

INSERT INTO instructors (user_id, department) VALUES
(2, 'Computer Science');

INSERT INTO courses (code, title, credits) VALUES
('DS101', 'Data Structures', 4),
('IP102', 'Introduction to Programming', 3),
('DM103', 'Discrete Mathematics', 4);

INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES
(1, 1, 'Mon 10-12', 'A1', 40, 'Spring', 2025),
(2, 1, 'Wed 9-11', 'B2', 35, 'Spring', 2025),
(3, 1, 'Fri 2-4', 'C3', 40, 'Spring', 2025);

INSERT INTO enrollments (student_id, section_id, status)
VALUES (1, 1, 'ENROLLED');
