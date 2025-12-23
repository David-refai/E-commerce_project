package org.example.ecommerce_project.entity;

import jakarta.persistence.*;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.example.ecommerce_project.entity.enums.PaymentMethod;
import org.example.ecommerce_project.entity.enums.PaymentStatus;

import java.time.Instant;

@Entity
@Table(name = "payment")
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "ts", nullable = false)
    private Instant ts;

    @PrePersist
    void prePersist() { this.ts = Instant.now(); }

}

