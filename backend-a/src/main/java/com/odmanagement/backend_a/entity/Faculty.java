package com.odmanagement.backend_a.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faculties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String regNo;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String department;

    @Column(name = "cabin_number")
    private String cabinNumber;

    @Column(nullable = false)
    private String designation = "Assistant Professor";

    @Column(nullable = false)
    private String role; // HOD, FACULTY, MENTOR
}
