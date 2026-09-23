package com.example.nfe.api.dto;

import com.example.nfe.domain.PaymentIndicator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * API representation of a payment form detail (detPag).
 * <p>
 * {@code paymentMethod} is the official 2-digit payment form code and
 * {@code amount} the explicit payment value — neither is derived or
 * defaulted by the application.
 */
public record PaymentDetailDto(
        @NotBlank(message = "paymentMethod must not be blank") String paymentMethod,
        @NotNull(message = "amount must not be null") BigDecimal amount,
        PaymentIndicator indicator,
        LocalDate paymentDate) {
}
