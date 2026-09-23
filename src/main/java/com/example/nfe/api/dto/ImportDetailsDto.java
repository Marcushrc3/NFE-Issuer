package com.example.nfe.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ImportDetailsDto(
        @NotBlank(message = "declarationNumber must not be blank") String declarationNumber,
        @NotNull(message = "declarationDate must not be null") LocalDate declarationDate,
        @NotBlank(message = "clearancePlace must not be blank") String clearancePlace) {
}
