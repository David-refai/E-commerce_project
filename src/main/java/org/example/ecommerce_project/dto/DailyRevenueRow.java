package org.example.ecommerce_project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRevenueRow(LocalDate day, BigDecimal revenue) {}
