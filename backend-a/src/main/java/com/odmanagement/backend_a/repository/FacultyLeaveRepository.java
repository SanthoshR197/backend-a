package com.odmanagement.backend_a.repository;

import com.odmanagement.backend_a.entity.FacultyLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyLeaveRepository extends JpaRepository<FacultyLeave, Long> {

    List<FacultyLeave> findByFaculty_RegNo(String regNo);

    List<FacultyLeave> findByStatus(String status);

    long countByStatusAndFaculty_Department(String status, String department);

    // Count CLs for a specific month/year for a faculty
    @Query("SELECT COUNT(f) FROM FacultyLeave f WHERE f.faculty.regNo = :regNo AND f.leaveType = 'CL' AND MONTH(f.fromDate) = :month AND YEAR(f.fromDate) = :year AND f.status != 'REJECTED'")
    long countClsInMonth(@org.springframework.data.repository.query.Param("regNo") String regNo,
            @org.springframework.data.repository.query.Param("month") int month,
            @org.springframework.data.repository.query.Param("year") int year);
}
