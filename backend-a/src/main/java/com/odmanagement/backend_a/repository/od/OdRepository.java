package com.odmanagement.backend_a.repository.od;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.odmanagement.backend_a.entity.od.OdRequest;

@Repository
public interface OdRepository extends JpaRepository<OdRequest, Long> {
    List<OdRequest> findByRegNoOrderByOdIdDesc(String regNo);

    List<OdRequest> findByApproverRegNoOrderByOdIdDesc(String approverRegNo);

    List<OdRequest> findByMentorRegNoOrderByOdIdDesc(String mentorRegNo);

    List<OdRequest> findByHodRegNoOrderByOdIdDesc(String hodRegNo);

    List<OdRequest> findByStatusOrderByOdIdDesc(String status);

    List<OdRequest> findByStatusInOrderByOdIdDesc(List<String> statuses);

    long countByStatus(String status);

    // Mentor Stats
    long countByMentorRegNoAndStatus(String mentorRegNo, String status);

    long countByMentorRegNoAndStatusIn(String mentorRegNo, List<String> statuses);

    // HOD Stats
    long countByHodRegNoAndStatus(String hodRegNo, String status);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Student s JOIN OdRequest od ON s.regNo = od.regNo " +
            "WHERE s.department = :dept AND s.year = :year " +
            "AND s.semester = :sem AND s.section = :sec " +
            "AND od.status = 'APPROVED' " +
            "AND :date BETWEEN od.fromDate AND od.toDate")
    java.util.List<com.odmanagement.backend_a.entity.Student> findStudentsWithOdOnDate(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDate date,
            @org.springframework.data.repository.query.Param("dept") String dept,
            @org.springframework.data.repository.query.Param("year") String year,
            @org.springframework.data.repository.query.Param("sem") String sem,
            @org.springframework.data.repository.query.Param("sec") String sec);
}