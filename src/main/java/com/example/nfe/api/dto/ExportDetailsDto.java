package com.example.nfe.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ExportDetailsDto(
        @NotBlank(message = "destinationCountry must not be blank") String destinationCountry) {
}
