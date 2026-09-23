package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Core fields of the {@code gIBSCBS} group of the RTC block.
 * <p>
 * Maps {@code vBC} to {@code taxBase} and {@code vIBS} to {@code ibsAmount}.
 * The IBS value is explicit in the layout and is never calculated from the
 * UF + municipality amounts. All nested groups may be null. Data only: no
 * validation, no calculations and no fiscal rules.
 */
public record IbsCbsTaxation(
        BigDecimal taxBase,
        IbsUfTax ibsUf,
        IbsMunicipalityTax ibsMunicipality,
        BigDecimal ibsAmount,
        CbsTax cbs,
        RegularTaxation regularTaxation) {
}
