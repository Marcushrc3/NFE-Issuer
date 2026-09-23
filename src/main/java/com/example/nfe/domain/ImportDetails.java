package com.example.nfe.domain;

import java.time.LocalDate;

/**
 * Provisional import-specific information, present only for IMPORT emissions.
 * The fields do not yet represent the official NF-e model and DI/DUIMP
 * integration stays completely outside the domain.
 */
public record ImportDetails(
        String declarationNumber,
        LocalDate declarationDate,
        String clearancePlace) {
}
