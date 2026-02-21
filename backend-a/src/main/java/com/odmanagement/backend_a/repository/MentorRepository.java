package com.odmanagement.backend_a.repository;

import com.odmanagement.backend_a.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Long> {
    Optional<Mentor> findByRegNo(String regNo);

    Optional<Mentor> findByPriority(Integer priority);

    Optional<Mentor> findFirstByPriority(Integer priority);
}
