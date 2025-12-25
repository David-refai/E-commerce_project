package org.example.ecommerce_project.repository.report;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.ecommerce_project.dto.LowStockRow;
import org.example.ecommerce_project.dto.TopProductRow;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
@Primary
public class ReportRepositoryImpl implements ReportRepo {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<TopProductRow> topProducts(Instant from, Instant toExclusive, int limit) {
        return em.createQuery("""
            select new org.example.ecommerce_project.dto.TopProductRow(
                p.id, p.sku, p.name, sum(oi.qty)
            )
            from OrderItem oi
            join oi.order o
            join oi.product p
            where o.status = :paid
              and o.createdAt >= :from
              and o.createdAt <  :to
            group by p.id, p.sku, p.name
            order by sum(oi.qty) desc
        """, TopProductRow.class)
                .setParameter("paid", OrderStatus.PAID)
                .setParameter("from", from)
                .setParameter("to", toExclusive)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<LowStockRow> lowStock(int threshold) {
        return em.createQuery("""
            select new org.example.ecommerce_project.dto.LowStockRow(
                p.id, p.sku, p.name, i.inStock
            )
            from Inventory i
            join i.product p
            where i.inStock < :threshold
              and p.active = true
            order by i.inStock asc, p.name asc
        """, LowStockRow.class)
                .setParameter("threshold", threshold)
                .getResultList();
    }

    @Override
    public BigDecimal revenueBetween(Instant from, Instant toExclusive) {
        BigDecimal res = em.createQuery("""
            select coalesce(sum(o.total), 0)
            from Order o
            where o.status = :paid
              and o.createdAt >= :from
              and o.createdAt <  :to
        """, BigDecimal.class)
                .setParameter("paid", OrderStatus.PAID)
                .setParameter("from", from)
                .setParameter("to", toExclusive)
                .getSingleResult();

        return res == null ? BigDecimal.ZERO : res;
    }
}
