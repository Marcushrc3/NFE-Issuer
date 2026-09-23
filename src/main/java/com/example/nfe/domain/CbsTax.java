package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Fields of the {@code gCBS} group of the RTC block.
 * <p>
 * Maps {@code pCBS} to {@code rate} and {@code vCBS} to {@code amount}, with
 * the conditional groups {@code gDif}, {@code gDevTrib} and {@code gRed} in
 * between. The CST and the base (vBC) live at {@code IBSCBS}/{@code gIBSCBS}
 * level and are not repeated here. {@code gALCZFMCBS} (ALC/ZFM) is
 * deliberately unmodelled for now. All conditional groups may be null. Data
 * only: no validation, no calculations and no fiscal rules.
 */
public record CbsTax(
        BigDecimal rate,
        RtcDeferral deferral,
        RtcDevolution devolution,
        RtcReduction reduction,
        BigDecimal amount) {
}
