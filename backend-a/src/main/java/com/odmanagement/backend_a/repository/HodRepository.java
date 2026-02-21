package com.odmanagement.backend_a.repository;

import com.odmanagement.backend_a.entity.Hod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HodRepository extends JpaRepository<Hod, Long> {
    Optional<Hod> findByRegNo(String regNo);

    Optional<Hod> findByEmail(String email);

    Optional<Hod> findByDepartment(String department);

    Optional<Hod> findFirstByDepartment(String department);
}
