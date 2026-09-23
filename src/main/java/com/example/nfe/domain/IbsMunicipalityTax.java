package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Fields of the {@code gIBSMun} group (IBS of the municipality) of the RTC
 * block.
 * <p>
 * Maps {@code pIBSMun} to {@code rate} and {@code vIBSMun} to {@code amount},
 * with the conditional groups {@code gDif}, {@code gDevTrib} and
 * {@code gRed} in between. The base (vBC) is shared at {@code gIBSCBS} level
 * and is not repeated here. All conditional groups may be null. Data only:
 * no validation, no calculations and no fiscal rules.
 */
public record IbsMunicipalityTax(
        BigDecimal rate,
        RtcDeferral deferral,
        RtcDevolution devolution,
        RtcReduction reduction,
        BigDecimal amount) {
}
