package org.example.ecommerce_project.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.Payment;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.example.ecommerce_project.entity.enums.PaymentMethod;
import org.example.ecommerce_project.entity.enums.PaymentStatus;
import org.example.ecommerce_project.repository.OrderRepo;
import org.example.ecommerce_project.repository.PaymentRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final OrderRepo orderRepo;

    public PaymentService(PaymentRepo paymentRepo, OrderRepo orderRepo) {
        this.paymentRepo = paymentRepo;
        this.orderRepo = orderRepo;
    }

    @Transactional
    public Payment createPayment(Long orderId, PaymentMethod method) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method must not be null");
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        paymentRepo.findByOrderId(orderId).ifPresent(existing -> {
            throw new IllegalArgumentException("Payment already exists for orderId: " + orderId);
        });

        Payment payment = new Payment();
        // Payment har (enligt din repo-metod findByOrderId) en koppling till Order.
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);

        Payment saved = paymentRepo.save(payment);

        // Koppla tillbaka till order (bra för bi-directional mapping)
        order.setPayment(saved);
        orderRepo.save(order);

        return saved;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return paymentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        return paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for orderId: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepo.findAll();
    }

    @Transactional
    public Payment approvePayment(Long orderId) {
        Payment payment = getPaymentByOrderId(orderId);

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            return payment; // redan godkänd
        }

        payment.setStatus(PaymentStatus.APPROVED);
        Payment saved = paymentRepo.save(payment);

        // När betalning godkänns: uppdatera order-status -> PAID
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        order.setStatus(OrderStatus.PAID);
        orderRepo.save(order);

        return saved;
    }

    @Transactional
    public Payment declinePayment(Long orderId) {
        Payment payment = getPaymentByOrderId(orderId);

        if (payment.getStatus() == PaymentStatus.DECLINED) {
            return payment; // redan nekad
        }

        payment.setStatus(PaymentStatus.DECLINED);
        return paymentRepo.save(payment);
    }

    @Transactional
    public void deletePayment(Long id) {
        Payment existing = getPaymentById(id);
        paymentRepo.delete(existing);
    }
}
