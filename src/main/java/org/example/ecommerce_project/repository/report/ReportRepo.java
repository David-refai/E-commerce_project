package org.example.ecommerce_project.repository;

import org.example.ecommerce_project.dto.DailyRevenueRow;
import org.example.ecommerce_project.dto.LowStockRow;
import org.example.ecommerce_project.dto.TopProductRow;
import org.example.ecommerce_project.entity.Inventory;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


@org.springframework.stereotype.Repository
public interface ReportRepo{
    List<TopProductRow> topProducts(Instant from, Instant toExclusive, int limit);
    List<LowStockRow> lowStock(int threshold);
    BigDecimal revenueBetween(Instant from, Instant toExclusive);
}
