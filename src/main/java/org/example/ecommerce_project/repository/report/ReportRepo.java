package org.example.ecommerce_project.repository.report;

import org.example.ecommerce_project.dto.LowStockRow;
import org.example.ecommerce_project.dto.TopProductRow;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


@org.springframework.stereotype.Repository
public interface ReportRepo{
    List<TopProductRow> topProducts(Instant from, Instant toExclusive, int limit);
    List<LowStockRow> lowStock(int threshold);
    BigDecimal revenueBetween(Instant from, Instant toExclusive);
}
