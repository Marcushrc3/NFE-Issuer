package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Structural ST/FCP-ST block of ICMS (substituição tributária).
 * <p>
 * Maps {@code modBCST}, {@code pMVAST}, {@code pRedBCST}, {@code vBCST},
 * {@code pICMSST}, {@code vICMSST}, {@code vBCFCPST}, {@code pFCPST} and
 * {@code vFCPST}. All fields may be null. Data only: no validation, no
 * calculations and no fiscal rules.
 */
public record IcmsStTax(
        String modBcSt,
        BigDecimal stMarginRate,
        BigDecimal stReductionRate,
        BigDecimal stBase,
        BigDecimal stRate,
        BigDecimal stAmount,
        BigDecimal fcpStBase,
        BigDecimal fcpStRate,
        BigDecimal fcpStAmount) {
}
