package com.example.nfe.api.dto;

import com.example.nfe.application.service.ProcessingMode;
import com.example.nfe.domain.OperationType;
import com.example.nfe.domain.TaxTotals;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * NF-e emission request.
 * <p>
 * {@code taxTotals} carries the existing domain {@link TaxTotals} graph
 * (the {@code total/ICMSTot} source values: ICMS totals plus vII, vIPI,
 * vIPIDevol, vPIS, vCOFINS and vTotTrib). The shared domain type is reused
 * directly — no duplicated or parallel totals model. It is optional at the
 * API boundary: when omitted or null, the mapped domain tax totals remain
 * null and the generated {@code ICMSTot} lacks the corresponding explicit
 * values. Every value is pass-through: the application performs no
 * calculation, derivation or defaulting of tax totals; the bundled PL_010f
 * XSD remains the authoritative structural/lexical boundary.
 */
public record NfeEmissionRequest(
        @NotBlank(message = "externalReference must not be blank") String externalReference,
        @NotNull(message = "operationType must not be null") OperationType operationType,
        String model,
        Integer series,
        Long number,
        OffsetDateTime emissionDate,
        String operationDescription,
        String fiscalEstablishmentCity,
        String stateCode,
        String randomCode,
        String operationDirection,
        String destinationType,
        String printFormat,
        String emissionType,
        String checkDigit,
        String environment,
        String purpose,
        String finalConsumer,
        String presenceIndicator,
        String processType,
        String processVersion,
        @NotNull(message = "issuer must not be null") @Valid IssuerDto issuer,
        @NotNull(message = "recipient must not be null") @Valid RecipientDto recipient,
        @NotEmpty(message = "items must contain at least one item") @Valid List<ItemDto> items,
        @Valid TaxTotals taxTotals,
        @Valid ImportDetailsDto importDetails,
        @Valid ExportDetailsDto exportDetails,
        @Valid PaymentDto payment,
        @NotNull(message = "processingMode must not be null") ProcessingMode processingMode) {

    /**
     * Convenience constructor preserving the pre-tax-totals call surface:
     * callers that do not supply tax totals keep working unchanged (the
     * values are simply absent, exactly as before this contract existed).
     */
    public NfeEmissionRequest(String externalReference, OperationType operationType, String model,
                              Integer series, Long number, OffsetDateTime emissionDate,
                              String operationDescription, String fiscalEstablishmentCity,
                              String stateCode, String randomCode, String operationDirection,
                              String destinationType, String printFormat, String emissionType,
                              String checkDigit, String environment, String purpose,
                              String finalConsumer, String presenceIndicator, String processType,
                              String processVersion, IssuerDto issuer, RecipientDto recipient,
                              List<ItemDto> items, ImportDetailsDto importDetails,
                              ExportDetailsDto exportDetails, PaymentDto payment,
                              ProcessingMode processingMode) {
        this(externalReference, operationType, model, series, number, emissionDate,
                operationDescription, fiscalEstablishmentCity, stateCode, randomCode,
                operationDirection, destinationType, printFormat, emissionType, checkDigit,
                environment, purpose, finalConsumer, presenceIndicator, processType,
                processVersion, issuer, recipient, items, null, importDetails, exportDetails,
                payment, processingMode);
    }
}
