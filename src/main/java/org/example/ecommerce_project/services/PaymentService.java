package org.example.ecommerce_project.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.ecommerce_project.entity.Order;
import org.example.ecommerce_project.entity.Payment;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.example.ecommerce_project.entity.enums.PaymentMethod;
import org.example.ecommerce_project.entity.enums.PaymentStatus;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.OrderRepo;
import org.example.ecommerce_project.repository.PaymentRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    // Sannolikhet att betalningen godkänns (90%)
    private static final double APPROVE_PROBABILITY = 0.9;

    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;
    private final InventoryService inventoryService;

    public PaymentService(OrderRepo orderRepo, PaymentRepo paymentRepo, InventoryService inventoryService) {
        this.orderRepo = orderRepo;
        this.paymentRepo = paymentRepo;
        this.inventoryService = inventoryService;

    }

    /**
     * Behandlar betalning för en order:
     * - Validerar orderstatus
     * - Skapar betalning
     * - Sätter order till PAID vid lyckad betalning
     * - Återställer lager vid nekad betalning
     * @param orderId orderns ID
     * @param method vald betalningsmetod
     * @return sparad Payment
     */
    @Transactional
    public Payment processPayment(Long orderId, PaymentMethod method) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            throw AppException.validation("Order is already PAID");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw AppException.validation("Cannot pay a CANCELLED order");
        }

        paymentRepo.findByOrderId(orderId).ifPresent(p -> {
            throw AppException.businessRule("Payment already exists for orderId: " + orderId);
        });

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(method);

        boolean approved = java.util.concurrent.ThreadLocalRandom.current().nextDouble() < APPROVE_PROBABILITY;

        if (approved) {
            payment.setStatus(PaymentStatus.APPROVED);
            order.setStatus(OrderStatus.PAID);
            orderRepo.save(order);
        } else {
            payment.setStatus(PaymentStatus.DECLINED);

            // Lagret minskas vid skapande av order, därför måste det återställas vid DECLINED
            for (var item : order.getItems()) {
                inventoryService.releaseStock(item.getProduct().getId(), item.getQty());
            }
            orderRepo.save(order);
        }

        Payment saved = paymentRepo.save(payment);

        order.setPayment(saved);
        orderRepo.save(order);

        return saved;
    }

}
