package com.example.nfe.api.dto;

import com.example.nfe.application.service.ProcessingMode;
import com.example.nfe.domain.OperationType;
import com.example.nfe.domain.TaxTotals;
import com.example.nfe.domain.Totals;
import com.example.nfe.domain.Transport;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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
 * <p>
 * {@code transport} carries the existing domain {@link Transport} graph
 * (freight mode, carrier document/name and delivery address), reused
 * directly with no duplicated model. It is optional at the API boundary:
 * when omitted or null, the mapped domain transport remains null and no
 * {@code transp} group is generated. No default freight mode is invented —
 * the client supplies it explicitly when a valid NF-e requires it, and the
 * XSD validates the official {@code modFrete} enumeration (0..4, 9).
 * <p>
 * {@code totals} carries the existing domain {@link Totals} graph (the
 * commercial {@code ICMSTot} values: items total, freight, insurance,
 * discount, other charges and NF total), reused directly with no duplicated
 * model. It is optional at the API boundary: when omitted or null, the
 * application derives a minimal totals block from the item sum (legacy
 * behavior) — when supplied, every value passes through exactly and nothing
 * is calculated, derived or defaulted.
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
        @Pattern(regexp = "[0-9]{8}",
                message = "randomCode (cNF) must be exactly 8 digits")
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
        @Valid Totals totals,
        @Valid ImportDetailsDto importDetails,
        @Valid ExportDetailsDto exportDetails,
        @Valid PaymentDto payment,
        @Valid Transport transport,
        @NotNull(message = "processingMode must not be null") ProcessingMode processingMode) {

    /**
     * Convenience constructor preserving the pre-commercial-totals call
     * surface: callers that do not supply commercial totals keep working
     * unchanged (the totals block is derived from the item sum, exactly as
     * before this field existed).
     */
    public NfeEmissionRequest(String externalReference, OperationType operationType, String model,
                              Integer series, Long number, OffsetDateTime emissionDate,
                              String operationDescription, String fiscalEstablishmentCity,
                              String stateCode, String randomCode, String operationDirection,
                              String destinationType, String printFormat, String emissionType,
                              String checkDigit, String environment, String purpose,
                              String finalConsumer, String presenceIndicator, String processType,
                              String processVersion, IssuerDto issuer, RecipientDto recipient,
                              List<ItemDto> items, TaxTotals taxTotals,
                              ImportDetailsDto importDetails, ExportDetailsDto exportDetails,
                              PaymentDto payment, Transport transport,
                              ProcessingMode processingMode) {
        this(externalReference, operationType, model, series, number, emissionDate,
                operationDescription, fiscalEstablishmentCity, stateCode, randomCode,
                operationDirection, destinationType, printFormat, emissionType, checkDigit,
                environment, purpose, finalConsumer, presenceIndicator, processType,
                processVersion, issuer, recipient, items, taxTotals, null, importDetails,
                exportDetails, payment, transport, processingMode);
    }

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
                processVersion, issuer, recipient, items, null, null, importDetails, exportDetails,
                payment, null, processingMode);
    }
}
