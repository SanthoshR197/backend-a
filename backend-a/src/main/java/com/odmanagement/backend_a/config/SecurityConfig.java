package com.odmanagement.backend_a.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.odmanagement.backend_a.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ Enable CORS
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth

                                                // Public
                                                .requestMatchers(
                                                                "/auth/**",
                                                                "/api/od/verify/**", // ✅ Mentor Email Links
                                                                "/api/faculty/leave/verify/**", // ✅ HOD Email Links
                                                                "/uploads/**", // ✅ Public access to uploaded files
                                                                "/api/faculty/search", // ✅ Allow Search
                                                                "/api/student/search", // ✅ Allow Search
                                                                "/api/student/demographics" // ✅ Allow Demographics
                                                                                            // Analysis
                                                ).permitAll()

                                                // Mentor APIs
                                                .requestMatchers(
                                                                "/api/od/pending",
                                                                "/api/od/history/mentor", // ✅ Added specific mentor
                                                                                          // history
                                                                "/api/od/approve/**",
                                                                "/api/od/reject/**")
                                                .hasAnyRole("MENTOR", "HOD")

                                                // Student APIs
                                                .requestMatchers(
                                                                "/api/od/apply",
                                                                "/api/od/upload",
                                                                "/api/od/history/**",
                                                                "/api/faculty/search") // ✅ Allow Students to search
                                                                                       // Faculty
                                                .hasRole("STUDENT")

                                                // Faculty APIs
                                                .requestMatchers(
                                                                "/api/faculty/leave/apply",
                                                                "/api/faculty/leave/history",
                                                                "/api/student/search") // ✅ Allow Faculty to search
                                                                                       // Students
                                                .hasAnyRole("FACULTY", "HOD")

                                                // HOD APIs
                                                .requestMatchers(
                                                                "/api/faculty/leave/pending",
                                                                "/api/faculty/leave/approve/**",
                                                                "/api/faculty/leave/reject/**",
                                                                "/api/hod/stats",
                                                                "/api/od/pending-hod")
                                                .hasRole("HOD")

                                                // Everything else
                                                .anyRequest().authenticated())
                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
                org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
                configuration.setAllowedOrigins(java.util.List.of("http://localhost:5173")); // ✅ Allow Frontend
                configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(java.util.List.of("*"));
                configuration.setAllowCredentials(true);

                org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }
}
