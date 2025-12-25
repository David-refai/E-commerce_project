package org.example.ecommerce_project.services.report;

import org.example.ecommerce_project.dto.TopProductRow;
import org.example.ecommerce_project.entity.enums.OrderStatus;
import org.example.ecommerce_project.reports.LowStockRow;
import org.example.ecommerce_project.reports.TopProductRow;
import org.example.ecommerce_project.repository.ReportRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepo reportRepo;

    public ReportService(ReportRepo reportRepo) {
        this.reportRepo = reportRepo;
    }

    public List<TopProductRow> topProducts(int topN, LocalDate from, LocalDate toExclusive) {
        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = toExclusive.atStartOfDay(ZoneId.systemDefault()).toInstant();

        return reportRepo.findTopProducts(
                OrderStatus.PAID,
                fromTs,
                toTs,
                PageRequest.of(0, topN)
        );
    }

    public List<LowStockRow> lowStock(int threshold) {
        return inventoryRepo.findLowStock(threshold);
    }

    public BigDecimal revenueBetween(LocalDate from, LocalDate toExclusive) {
        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = toExclusive.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return orderRepo.revenueBetween(OrderStatus.PAID, fromTs, toTs);
    }
}
