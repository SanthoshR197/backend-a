package com.odmanagement.backend_a.service;

import com.odmanagement.backend_a.entity.Mentor;
import com.odmanagement.backend_a.entity.Student;
import com.odmanagement.backend_a.repository.FacultyRepository;
import com.odmanagement.backend_a.repository.MentorRepository;
import com.odmanagement.backend_a.repository.od.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.odmanagement.backend_a.dto.LoginRequest;
import com.odmanagement.backend_a.dto.LoginResponse;
import com.odmanagement.backend_a.entity.User;
import com.odmanagement.backend_a.repository.UserRepository;
import com.odmanagement.backend_a.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
            StudentRepository studentRepository,
            MentorRepository mentorRepository,
            FacultyRepository facultyRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.mentorRepository = mentorRepository;
        this.facultyRepository = facultyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole());

        Object userDetails = null;
        if ("STUDENT".equals(user.getRole())) {
            userDetails = studentRepository.findById(user.getUsername()).orElse(null);
        } else if ("MENTOR".equals(user.getRole())) {
            // Existing logic for Mentors (Username = Name)
            String username = user.getUsername();
            userDetails = mentorRepository.findAll().stream()
                    .filter(m -> m.getName().equals(username))
                    .findFirst()
                    .orElse(null);
        } else if ("HOD".equals(user.getRole()) || "FACULTY".equals(user.getRole())) {
            // New logic for HOD/FACULTY (Username = Name)
            userDetails = facultyRepository.findByName(user.getUsername()).orElse(null);
        }

        return new LoginResponse(token, user.getRole(), userDetails);
    }
}
