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

    public ReportService(ReportRepo reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * Hämtar bäst säljande produkter under ett datumintervall
     * @param topN antal produkter att returnera (default = 5 om <=0)
     * @param from startdatum (inklusivt)
     * @param toExclusive slutdatum (exklusivt)
     * @return lista med toppliste-rader
     */
    public List<TopProductRow> topProducts(int topN, LocalDate from, LocalDate toExclusive) {
        if (topN <= 0) topN = 5;
        validateRange(from, toExclusive);

        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = toExclusive.atStartOfDay(ZoneId.systemDefault()).toInstant();

        return reportRepository.topProducts(fromTs, toTs, topN);
    }

    /**
     * Returnerar produkter med lågt lagersaldo under en given gräns
     * @param threshold gränsvärde för lagersaldo
     */
    public List<LowStockRow> lowStock(int threshold) {
        if (threshold < 0) throw AppException.validation("threshold must be >= 0");
        return reportRepository.lowStock(threshold);
    }

    /**
     * Beräknar totala intäkter mellan två datum
     * Returnerar 0 om inget resultat finns
     * @param from startdatum
     * @param toExclusive slutdatum (exklusivt)
     */
    public BigDecimal revenueBetween(LocalDate from, LocalDate toExclusive) {
        validateRange(from, toExclusive);

        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = toExclusive.atStartOfDay(ZoneId.systemDefault()).toInstant();

        BigDecimal revenue = reportRepository.revenueBetween(fromTs, toTs);
        return revenue == null ? BigDecimal.ZERO : revenue;
    }

    /**
     * Validerar att datumintervall är korrekt och att slutdatum är efter startdatum
     */
    private void validateRange(LocalDate from, LocalDate toExclusive) {
        if (from == null || toExclusive == null) {
            throw AppException.businessRule("from/to dates are required");
        }
        if (!toExclusive.isAfter(from)) {
            throw AppException.businessRule("'to' must be after 'from'");
        }
    }
}
