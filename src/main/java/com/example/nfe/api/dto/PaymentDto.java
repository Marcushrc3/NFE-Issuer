package com.example.nfe.api.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * API representation of the payment group (pag). Optional — when omitted,
 * the generated XML remains schema-invalid until the source supplies it.
 */
public record PaymentDto(
        @NotEmpty(message = "details must not be empty") List<PaymentDetailDto> details) {
}
