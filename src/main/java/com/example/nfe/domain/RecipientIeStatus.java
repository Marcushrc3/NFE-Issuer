package com.example.nfe.domain;

/**
 * Recipient IE (state registration) status, mirroring the semantics of the
 * official NF-e {@code indIEDest} indicator:
 * <ul>
 *   <li>{@link #CONTRIBUTOR} — ICMS taxpayer</li>
 *   <li>{@link #EXEMPT} — taxpayer exempt from state registration</li>
 *   <li>{@link #NOT_CONTRIBUTOR} — non-taxpayer</li>
 * </ul>
 * <p>
 * This enum carries business meaning only. The official numeric codes
 * (1, 2, 9) belong to the XML infrastructure mapping layer and must never
 * leak into the domain.
 */
public enum RecipientIeStatus {
    CONTRIBUTOR,
    EXEMPT,
    NOT_CONTRIBUTOR
}
