package org.example.ecommerce_project.services;

import org.example.ecommerce_project.dto.LowStockRow;
import org.example.ecommerce_project.dto.TopProductRow;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.report.ReportRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepo reportRepository;

    public ReportService( ReportRepo reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<TopProductRow> topProducts(int topN, LocalDate from, LocalDate toExclusive) {
        if (topN <= 0) topN = 5;
        validateRange(from, toExclusive);

        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = toExclusive.atStartOfDay(ZoneId.systemDefault()).toInstant();

        return reportRepository.topProducts(fromTs, toTs, topN);
    }

    public List<LowStockRow> lowStock(int threshold) {
        if (threshold < 0) throw AppException.validation("threshold must be >= 0");
        return reportRepository.lowStock(threshold);
    }

    public BigDecimal revenueBetween(LocalDate from, LocalDate toExclusive) {
        validateRange(from, toExclusive);

        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = toExclusive.atStartOfDay(ZoneId.systemDefault()).toInstant();

        BigDecimal revenue = reportRepository.revenueBetween(fromTs, toTs);
        return revenue == null ? BigDecimal.ZERO : revenue;
    }

    private void validateRange(LocalDate from, LocalDate toExclusive) {
        if (from == null || toExclusive == null) {
            throw AppException.businessRule("from/to dates are required");
        }
        if (!toExclusive.isAfter(from)) {
            throw AppException.businessRule("'to' must be after 'from'");
        }
    }
}
