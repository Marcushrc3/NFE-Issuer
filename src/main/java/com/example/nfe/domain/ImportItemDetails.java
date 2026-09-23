package com.example.nfe.domain;

/**
 * Neutral customs declaration/addition reference for an imported item.
 * <p>
 * Carries the declaration number, the addition number/sequence that tie
 * the item to its customs entry, and the FCI reference when applicable.
 * Fields are plain data, nullable, and free of any legacy or
 * regime-specific naming (DI/DUIMP). The domain stores {@code fci} when
 * supplied but knows nothing about how it is obtained (DI, DUIMP, Portal
 * Único, legacy system or any external source). No validation, no
 * calculations and no fiscal rules are applied by the domain.
 */
public record ImportItemDetails(
        String declarationNumber,
        Integer additionNumber,
        Integer additionSequence,
        String fci) {
}
