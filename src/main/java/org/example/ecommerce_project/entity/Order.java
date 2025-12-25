package org.example.ecommerce_project.entity;

import jakarta.persistence.*;
import org.example.ecommerce_project.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status = OrderStatus.NEW;

    // Order total (calculated from items)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        recalcTotal();
    }

    @PreUpdate
    void preUpdate() {
        recalcTotal();
    }

    // Recalculate total from items
    public void recalcTotal() {
        this.total = items.stream()
                .map(OrderItem::getLineTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    // Add item and sync relation
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    // Remove item and sync relation
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // getters/setters (no setTotal, no setCreatedAt)
    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        if (this.payment != null) {
            this.payment.setOrder(null);
        }
        this.payment = payment;
        if (payment != null) {
            payment.setOrder(this);
        }
    }

}
