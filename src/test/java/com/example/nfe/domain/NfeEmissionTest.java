package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NfeEmissionTest {

    private static final Address ADDRESS = new Address("Main St", "100", "Sao Paulo", "SP", "01310-100", "Centro", "3550308");
    private static final Issuer ISSUER = new Issuer("Acme Ltd", "12345678000199", "123456789", ADDRESS, null, null);
    private static final Recipient RECIPIENT = new Recipient("Beta Corp", "98765432000188", ADDRESS, null);
    private static final List<NfeItem> ITEMS = List.of(
            new NfeItem("P-1", "Widget", "84818090",
                    null, null, null,
                    "UN",
                    new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                    null, null, null,
                    null, null, null, null,
                    "5102", "0", null, null, null, null, null));
    private static final Totals TOTALS = new Totals(
            new BigDecimal("10.50"), null, null, null, null, new BigDecimal("10.50"));
    private static final TaxTotals TAX_TOTALS = new TaxTotals(null, null, null, null, null, null, null, null, null);
    private static final ImportDetails IMPORT_DETAILS =
            new ImportDetails("DI-123", LocalDate.of(2026, 1, 15), "Porto de Santos");
    private static final ExportDetails EXPORT_DETAILS = new ExportDetails("United States", null, null, null);

    @Test
    void shouldAcceptImportWithImportDetails() {
        NfeEmission emission = emission(OperationType.IMPORT, IMPORT_DETAILS, null);

        assertEquals(OperationType.IMPORT, emission.operationType());
        assertEquals(IMPORT_DETAILS, emission.importDetails());
    }

    @Test
    void shouldRejectImportWithoutImportDetails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emission(OperationType.IMPORT, null, null));

        assertTrue(exception.getMessage().contains("importDetails"));
    }

    @Test
    void shouldRejectImportWithExportDetails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emission(OperationType.IMPORT, IMPORT_DETAILS, EXPORT_DETAILS));

        assertTrue(exception.getMessage().contains("exportDetails"));
    }

    @Test
    void shouldAcceptExportWithExportDetails() {
        NfeEmission emission = emission(OperationType.EXPORT, null, EXPORT_DETAILS);

        assertEquals(OperationType.EXPORT, emission.operationType());
        assertEquals(EXPORT_DETAILS, emission.exportDetails());
    }

    @Test
    void shouldRejectExportWithoutExportDetails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emission(OperationType.EXPORT, null, null));

        assertTrue(exception.getMessage().contains("exportDetails"));
    }

    @Test
    void shouldRejectExportWithImportDetails() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emission(OperationType.EXPORT, IMPORT_DETAILS, EXPORT_DETAILS));

        assertTrue(exception.getMessage().contains("importDetails"));
    }

    @Test
    void shouldAcceptTransferWithoutOperationSpecificDetails() {
        NfeEmission emission = emission(OperationType.TRANSFER, null, null);

        assertEquals(OperationType.TRANSFER, emission.operationType());
        assertNull(emission.importDetails());
        assertNull(emission.exportDetails());
    }

    @Test
    void shouldRejectTransferWithImportDetails() {
        assertThrows(IllegalArgumentException.class,
                () -> emission(OperationType.TRANSFER, IMPORT_DETAILS, null));
    }

    @Test
    void shouldRejectTransferWithExportDetails() {
        assertThrows(IllegalArgumentException.class,
                () -> emission(OperationType.TRANSFER, null, EXPORT_DETAILS));
    }

    @Test
    void shouldRejectEmptyItems() {
        assertThrows(IllegalArgumentException.class,
                () -> emission(OperationType.TRANSFER, null, null, List.of()));
    }

    @Test
    void shouldExposeUnmodifiableItemsList() {
        NfeEmission emission = emission(OperationType.TRANSFER, null, null);

        assertThrows(UnsupportedOperationException.class, () -> emission.items().clear());
    }

    @Test
    void shouldDefensivelyCopyItemsList() {
        List<NfeItem> mutableItems = new ArrayList<>(ITEMS);

        NfeEmission emission = emission(OperationType.TRANSFER, null, null, mutableItems);
        mutableItems.clear();

        assertEquals(1, emission.items().size());
    }

    @Test
    void shouldAllowNullHeaderFields() {
        NfeEmission emission = emission(OperationType.TRANSFER, null, null);

        assertNull(emission.model());
        assertNull(emission.series());
        assertNull(emission.number());
        assertNull(emission.emissionDate());
        assertNull(emission.operationDescription());
        assertNull(emission.fiscalEstablishmentCity());
    }

    @Test
    void shouldPreserveHeaderFieldsExactlyAsSupplied() {
        OffsetDateTime emissionDate = OffsetDateTime.parse("2026-01-15T10:30:00-03:00");

        NfeEmission emission = emissionWithHeader(
                OperationType.IMPORT, IMPORT_DETAILS, null,
                "55", 3, 1000L,
                emissionDate,
                "Remessa para industrialização",
                "3550308");

        assertEquals("55", emission.model());
        assertEquals(3, emission.series());
        assertEquals(1000L, emission.number());
        assertEquals(emissionDate, emission.emissionDate());
        assertEquals("Remessa para industrialização", emission.operationDescription());
        assertEquals("3550308", emission.fiscalEstablishmentCity());
    }

    @Test
    void shouldKeepOperationInvariantsWithPopulatedHeader() {
        NfeEmission emission = emissionWithHeader(
                OperationType.IMPORT, IMPORT_DETAILS, null,
                "55", 1, 42L,
                OffsetDateTime.parse("2026-01-15T10:30:00-03:00"),
                "Importação",
                "3548500");

        assertEquals(OperationType.IMPORT, emission.operationType());
        assertEquals(IMPORT_DETAILS, emission.importDetails());
        assertNull(emission.exportDetails());
        assertEquals(1, emission.items().size());
    }

    @Test
    void shouldPreserveIdentificationFields() {
        NfeEmission emission = emissionWithIdentity(
                "35", "00000001", "1", "1", "1", "1", "0", "2", "1", "1", "1", "0", "1.0");

        assertEquals("35", emission.stateCode());
        assertEquals("00000001", emission.randomCode());
        assertEquals("1", emission.operationDirection());
        assertEquals("1", emission.destinationType());
        assertEquals("1", emission.printFormat());
        assertEquals("1", emission.emissionType());
        assertEquals("0", emission.checkDigit());
        assertEquals("2", emission.environment());
        assertEquals("1", emission.purpose());
        assertEquals("1", emission.finalConsumer());
        assertEquals("1", emission.presenceIndicator());
        assertEquals("0", emission.processType());
        assertEquals("1.0", emission.processVersion());
    }

    @Test
    void shouldAllowNullIdentificationFields() {
        NfeEmission emission = emission(OperationType.TRANSFER, null, null);

        assertNull(emission.stateCode());
        assertNull(emission.randomCode());
        assertNull(emission.operationDirection());
        assertNull(emission.destinationType());
        assertNull(emission.printFormat());
        assertNull(emission.emissionType());
        assertNull(emission.checkDigit());
        assertNull(emission.environment());
        assertNull(emission.purpose());
        assertNull(emission.finalConsumer());
        assertNull(emission.presenceIndicator());
        assertNull(emission.processType());
        assertNull(emission.processVersion());
    }

    private static NfeEmission emission(OperationType operationType,
                                        ImportDetails importDetails,
                                        ExportDetails exportDetails) {
        return emission(operationType, importDetails, exportDetails, ITEMS);
    }

    private static NfeEmission emission(OperationType operationType,
                                        ImportDetails importDetails,
                                        ExportDetails exportDetails,
                                        List<NfeItem> items) {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                operationType,
                EmissionStatus.RECEIVED,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                ISSUER,
                RECIPIENT,
                items,
                TOTALS,
                TAX_TOTALS,
                importDetails,
                exportDetails,
                null,
                null);
    }

    private static NfeEmission emissionWithIdentity(
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
            String processVersion) {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                null, null, null, null, null, null,
                stateCode, randomCode, operationDirection, destinationType,
                printFormat, emissionType, checkDigit, environment, purpose,
                finalConsumer, presenceIndicator, processType, processVersion,
                ISSUER,
                RECIPIENT,
                ITEMS,
                TOTALS,
                TAX_TOTALS,
                null,
                null,
                null,
                null);
    }

    private static NfeEmission emissionWithHeader(
            OperationType operationType,
            ImportDetails importDetails,
            ExportDetails exportDetails,
            String model,
            Integer series,
            Long number,
            OffsetDateTime emissionDate,
            String operationDescription,
            String fiscalEstablishmentCity) {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                operationType,
                EmissionStatus.RECEIVED,
                model, series, number, emissionDate, operationDescription, fiscalEstablishmentCity,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                ISSUER,
                RECIPIENT,
                ITEMS,
                TOTALS,
                TAX_TOTALS,
                importDetails,
                exportDetails,
                null,
                null);
    }
}
