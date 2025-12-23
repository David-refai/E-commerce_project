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
    @JoinColumn(
            name = "order_id",
            nullable = false,
            unique = true
    )
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

    @PreUpdate
    void preUpdate() { this.ts = Instant.now(); }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getTs() {
        return ts;
    }

    public void setTs(Instant ts) {
        this.ts = ts;
    }
}

