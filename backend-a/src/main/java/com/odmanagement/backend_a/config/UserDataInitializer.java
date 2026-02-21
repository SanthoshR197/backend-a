package com.odmanagement.backend_a.config;

import com.odmanagement.backend_a.entity.Mentor;
import com.odmanagement.backend_a.entity.Student;
import com.odmanagement.backend_a.entity.User;
import com.odmanagement.backend_a.repository.MentorRepository;
import com.odmanagement.backend_a.repository.od.StudentRepository;
import com.odmanagement.backend_a.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // Changed from BCrypt to generic

import java.util.List;

@Configuration
public class UserDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(UserDataInitializer.class);

    @Bean
    CommandLineRunner syncStudentsToUsers(
            StudentRepository studentRepository,
            UserRepository userRepository,
            MentorRepository mentorRepository,
            com.odmanagement.backend_a.repository.FacultyRepository facultyRepository,
            PasswordEncoder passwordEncoder) { // Using PasswordEncoder interface
        return args -> {

            // 1. Sync Students
            List<Student> students = studentRepository.findAll();

            for (Student student : students) {

                // Update additional fields if missing (already handled in DataInitializer but
                // checks here too)
                boolean updated = false;
                if (student.getDepartment() == null) {
                    student.setDepartment("CSE-AIML");
                    updated = true;
                }
                if (student.getSection() == null) {
                    student.setSection("A");
                    updated = true;
                }
                if (student.getYear() == null) {
                    student.setYear("3rd");
                    updated = true;
                }
                if (student.getSemester() == null) {
                    student.setSemester("6");
                    updated = true;
                }
                if (updated) {
                    studentRepository.save(student);
                }

                // Skip inactive students
                if (student.getActive() != null && !student.getActive()) {
                    continue;
                }

                // Match User to Student
                User user = userRepository.findByUsername(student.getRegNo())
                        .orElse(new User());

                user.setUsername(student.getRegNo());

                // DOB → password (Always update to ensure sync)
                if (student.getDob() != null) {
                    // DOB is now a String, so we use it directly
                    String rawPassword = student.getDob();
                    user.setPasswordHash(passwordEncoder.encode(rawPassword));
                    logger.info("Updated password for {}: {}", student.getRegNo(), rawPassword);
                } else {
                    logger.warn("Student {} has no DOB!", student.getRegNo());
                }

                user.setRole("STUDENT");
                user.setActive(true);

                userRepository.save(user);
            }
            logger.info("✅ Student → User sync completed");

            // 2. Seed Mentors (Legacy/Specific Table Logic)
            List<MentorSeedData> mentorsToSeed = List.of(
                    new MentorSeedData("FET-37", "Balaji N S", "nive4115@gmail.com", 1),
                    new MentorSeedData("FET-38", "Modassir Khan", "santhoshr0719@gmail.com", 2));

            for (MentorSeedData data : mentorsToSeed) {
                Mentor mentor = mentorRepository.findByRegNo(data.regNo).orElse(new Mentor());

                mentor.setRegNo(data.regNo);
                mentor.setName(data.name);
                mentor.setEmail(data.email);
                mentor.setPriority(data.priority);
                mentor.setDepartment("CSE-AIML");
                mentor.setSection("A");

                mentorRepository.save(mentor);
                logger.info("✅ Upserted Mentor: {}", data.name);

                if (!userRepository.existsByUsername(data.name)) {
                    User user = new User();
                    user.setUsername(data.name);
                    user.setPasswordHash(passwordEncoder.encode(data.regNo)); // RegNo as Password
                    user.setRole("MENTOR");
                    user.setActive(true);
                    userRepository.save(user);
                    logger.info("✅ Created User for Mentor: {}", data.name);
                }
            }

            // 3. Sync Faculty Table to Users
            List<com.odmanagement.backend_a.entity.Faculty> faculties = facultyRepository.findAll();
            for (com.odmanagement.backend_a.entity.Faculty faculty : faculties) {

                String username = faculty.getName(); // Username is Name
                if (userRepository.existsByUsername(username)) {
                    continue; // Already exists
                }

                User user = new User();
                user.setUsername(username);
                // Password is RegNo
                user.setPasswordHash(passwordEncoder.encode(faculty.getRegNo()));
                user.setRole(faculty.getRole()); // HOD, FACULTY, MENTOR
                user.setActive(true);
                userRepository.save(user);
                logger.info("✅ Created User for Faculty: {} ({}) - Password: {}", faculty.getName(), faculty.getRole(),
                        faculty.getRegNo());
            }

            // 4. MANUAL OVERRIDE FOR USER REQUEST
            String manualRegNo = "RA2311026050077";
            String manualDob = "19-07-2006"; // Password

            if (!userRepository.existsByUsername(manualRegNo)) {
                User manualUser = new User();
                manualUser.setUsername(manualRegNo);
                manualUser.setPasswordHash(passwordEncoder.encode(manualDob));
                manualUser.setRole("STUDENT");
                manualUser.setActive(true);
                userRepository.save(manualUser);
                logger.info("✅ Manually created requested user: {} with password: {}", manualRegNo, manualDob);

                // Also ensure they exist in Student table for profile fetching
                if (!studentRepository.existsById(manualRegNo)) {
                    Student manualStudent = new Student();
                    manualStudent.setRegNo(manualRegNo);
                    manualStudent.setStudentName("Mahasanthosh"); // Placeholder name
                    manualStudent.setDob("19-07-2006"); // Set String DOB
                    manualStudent.setStudentEmail("santhoshr0719@gmail.com"); // Placeholder
                    manualStudent.setDepartment("CSE-AIML");
                    manualStudent.setSection("A");
                    manualStudent.setYear("3rd");
                    manualStudent.setSemester("6");
                    manualStudent.setActive(true);
                    studentRepository.save(manualStudent);
                    logger.info("✅ Manually created Student entity for profile: {}", manualRegNo);
                }
            } else {
                // If exists, force update password to be sure
                User existingUser = userRepository.findByUsername(manualRegNo).get();
                existingUser.setPasswordHash(passwordEncoder.encode(manualDob));
                userRepository.save(existingUser);
                logger.info("✅ Force updated password for existing manual user: {}", manualRegNo);

                // Ensure student entity exists too
                if (!studentRepository.existsById(manualRegNo)) {
                    Student manualStudent = new Student();
                    manualStudent.setRegNo(manualRegNo);
                    manualStudent.setStudentName("Mahasanthosh");
                    manualStudent.setDob("19-07-2006"); // Set String DOB
                    manualStudent.setDepartment("CSE-AIML");
                    manualStudent.setSection("A");
                    manualStudent.setYear("3rd");
                    manualStudent.setSemester("6");
                    manualStudent.setActive(true);
                    studentRepository.save(manualStudent);
                }
            }

            // FORCE UPDATE FOR BALAJI GANESH R (HOD/Faculty)
            String targetUser = "Balaji Ganesh R";
            String targetPass = "FET-22";

            if (userRepository.existsByUsername(targetUser)) {
                User u = userRepository.findByUsername(targetUser).get();
                u.setPasswordHash(passwordEncoder.encode(targetPass));
                if (u.getRole() == null)
                    u.setRole("HOD");
                userRepository.save(u);
                logger.info("✅ Force updated password for {}: {}", targetUser, targetPass);
            } else {
                User u = new User();
                u.setUsername(targetUser);
                u.setPasswordHash(passwordEncoder.encode(targetPass));
                u.setRole("HOD");
                u.setActive(true);
                userRepository.save(u);
                logger.info("✅ Created manual user {}: {}", targetUser, targetPass);
            }

            // ==============================================================================
            // 5. COMPREHENSIVE DUMMY DATA SEEDING (FOR TESTING ALL ROLES)
            // ==============================================================================

            // A. Seed Dummy Students (RA2311001 to RA2311005)
            for (int i = 1; i <= 5; i++) {
                String dummyRegNo = "RA231100" + i;
                String dummyDob = "01-01-2005"; // Common Password

                if (!userRepository.existsByUsername(dummyRegNo)) {
                    // Create User
                    User u = new User();
                    u.setUsername(dummyRegNo);
                    u.setPasswordHash(passwordEncoder.encode(dummyDob));
                    u.setRole("STUDENT");
                    u.setActive(true);
                    userRepository.save(u);

                    // Create Student Entity (if missing)
                    if (!studentRepository.existsById(dummyRegNo)) {
                        Student s = new Student();
                        s.setRegNo(dummyRegNo);
                        s.setStudentName("Student " + i);
                        s.setDob("01-01-2005"); // Set String DOB
                        s.setStudentEmail("student" + i + "@srmist.edu.in");
                        s.setDepartment("CSE-AIML");
                        s.setSection("A");
                        s.setYear("3rd");
                        s.setSemester("6");
                        s.setActive(true);
                        studentRepository.save(s);
                    }
                    logger.info("✅ Seeded Dummy Student: {}", dummyRegNo);
                }
            }

            // Mentors and HOD seeding logic remains same...
            // (Skipping redundant copy/paste for brevity, but will include needed parts if
            // I replace full file)
            // Wait, I am using write_to_file so I need the FULL content.
            // I will include the rest of the file logic.

            // B. Cleanup Dummy Mentors (FET-M01, FET-M02) - REMOVED SEEDING
            List<String> dummyRegNos = List.of("FET-M01", "FET-M02");
            for (String regNo : dummyRegNos) {
                if (mentorRepository.findByRegNo(regNo).isPresent()) {
                    Mentor m = mentorRepository.findByRegNo(regNo).get();
                    mentorRepository.delete(m);
                    logger.info("🗑️ Deleted Dummy Mentor: {}", m.getName());

                    // Also delete User
                    if (userRepository.existsByUsername(m.getName())) {
                        User u = userRepository.findByUsername(m.getName()).get();
                        userRepository.delete(u);
                        logger.info("🗑️ Deleted User for Dummy Mentor: {}", m.getName());
                    }
                }
            }

            // C. Seed Dummy HOD (FET-HOD)
            if (!userRepository.existsByUsername("HOD CSE")) {
                User u = new User();
                u.setUsername("HOD CSE");
                u.setPasswordHash(passwordEncoder.encode("FET-HOD"));
                u.setRole("HOD");
                u.setActive(true);
                userRepository.save(u);

                if (facultyRepository.findByRegNo("FET-HOD").isEmpty()) {
                    com.odmanagement.backend_a.entity.Faculty f = new com.odmanagement.backend_a.entity.Faculty();
                    f.setRegNo("FET-HOD");
                    f.setName("HOD CSE");
                    f.setEmail("hod.cse@srmist.edu.in");
                    f.setDepartment("CSE");
                    f.setRole("HOD");
                    f.setDesignation("Head of Department");
                    facultyRepository.save(f);
                }
                logger.info("✅ Seeded Dummy HOD: HOD CSE");
            }

        };
    }

    private static class MentorSeedData {
        String regNo;
        String name;
        String email;
        Integer priority;

        public MentorSeedData(String regNo, String name, String email, Integer priority) {
            this.regNo = regNo;
            this.name = name;
            this.email = email;
            this.priority = priority;
        }
    }
}
