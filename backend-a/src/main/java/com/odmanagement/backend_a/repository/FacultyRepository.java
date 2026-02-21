package com.odmanagement.backend_a.repository;

import com.odmanagement.backend_a.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByRegNo(String regNo);

    Optional<Faculty> findByEmail(String email);

    Optional<Faculty> findByName(String name);

    List<Faculty> findByRole(String role);

    Optional<Faculty> findByRoleAndDepartment(String role, String department);

    List<Faculty> findByNameContainingIgnoreCaseOrRegNoContainingIgnoreCase(String name, String regNo);
}
