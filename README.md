🎓 University ERP System (Java + Swing)

A desktop-based University ERP application built using Java and Swing, designed to manage courses, sections, enrollments, grades, and users with role-based access control.
The system models real university workflows for Students, Instructors, and Admins, with a strong emphasis on data integrity, access rules, and system robustness.

✨ Key Features
👤 Role-Based Access

The system supports three distinct user roles:

Student: course registration/drop, timetable viewing, grade viewing, transcript export

Instructor: manage assigned sections, enter assessment scores, compute final grades, view class statistics

Admin: manage users, courses, sections, instructor assignments, and system-wide settings

Each action is validated against role permissions to prevent unauthorized access.

🛠️ Core Functionalities
Common

Secure login with role-specific dashboards

Clear success/error feedback for all user actions

Searchable and sortable tables for courses and sections

Student

Browse course catalog (code, title, credits, capacity, instructor)

Register/drop sections with seat availability checks

View weekly timetable

View detailed grades and final grades

Download transcript (CSV/PDF)

Instructor

View only assigned sections

Define assessment components and enter scores

Compute final grades using custom weightage

View simple class statistics (averages, distributions)

Optional CSV import/export for grades

Admin

Add students, instructors, and admins

Create and manage courses and sections

Assign instructors to sections

Toggle Maintenance Mode (system-wide read-only mode)

Optional backup/restore support

🧠 System Design
🔐 Authentication & Security

Two-database architecture:

Auth DB: usernames, roles, password hashes

ERP DB: students, instructors, courses, sections, enrollments, grades

Passwords are stored only as secure hashes (no plaintext passwords)

Auth DB and ERP DB are linked using a shared user ID

🧱 Architecture

The application follows a layered design:

UI Layer (ui.*): Swing-based screens and dialogs

Service Layer (service.*): core business logic and rule enforcement

Data Layer (data.*): database access and persistence

Access Layer (access.*): permission checks and maintenance rules

Auth Layer (auth.*): login, session handling, password verification

UI components never interact directly with the database; all actions go through the service layer.

🗄️ Database Design Highlights

Normalized relational schema

Separate tables for users, courses, sections, enrollments, grades, and settings

Constraints to prevent duplicate enrollments

Validation for capacities, deadlines, and role boundaries

Maintenance flag enforced consistently across all write operations

⚙️ Tech Stack

Language: Java

UI: Java Swing

Database: MySQL / MariaDB (via JDBC)

Password Hashing: bcrypt

Exports: CSV / PDF

Architecture: Layered (UI → Service → Data)

▶️ How to Run

Ensure Java (JDK 8+) is installed

Set up the Auth DB and ERP DB using provided schema/seed scripts

Update database connection details in configuration

Run the main application class

Use sample credentials to log in as Student / Instructor / Admin

🧪 Testing & Validation

Acceptance tests for all major user flows

Edge-case handling (duplicate registrations, full sections, permission violations)

Data integrity checks

Maintenance Mode validation

Secure authentication verification

📌 Learning Outcomes

Designed a real-world ERP system with multiple user roles

Applied DBMS concepts: schema design, normalization, constraints, transactions

Implemented secure authentication with separation of concerns

Built a maintainable desktop GUI application

Practiced clean architecture and systematic testing

📎 Notes

This project was developed as part of an Advanced Programming course and simulates real academic administration workflows.
