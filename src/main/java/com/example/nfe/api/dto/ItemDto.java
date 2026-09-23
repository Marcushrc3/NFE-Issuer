package com.example.nfe.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * API representation of a single NF-e item.
 * <p>
 * The six tributary identification fields ({@code cEan}, {@code cEanTrib},
 * {@code tributaryUnit}, {@code tributaryQuantity},
 * {@code tributaryUnitValue}, {@code indTot}) map to the {@code prod} group
 * elements {@code cEAN}, {@code cEANTrib}, {@code uTrib}, {@code qTrib},
 * {@code vUnTrib} and {@code indTot}, which are mandatory in the official
 * PL_010f schema. They are optional at the API boundary on purpose: when
 * omitted or null, the mapped domain values remain null, the XML mapper
 * emits no elements for them and XSD validation rejects the incomplete
 * NF-e. No value is derived or invented: no GTIN derivation, no tributary
 * triplet conversion from the commercial triplet, no {@code indTot}
 * heuristic. The bundled XSD remains the authoritative lexical boundary
 * ({@code cEAN}/{@code cEANTrib}: {@code SEM GTIN|8|12-14 digits};
 * {@code uTrib}: 1-6 chars; {@code qTrib}/{@code vUnTrib}: non-negative
 * decimals with 1-4 / 1-10 decimal places; {@code indTot}: 0 or 1).
 */
public record ItemDto(
        @NotBlank(message = "productCode must not be blank") String productCode,
        @NotBlank(message = "description must not be blank") String description,
        @NotBlank(message = "ncm must not be blank") String ncm,
        String cEan,
        String cEanTrib,
        @NotBlank(message = "unit must not be blank") String unit,
        @NotNull(message = "quantity must not be null")
        @Positive(message = "quantity must be greater than zero") BigDecimal quantity,
        @NotNull(message = "unitValue must not be null")
        @PositiveOrZero(message = "unitValue must not be negative") BigDecimal unitValue,
        @NotNull(message = "totalValue must not be null")
        @PositiveOrZero(message = "totalValue must not be negative") BigDecimal totalValue,
        String tributaryUnit,
        BigDecimal tributaryQuantity,
        BigDecimal tributaryUnitValue,
        @NotBlank(message = "cfop must not be blank") String cfop,
        @NotBlank(message = "origin must not be blank") String origin,
        Boolean indTot) {

    /**
     * Convenience constructor preserving the pre-tributary-field call
     * surface: callers that do not supply the tributary fields keep
     * working unchanged (the values are simply absent, exactly as before
     * this contract existed).
     */
    public ItemDto(String productCode, String description, String ncm, String unit,
                   BigDecimal quantity, BigDecimal unitValue, BigDecimal totalValue,
                   String cfop, String origin) {
        this(productCode, description, ncm, null, null,
                unit, quantity, unitValue, totalValue,
                null, null, null,
                cfop, origin, null);
    }
}
