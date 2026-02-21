package com.odmanagement.backend_a.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "hods")
public class Hod {

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

    // Default constructor
    public Hod() {
    }

    public Hod(String name, String regNo, String email, String department) {
        this.name = name;
        this.regNo = regNo;
        this.email = email;
        this.department = department;
    }
}
