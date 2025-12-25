package org.example.ecommerce_project.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RevenuePerDayView {
    LocalDate getDay();

    BigDecimal getRevenue();
}
