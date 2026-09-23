package com.example.nfe.domain;

import java.math.BigDecimal;

/**
 * Item of the emission.
 * <p>
 * {@code ncm}, {@code unit}, {@code cfop} and {@code origin} are fiscal codes
 * carried as data only — no enums and no validation rules are applied by the
 * the domain. {@code taxation} is an optional {@link ItemTaxation} placeholder
 * for the future taxation model and {@code importDetails} is an optional
 * {@link ImportItemDetails} reference, present only for imported items.
 * <p>
 * Monetary composition ({@code freight}, {@code insurance}, {@code discount}
 * and {@code otherCharges}) is supplied explicitly: the domain performs no
 * calculation and these fields may be null. The tributary triplet
 * ({@code tributaryUnit}, {@code tributaryQuantity},
 * {@code tributaryUnitValue}) maps to {@code uTrib/qTrib/vUnTrib}; it is
 * independent from the commercial triplet and no conversion or derivation
 * is performed. Identification fields ({@code cEan}, {@code cEanTrib},
 * {@code cest}, {@code indTot}) are carried as plain data: no length,
 * format or check-digit validation, no derivation of the tributary EAN from
 * the commercial one, and {@code indTot} does not affect
 * {@code totalValue}. The purchase-order reference ({@code xPed},
 * {@code nItemPed}) is source/document data: the domain stores it when
 * supplied but does not know whether the source is a legacy system, an
 * ERP, an API client or any other external system; the two fields are
 * independent and never derived from one another. Basic invariants:
 * {@code quantity} must be positive and {@code unitValue} must not be
 * negative. {@code totalValue} is supplied explicitly; no calculation is
 * performed by the domain.
 */
public record NfeItem(
        String productCode,
        String description,
        String ncm,
        String cEan,
        String cEanTrib,
        String cest,
        String unit,
        BigDecimal quantity,
        BigDecimal unitValue,
        BigDecimal totalValue,
        String tributaryUnit,
        BigDecimal tributaryQuantity,
        BigDecimal tributaryUnitValue,
        BigDecimal freight,
        BigDecimal insurance,
        BigDecimal discount,
        BigDecimal otherCharges,
        String cfop,
        String origin,
        Boolean indTot,
        String xPed,
        String nItemPed,
        ItemTaxation taxation,
        ImportItemDetails importDetails) {

    public NfeItem {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitValue == null || unitValue.signum() < 0) {
            throw new IllegalArgumentException("unitValue must not be negative");
        }
    }
}
