DROP DATABASE IF EXISTS auth_db;
CREATE DATABASE auth_db;
USE auth_db;

CREATE TABLE users_auth (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    role ENUM('ADMIN','INSTRUCTOR','STUDENT') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE','BLOCKED') DEFAULT 'ACTIVE',
    last_login DATETIME NULL,
    failed_attempts INT DEFAULT 0,
    last_failed DATETIME NULL
);

INSERT INTO users_auth (username, role, password_hash, status) VALUES
('admin1', 'ADMIN', '$2a$10$BpczG3oivJFrtqCbyqZXh.S5eLMfewuSS4FHWct8OeQSG8H5naKeC', 'ACTIVE'),
('inst1', 'INSTRUCTOR', '$2a$10$C1qXgT5pav1d7O.Kim.E0O.p6FwRNR22Re3Wr7R.pyaqfFqQ.dCmy', 'ACTIVE'),
('stu1', 'STUDENT', '$2a$10$pMrTaYsN/LXTMZjS.hsIfuMVeh8uTFTXmTBX5wWbJzP.LKv.A4gsm', 'ACTIVE'),
('stu2', 'STUDENT', '$2a$10$Wed.zo1XvagI1md9/pkQzeETXFr0dS9HKTOwpHxwtTU87OvJlue6.', 'ACTIVE');
