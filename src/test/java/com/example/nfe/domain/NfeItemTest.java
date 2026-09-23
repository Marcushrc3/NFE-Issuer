package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NfeItemTest {

    @Test
    void shouldAcceptValidItem() {
        NfeItem item = item(new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"));

        assertEquals("P-1", item.productCode());
        assertEquals("Widget", item.description());
        assertEquals("84818090", item.ncm());
        assertEquals("UN", item.unit());
        assertEquals(new BigDecimal("1"), item.quantity());
        assertEquals(new BigDecimal("10.50"), item.unitValue());
        assertEquals(new BigDecimal("10.50"), item.totalValue());
        assertEquals("5102", item.cfop());
        assertEquals("0", item.origin());
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> item(BigDecimal.ZERO, new BigDecimal("10.50"), new BigDecimal("10.50")));
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> item(new BigDecimal("-1"), new BigDecimal("10.50"), new BigDecimal("10.50")));
    }

    @Test
    void shouldRejectNegativeUnitValue() {
        assertThrows(IllegalArgumentException.class,
                () -> item(new BigDecimal("1"), new BigDecimal("-0.01"), new BigDecimal("10.50")));
    }

    @Test
    void shouldAcceptZeroUnitValue() {
        NfeItem item = item(new BigDecimal("1"), BigDecimal.ZERO, BigDecimal.ZERO);

        assertEquals(0, item.unitValue().compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldKeepTotalValueExplicitlyAsSupplied() {
        BigDecimal totalValue = new BigDecimal("42.75");

        NfeItem item = item(new BigDecimal("3"), new BigDecimal("10.50"), totalValue);

        assertEquals(totalValue, item.totalValue());
    }

    @Test
    void shouldAllowNullMonetaryFields() {
        NfeItem item = item(new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"));

        assertNull(item.freight());
        assertNull(item.insurance());
        assertNull(item.discount());
        assertNull(item.otherCharges());
    }

    @Test
    void shouldPreserveMonetaryFieldsExactlyAsSupplied() {
        NfeItem item = item(
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("1000"),
                null, null, null,
                new BigDecimal("100"), new BigDecimal("20"),
                new BigDecimal("10"), new BigDecimal("5"), null);

        assertEquals(new BigDecimal("100"), item.freight());
        assertEquals(new BigDecimal("20"), item.insurance());
        assertEquals(new BigDecimal("10"), item.discount());
        assertEquals(new BigDecimal("5"), item.otherCharges());
    }

    @Test
    void shouldNotCalculateTotalValueFromMonetaryFields() {
        // supplied totalValue (1000) intentionally differs from
        // 10.50 + 100 + 20 - 10 + 5 = 125.50 — the domain must not recalculate.
        NfeItem item = item(
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("1000"),
                null, null, null,
                new BigDecimal("100"), new BigDecimal("20"),
                new BigDecimal("10"), new BigDecimal("5"), null);

        assertEquals(new BigDecimal("1000"), item.totalValue());
    }

    @Test
    void shouldAllowNullImportDetails() {
        NfeItem item = item(new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"));

        assertNull(item.importDetails());
    }

    @Test
    void shouldPreserveImportDetailsWhenSupplied() {
        ImportItemDetails importDetails = new ImportItemDetails("24-1234567-8", 1, 2, null);

        NfeItem item = item(
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                importDetails);

        assertSame(importDetails, item.importDetails());
    }

    @Test
    void shouldAllowNullTributaryFields() {
        NfeItem item = item(new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"));

        assertNull(item.tributaryUnit());
        assertNull(item.tributaryQuantity());
        assertNull(item.tributaryUnitValue());
    }

    @Test
    void shouldPreserveTributaryFieldsExactlyAsSupplied() {
        NfeItem item = item(
                "BOX", new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("1000"),
                "UN", new BigDecimal("120"), new BigDecimal("8.3333"));

        assertEquals("BOX", item.unit());
        assertEquals(new BigDecimal("10"), item.quantity());
        assertEquals(new BigDecimal("100"), item.unitValue());
        assertEquals("UN", item.tributaryUnit());
        assertEquals(new BigDecimal("120"), item.tributaryQuantity());
        assertEquals(new BigDecimal("8.3333"), item.tributaryUnitValue());
    }

    @Test
    void shouldKeepCommercialAndTributaryValuesIndependent() {
        // no conversion or derivation: commercial 10 x 100 = 1000,
        // tributary 7 x 50 = 350, totalValue stays exactly as supplied.
        NfeItem item = item(
                "BOX", new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("1000"),
                "UN", new BigDecimal("7"), new BigDecimal("50"));

        assertEquals(new BigDecimal("1000"), item.totalValue());
        assertEquals(new BigDecimal("7"), item.tributaryQuantity());
        assertEquals(new BigDecimal("50"), item.tributaryUnitValue());
    }

    @Test
    void shouldAllowNullIdentificationFields() {
        NfeItem item = itemWithIdentification(null, null, null, null);

        assertNull(item.cEan());
        assertNull(item.cEanTrib());
        assertNull(item.cest());
        assertNull(item.indTot());
    }

    @Test
    void shouldPreserveIdentificationFieldsExactlyAsSupplied() {
        NfeItem item = itemWithIdentification("7891234567895", "7891234567895", "0101010", true);

        assertEquals("7891234567895", item.cEan());
        assertEquals("7891234567895", item.cEanTrib());
        assertEquals("0101010", item.cest());
        assertEquals(Boolean.TRUE, item.indTot());
    }

    @Test
    void shouldAllowCommercialAndTributaryEanToBeDifferent() {
        NfeItem equal = itemWithIdentification("7891234567895", "7891234567895", null, null);
        NfeItem different = itemWithIdentification("7891234567895", "7899876543210", null, null);

        assertEquals("7891234567895", equal.cEan());
        assertEquals("7891234567895", equal.cEanTrib());
        assertEquals("7891234567895", different.cEan());
        assertEquals("7899876543210", different.cEanTrib());
    }

    @Test
    void shouldAllowIndTotToBeFalse() {
        NfeItem item = itemWithIdentification(null, null, null, false);

        assertEquals(Boolean.FALSE, item.indTot());
    }

    @Test
    void shouldAllowIdentificationFieldsToExistIndependently() {
        NfeItem mixed = itemWithIdentification(null, "7891234567895", "0101010", null);
        NfeItem cestOnly = itemWithIdentification(null, null, "0202020", null);

        assertNull(mixed.cEan());
        assertEquals("7891234567895", mixed.cEanTrib());
        assertEquals("0101010", mixed.cest());
        assertNull(mixed.indTot());

        assertNull(cestOnly.cEan());
        assertNull(cestOnly.cEanTrib());
        assertEquals("0202020", cestOnly.cest());
        assertNull(cestOnly.indTot());
    }

    @Test
    void shouldAllowNullPurchaseOrderFields() {
        NfeItem item = itemWithPurchaseOrder(null, null);

        assertNull(item.xPed());
        assertNull(item.nItemPed());
    }

    @Test
    void shouldPreservePurchaseOrderFieldsExactlyAsSupplied() {
        NfeItem item = itemWithPurchaseOrder("PO-2026-000123", "00010");

        assertEquals("PO-2026-000123", item.xPed());
        assertEquals("00010", item.nItemPed());
    }

    @Test
    void shouldAllowPurchaseOrderNumberWithoutItemNumber() {
        NfeItem item = itemWithPurchaseOrder("PO-2026-000123", null);

        assertEquals("PO-2026-000123", item.xPed());
        assertNull(item.nItemPed());
    }

    @Test
    void shouldAllowPurchaseOrderItemWithoutPurchaseOrderNumber() {
        NfeItem item = itemWithPurchaseOrder(null, "00010");

        assertNull(item.xPed());
        assertEquals("00010", item.nItemPed());
    }

    @Test
    void shouldKeepPurchaseOrderFieldsIndependent() {
        NfeItem pedOnly = itemWithPurchaseOrder("PO-2026-000123", null);
        NfeItem itemOnly = itemWithPurchaseOrder(null, "00010");

        assertEquals("PO-2026-000123", pedOnly.xPed());
        assertNull(pedOnly.nItemPed());
        assertNull(itemOnly.xPed());
        assertEquals("00010", itemOnly.nItemPed());
    }

    private static NfeItem itemWithPurchaseOrder(String xPed, String nItemPed) {
        return new NfeItem(
                "P-1",
                "Widget",
                "84818090",
                null,
                null,
                null,
                "UN",
                new BigDecimal("1"),
                new BigDecimal("10.50"),
                new BigDecimal("10.50"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "5102",
                "0",
                null,
                xPed,
                nItemPed,
                null,
                null);
    }

    private static NfeItem itemWithIdentification(
            String cEan,
            String cEanTrib,
            String cest,
            Boolean indTot) {
        return new NfeItem(
                "P-1",
                "Widget",
                "84818090",
                cEan,
                cEanTrib,
                cest,
                "UN",
                new BigDecimal("1"),
                new BigDecimal("10.50"),
                new BigDecimal("10.50"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "5102",
                "0",
                indTot,
                null,
                null,
                null,
                null);
    }

    private static NfeItem item(BigDecimal quantity, BigDecimal unitValue, BigDecimal totalValue) {
        return item(quantity, unitValue, totalValue, null, null, null, null, null, null, null, null);
    }

    private static NfeItem item(
            BigDecimal quantity,
            BigDecimal unitValue,
            BigDecimal totalValue,
            ImportItemDetails importDetails) {
        return item(quantity, unitValue, totalValue, null, null, null, null, null, null, null, importDetails);
    }

    private static NfeItem item(
            String unit,
            BigDecimal quantity,
            BigDecimal unitValue,
            BigDecimal totalValue,
            String tributaryUnit,
            BigDecimal tributaryQuantity,
            BigDecimal tributaryUnitValue) {
        return new NfeItem(
                "P-1",
                "Widget",
                "84818090",
                null,
                null,
                null,
                unit,
                quantity,
                unitValue,
                totalValue,
                tributaryUnit,
                tributaryQuantity,
                tributaryUnitValue,
                null,
                null,
                null,
                null,
                "5102",
                "0",
                null,
                null,
                null,
                null,
                null);
    }

    private static NfeItem item(
            BigDecimal quantity,
            BigDecimal unitValue,
            BigDecimal totalValue,
            String tributaryUnit,
            BigDecimal tributaryQuantity,
            BigDecimal tributaryUnitValue,
            BigDecimal freight,
            BigDecimal insurance,
            BigDecimal discount,
            BigDecimal otherCharges,
            ImportItemDetails importDetails) {
        return new NfeItem(
                "P-1",
                "Widget",
                "84818090",
                null,
                null,
                null,
                "UN",
                quantity,
                unitValue,
                totalValue,
                tributaryUnit,
                tributaryQuantity,
                tributaryUnitValue,
                freight,
                insurance,
                discount,
                otherCharges,
                "5102",
                "0",
                null,
                null,
                null,
                null,
                importDetails);
    }
}
