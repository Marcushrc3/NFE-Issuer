package com.example.nfe.domain;

/**
 * Export-specific information, present only for EXPORT emissions.
 * <p>
 * {@code destinationCountry} is the destination country of the export,
 * {@code exitState} the state from which the export operation exits,
 * {@code exportDispatchLocation} the place associated with the export
 * dispatch and {@code drawbackNumber} the drawback reference when
 * applicable. All fields are nullable, supplied as plain data: no code
 * validation, no inference and no calculations are applied by the domain.
 */
public record ExportDetails(
        String destinationCountry,
        String exitState,
        String exportDispatchLocation,
        String drawbackNumber) {
}
