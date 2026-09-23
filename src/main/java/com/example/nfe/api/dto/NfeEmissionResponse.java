package com.example.nfe.api.dto;

import com.example.nfe.application.service.ProcessingMode;
import com.example.nfe.application.service.ProcessingStatus;

/**
 * Processing result of an NF-e emission request.
 * <p>
 * {@code xml} carries the validated (XML_ONLY) or signed (EMIT) document.
 * For EMIT, {@code transmission} carries the API-safe SEFAZ batch and
 * authorization information (null for XML_ONLY and for a signed document
 * that was not transmitted). The raw SOAP response is never exposed.
 * No authorized/emitted status is fabricated by the application: outcomes
 * come exclusively from the configured {@code SefazTransmitter}.
 */
public record NfeEmissionResponse(
        String emissionId,
        ProcessingMode processingMode,
        ProcessingStatus status,
        String message,
        String xml,
        SefazTransmissionResponseDto transmission) {
}
