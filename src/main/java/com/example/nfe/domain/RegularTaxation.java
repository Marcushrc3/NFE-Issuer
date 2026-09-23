package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Core fields of the {@code gTribRegular} group of the RTC block: the
 * taxation as it would be if the resolutive/suspensive condition were not
 * met.
 * <p>
 * Maps {@code CSTReg}, {@code cClassTribReg}, {@code pAliqEfetRegIBSUF},
 * {@code vTribRegIBSUF}, {@code pAliqEfetRegIBSMun}, {@code vTribRegIBSMun},
 * {@code pAliqEfetRegCBS} and {@code vTribRegCBS}. These codes are
 * independent from the main CST/cClassTrib of IBSCBS. Codes are plain
 * strings; no enums. Data only: no validation, no calculations and no fiscal
 * rules.
 */
public record RegularTaxation(
        String cst,
        String cClassTrib,
        BigDecimal ibsUfEffectiveRate,
        BigDecimal ibsUfTaxAmount,
        BigDecimal ibsMunicipalityEffectiveRate,
        BigDecimal ibsMunicipalityTaxAmount,
        BigDecimal cbsEffectiveRate,
        BigDecimal cbsTaxAmount) {
}
