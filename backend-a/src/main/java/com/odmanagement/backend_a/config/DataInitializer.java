package com.odmanagement.backend_a.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.odmanagement.backend_a.entity.Faculty;
import com.odmanagement.backend_a.entity.Hod;
import com.odmanagement.backend_a.entity.Student;
import com.odmanagement.backend_a.entity.User;
import com.odmanagement.backend_a.repository.FacultyRepository;
import com.odmanagement.backend_a.repository.HodRepository;
import com.odmanagement.backend_a.repository.UserRepository;
import com.odmanagement.backend_a.repository.od.StudentRepository;

import com.odmanagement.backend_a.entity.Mentor;
import com.odmanagement.backend_a.repository.MentorRepository;

@Configuration
public class DataInitializer {

        @Bean
        CommandLineRunner initData(HodRepository hodRepository,
                        FacultyRepository facultyRepository,
                        StudentRepository studentRepository,
                        UserRepository userRepository,
                        MentorRepository mentorRepository,
                        com.odmanagement.backend_a.repository.od.OdRepository odRepository,
                        PasswordEncoder passwordEncoder) {
                return args -> {
                        // 1. Initialize HODs
                        // AIML HOD
                        createHodAndFaculty(hodRepository, facultyRepository, userRepository, mentorRepository,
                                        passwordEncoder,
                                        "Balaji Ganesh R", "FET-22", "si0221@srmist.edu.in", "AIML", "117");

                        // Maths HOD (New)
                        createHodAndFaculty(hodRepository, facultyRepository, userRepository, mentorRepository,
                                        passwordEncoder,
                                        "Shivaji", "FET-04", "mahasanthosh197@gmail.com", "Maths", "219");

                        // 2. Initialize Faculties
                        // AIML Faculties
                        createFaculty(facultyRepository, userRepository, mentorRepository, passwordEncoder,
                                        "Shri Sudarsana", "FET-01",
                                        "shrisudarsana2005@gmail.com", "AIML", "117");
                        createFaculty(facultyRepository, userRepository, mentorRepository, passwordEncoder, "Pramodh",
                                        "FET-02",
                                        "pramodhvijay6@gmail.com", "AIML", "117");

                        // Maths Faculty
                        createFaculty(facultyRepository, userRepository, mentorRepository, passwordEncoder, "Ashwanth",
                                        "FET-03",
                                        "aswaami09@gmail.com", "Maths", "219");

                        // 3. Initialize Mentors (As Faculty with role MENTOR)
                        saveToFacultyTable(facultyRepository, userRepository, mentorRepository, passwordEncoder,
                                        "Balaji N S", "FET-37",
                                        "nive4115@gmail.com", "AIML", "120",
                                        "MENTOR");
                        saveToFacultyTable(facultyRepository, userRepository, mentorRepository, passwordEncoder,
                                        "Modassir Khan",
                                        "FET-38",
                                        "santhoshr0719@gmail.com", "AIML", "120",
                                        "MENTOR");

                        // 4. Initialize Students with String DOB (dd-MM-yyyy)
                        // ONLY create if they don't exist to avoid overwriting manual changes
                        createStudent(studentRepository, userRepository, passwordEncoder, "RA2311003010001",
                                        "Mahasanthosh",
                                        "19-07-2005", "mahasanthosh@srmist.edu.in", "CSE-AIML", "3rd", "A", "6");

                        createStudent(studentRepository, userRepository, passwordEncoder, "RA2311026050194",
                                        "Student User",
                                        "01-01-2005", "student@srmist.edu.in", "CSE-AIML", "3rd", "A", "6");

                        // 5. Update Existing Students (Migration for manually added records and DOB
                        // fix)
                        updateExistingStudents(studentRepository);

                        // 6. Migrate OD Approver Tracking (New)
                        migrateExistingOds(odRepository);
                };
        }

        private void migrateExistingOds(com.odmanagement.backend_a.repository.od.OdRepository odRepository) {
                odRepository.findAll().forEach(od -> {
                        boolean updated = false;
                        if (od.getMentorRegNo() == null && od.getApproverRegNo() != null) {
                                // If status is MENTOR_APPROVED, the approverRegNo is the Mentor
                                if ("MENTOR_APPROVED".equals(od.getStatus())) {
                                        od.setMentorRegNo(od.getApproverRegNo());
                                        updated = true;
                                }
                                // If status is APPROVED, it might have been approved by HOD,
                                // but we know for CSE-AIML Section A, it's Balaji N S
                                else if ("APPROVED".equals(od.getStatus())) {
                                        // For this test setup, we assume Balaji N S was the mentor
                                        od.setMentorRegNo("Balaji N S");
                                        od.setHodRegNo(od.getApproverRegNo());
                                        updated = true;
                                }
                                // If REJECTED, handle similarly
                                else if ("REJECTED".equals(od.getStatus())) {
                                        if (od.getApproverRegNo() != null && od.getApproverRegNo().contains("HOD")) {
                                                od.setHodRegNo(od.getApproverRegNo());
                                                od.setMentorRegNo("Balaji N S"); // Best guess for CSE-AIML A
                                        } else {
                                                od.setMentorRegNo(od.getApproverRegNo());
                                        }
                                        updated = true;
                                }
                        }
                        if (updated) {
                                odRepository.save(od);
                                System.out.println("✅ Migrated tracking for OD " + od.getOdId());
                        }
                });
        }

        private void updateExistingStudents(StudentRepository studentRepository) {
                studentRepository.findAll().forEach(student -> {
                        boolean updated = false;

                        // Check missing fields (Safe Backfill)
                        if (student.getDepartment() == null || student.getDepartment().isEmpty()) {
                                student.setDepartment("CSE-AIML");
                                updated = true;
                        }
                        if (student.getYear() == null || student.getYear().isEmpty()) {
                                student.setYear("3rd");
                                updated = true;
                        }
                        if (student.getSection() == null || student.getSection().isEmpty()) {
                                student.setSection("A");
                                updated = true;
                        }
                        if (student.getSemester() == null || student.getSemester().isEmpty()) {
                                student.setSemester("6");
                                updated = true;
                        }

                        // Fix corrupted DOB
                        String dob = student.getDob();
                        if (dob != null && (dob.contains("+") || dob.startsWith("00"))) {
                                System.out.println("⚠️ Found corrupted DOB for " + student.getRegNo() + ": " + dob);
                                student.setDob("01-01-2005");
                                updated = true;
                        }

                        if (updated) {
                                studentRepository.save(student);
                                System.out.println("✅ Backfilled/Fixed student details for: " + student.getRegNo());
                        }
                });
        }

        private void createHodAndFaculty(HodRepository hodRepository, FacultyRepository facultyRepository,
                        UserRepository userRepository, MentorRepository mentorRepository,
                        PasswordEncoder passwordEncoder,
                        String name, String regNo, String email, String dept, String cabin) {

                if (hodRepository.findByRegNo(regNo).isEmpty()) {
                        Hod hod = new Hod();
                        hod.setName(name);
                        hod.setRegNo(regNo);
                        hod.setEmail(email);
                        hod.setDepartment(dept);
                        hodRepository.save(hod);
                        System.out.println("✅ HOD initialized: " + name);
                }

                // 2. Update/Create Faculty Table (Combined)
                saveToFacultyTable(facultyRepository, userRepository, mentorRepository, passwordEncoder, name, regNo,
                                email, dept, cabin,
                                "HOD");
        }

        private void createFaculty(FacultyRepository facultyRepository, UserRepository userRepository,
                        MentorRepository mentorRepository,
                        PasswordEncoder passwordEncoder,
                        String name, String regNo, String email, String dept, String cabin) {
                saveToFacultyTable(facultyRepository, userRepository, mentorRepository, passwordEncoder, name, regNo,
                                email, dept, cabin,
                                "FACULTY");
        }

        private void createStudent(StudentRepository studentRepository, UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        String regNo, String name, String dob, String email, String dept, String year, String section,
                        String semester) {

                if (!studentRepository.existsById(regNo)) {
                        Student student = new Student();
                        student.setRegNo(regNo);
                        student.setStudentName(name);
                        student.setDob(dob); // Store as String dd-MM-yyyy
                        student.setStudentEmail(email);
                        student.setDepartment(dept);
                        student.setYear(year);
                        student.setSection(section);
                        student.setSemester(semester);
                        student.setActive(true);
                        studentRepository.save(student);
                        System.out.println("✅ Student initialized: " + name + " (Pass: " + dob + ")");
                }

                // Create User Login if missing
                createUser(userRepository, passwordEncoder, regNo, dob, "STUDENT");
        }

        private void saveToFacultyTable(FacultyRepository facultyRepository, UserRepository userRepository,
                        MentorRepository mentorRepository,
                        PasswordEncoder passwordEncoder,
                        String name, String regNo, String email, String dept, String cabin, String role) {

                Faculty faculty = facultyRepository.findByRegNo(regNo).orElse(new Faculty());

                // Update fields regardless of whether it's new or existing
                faculty.setName(name);
                faculty.setRegNo(regNo);
                faculty.setEmail(email);
                faculty.setDepartment(dept);
                faculty.setCabinNumber(cabin);
                faculty.setDesignation("Assistant Professor");
                faculty.setRole(role);
                facultyRepository.save(faculty);
                System.out.println("✅ Faculty/User upserted: " + name);

                // Fix: Also ensure Mentor table is populated if role is MENTOR
                if ("MENTOR".equalsIgnoreCase(role)) {
                        Mentor mentor = mentorRepository.findByRegNo(regNo).orElse(new Mentor());
                        mentor.setRegNo(regNo);
                        mentor.setName(name);
                        mentor.setEmail(email);
                        mentor.setDepartment(dept);
                        if (mentor.getSection() == null)
                                mentor.setSection("A");
                        if (mentor.getPriority() == null)
                                mentor.setPriority(1);
                        mentorRepository.save(mentor);
                        System.out.println("✅ Mentor upserted in MENTOR table: " + name);
                }

                // Create User Login
                createUser(userRepository, passwordEncoder, name, regNo, role);
        }

        private void createUser(UserRepository userRepository, PasswordEncoder passwordEncoder, String username,
                        String password, String role) {
                if (!userRepository.findByUsername(username).isPresent()) {
                        User user = new User();
                        user.setUsername(username);
                        user.setPasswordHash(passwordEncoder.encode(password));
                        user.setRole(role);
                        user.setActive(true);
                        user.setCreatedAt(LocalDateTime.now());
                        userRepository.save(user);
                }
        }
}
