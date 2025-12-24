package org.example.ecommerce_project.services;

import org.example.ecommerce_project.entity.Inventory;
import org.example.ecommerce_project.repository.ReportRepo;
import org.example.ecommerce_project.repository.RevenuePerDayView;
import org.example.ecommerce_project.repository.TopSellingProductView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepo reportRepo;

    public ReportService(ReportRepo reportRepo) {
        this.reportRepo = reportRepo;
    }

    @Transactional(readOnly = true)
    public List<TopSellingProductView> top5BestSellingProducts() {
        return reportRepo.top5BestSellingProducts();
    }

    @Transactional(readOnly = true)
    public List<Inventory> lowStockProducts(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold must be >= 0");
        }
        return reportRepo.lowStockProducts(threshold);
    }

    @Transactional(readOnly = true)
    public List<RevenuePerDayView> revenuePerDay(LocalDate startDateInclusive, LocalDate endDateInclusive) {
        if (startDateInclusive == null || endDateInclusive == null) {
            throw new IllegalArgumentException("startDateInclusive and endDateInclusive are required");
        }
        if (endDateInclusive.isBefore(startDateInclusive)) {
            throw new IllegalArgumentException("endDateInclusive must be on/after startDateInclusive");
        }

        Instant fromInclusive = startDateInclusive.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toExclusive = endDateInclusive.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return reportRepo.revenuePerDay(fromInclusive, toExclusive);
    }
}
