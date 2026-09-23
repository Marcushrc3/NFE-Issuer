package com.example.nfe.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ItemDto(
        @NotBlank(message = "productCode must not be blank") String productCode,
        @NotBlank(message = "description must not be blank") String description,
        @NotBlank(message = "ncm must not be blank") String ncm,
        @NotBlank(message = "unit must not be blank") String unit,
        @NotNull(message = "quantity must not be null")
        @Positive(message = "quantity must be greater than zero") BigDecimal quantity,
        @NotNull(message = "unitValue must not be null")
        @PositiveOrZero(message = "unitValue must not be negative") BigDecimal unitValue,
        @NotNull(message = "totalValue must not be null")
        @PositiveOrZero(message = "totalValue must not be negative") BigDecimal totalValue,
        @NotBlank(message = "cfop must not be blank") String cfop,
        @NotBlank(message = "origin must not be blank") String origin) {
}
