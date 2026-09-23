package com.example.nfe.domain;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * NF-e emission aggregate, shared by IMPORT, TRANSFER and EXPORT.
 * <p>
 * Composition over inheritance: operation-specific data lives in optional
 * sections ({@link ImportDetails}, {@link ExportDetails}) rather than in
 * subclasses. {@link Totals}, {@link TaxTotals} and {@link Transport} are
 * provisional placeholders.
 * <p>
 * Header fields ({@code model}, {@code series}, {@code number},
 * {@code emissionDate}, {@code operationDescription},
 * {@code fiscalEstablishmentCity}) map conceptually to {@code mod},
 * {@code serie}, {@code nNF}, {@code dhEmi}, {@code natOp} and
 * {@code cMunFG}; they may be null and carry no fiscal validation at this
 * layer.
 * <p>
 * Identification fields ({@code stateCode}→cUF, {@code randomCode}→cNF,
 * {@code operationDirection}→tpNF, {@code destinationType}→idDest,
 * {@code printFormat}→tpImp, {@code emissionType}→tpEmis,
 * {@code checkDigit}→cDV, {@code environment}→tpAmb,
 * {@code purpose}→finNFe, {@code finalConsumer}→indFinal,
 * {@code presenceIndicator}→indPres, {@code processType}→procEmi and
 * {@code processVersion}→verProc) complete the official {@code ide}
 * group. They are nullable, explicit configuration values — no defaults are
 * invented and no derivation is performed at this layer. In particular
 * {@code checkDigit} (cDV) is an explicit input; its calculation belongs to
 * a later dedicated increment and is NOT done here.
 * <p>
 * Invariants enforced at construction:
 * <ul>
 *   <li>IMPORT requires {@code importDetails} and forbids {@code exportDetails}</li>
 *   <li>EXPORT requires {@code exportDetails} and forbids {@code importDetails}</li>
 *   <li>TRANSFER forbids both sections</li>
 *   <li>{@code items} must not be empty and is defensively copied</li>
 * </ul>
 */
public record NfeEmission(
        String emissionId,
        String externalReference,
        OperationType operationType,
        EmissionStatus status,
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
        Issuer issuer,
        Recipient recipient,
        List<NfeItem> items,
        Totals totals,
        TaxTotals taxTotals,
        ImportDetails importDetails,
        ExportDetails exportDetails,
        Transport transport,
        Payment payment) {

    public NfeEmission {
        if (operationType == null) {
            throw new IllegalArgumentException("operationType must not be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        items = List.copyOf(items);

        switch (operationType) {
            case IMPORT -> {
                if (importDetails == null) {
                    throw new IllegalArgumentException("importDetails is required for IMPORT");
                }
                if (exportDetails != null) {
                    throw new IllegalArgumentException("exportDetails must be absent for IMPORT");
                }
            }
            case EXPORT -> {
                if (exportDetails == null) {
                    throw new IllegalArgumentException("exportDetails is required for EXPORT");
                }
                if (importDetails != null) {
                    throw new IllegalArgumentException("importDetails must be absent for EXPORT");
                }
            }
            case TRANSFER -> {
                if (importDetails != null || exportDetails != null) {
                    throw new IllegalArgumentException(
                            "importDetails and exportDetails must be absent for TRANSFER");
                }
            }
        }
    }
}
