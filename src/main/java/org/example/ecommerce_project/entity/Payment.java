package org.example.ecommerce_project.entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.Order;

import java.time.Instant;

@Entity
@Table(name = "payment")
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}

