package com.example.nfe.testfixtures;

import com.example.nfe.domain.Address;
import com.example.nfe.domain.CbsTax;
import com.example.nfe.domain.EmissionStatus;
import com.example.nfe.domain.IbsCbsTaxation;
import com.example.nfe.domain.IbsCbsTotals;
import com.example.nfe.domain.IbsMunicipalityTax;
import com.example.nfe.domain.IbsUfTax;
import com.example.nfe.domain.IcmsTax;
import com.example.nfe.domain.IcmsTotals;
import com.example.nfe.domain.IsTotals;
import com.example.nfe.domain.Issuer;
import com.example.nfe.domain.ItemTaxation;
import com.example.nfe.domain.NfeEmission;
import com.example.nfe.domain.NfeItem;
import com.example.nfe.domain.OperationType;
import com.example.nfe.domain.Payment;
import com.example.nfe.domain.PaymentDetail;
import com.example.nfe.domain.PaymentIndicator;
import com.example.nfe.domain.Recipient;
import com.example.nfe.domain.RecipientIeStatus;
import com.example.nfe.domain.RegularTaxation;
import com.example.nfe.domain.RtcTaxation;
import com.example.nfe.domain.TaxTotals;
import com.example.nfe.domain.Totals;
import com.example.nfe.domain.Transport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Shared test fixtures for real-component pipeline tests: a complete
 * emission whose generated XML is infNFe-valid per the official PL_010f
 * schemas, and an invalid variant used to prove validation failures.
 */
public final class NfeFixtures {

    private NfeFixtures() {
    }

    /**
     * Full emission: when generated, its {@code infNFe} passes the official
     * schema and its signed {@code NFe} passes {@code nfe_v4.00.xsd}.
     */
    public static NfeEmission fullEmission() {
        return emission(
                "55", 3, 1000L, OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializacao", "3550308",
                "35", "00000001", "1", "1", "1", "1", "9", "2", "1", "1", "1", "0", "1.0");
    }

    /**
     * Emission missing the mandatory ide identification/header fields: its
     * generated {@code infNFe} fails the official schema.
     */
    public static NfeEmission invalidInfNfeEmission() {
        return emission(null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Emission with an invalid cUF: signable (infNFe Id exists), yet the
     * signed document fails the complete NFe schema.
     */
    public static NfeEmission signableButInvalidNfeEmission() {
        return emission(
                "55", 3, 1000L, OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializacao", "3550308",
                "XX", "00000001", "1", "1", "1", "1", "0", "2", "1", "1", "1", "0", "1.0");
    }

    /**
     * Full emission carrying the complete RTC surface (item-level gCBS and
     * gTribRegular plus emission-level ISTot and IBSCBSTot): its generated
     * {@code infNFe} passes the official schema, proving every mapped
     * field is schema-valid.
     */
    public static NfeEmission fullEmissionWithRtcGroups() {
        return new NfeEmission(
                "emission-1", "REF-1", OperationType.TRANSFER, EmissionStatus.RECEIVED,
                "55", 3, 1000L, OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializacao", "3550308",
                "35", "00000001", "1", "1", "1", "1", "9", "2", "1", "1", "1", "0", "1.0",
                issuer(), recipient(), List.of(rtcItem()),
                totals(), rtcTaxTotals(), null, null, transport(), payment());
    }

    private static NfeItem rtcItem() {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN", new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                null, null,
                new ItemTaxation(
                        new IcmsTax("00", "3",
                                new BigDecimal("10.50"), new BigDecimal("18.00"), new BigDecimal("1.89"),
                                new BigDecimal("2.00"), new BigDecimal("0.21"),
                                null, null, null),
                        null, null, null, null,
                        new RtcTaxation("300", "001000", "1",
                                new IbsCbsTaxation(new BigDecimal("100.00"),
                                        new IbsUfTax(new BigDecimal("10.00"), null, null, null,
                                                new BigDecimal("8.50")),
                                        new IbsMunicipalityTax(new BigDecimal("5.00"), null, null, null,
                                                new BigDecimal("3.40")),
                                        new BigDecimal("11.90"),
                                        new CbsTax(new BigDecimal("9.87"), null, null, null,
                                                new BigDecimal("5.40")),
                                        new RegularTaxation("100", "001001",
                                                new BigDecimal("12.3400"), new BigDecimal("1.23"),
                                                new BigDecimal("5.6700"), new BigDecimal("0.45"),
                                                new BigDecimal("9.8700"), new BigDecimal("1.02"))))),
                null);
    }

    private static TaxTotals rtcTaxTotals() {
        return new TaxTotals(
                new IcmsTotals(
                        new BigDecimal("10.50"), new BigDecimal("1.89"), new BigDecimal("0.00"),
                        new BigDecimal("0.21"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00")),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("2.10"),
                new IsTotals(new BigDecimal("7.77")),
                new IbsCbsTotals(new BigDecimal("200.00"), null, null, null, null));
    }

    private static NfeEmission emission(
            String model, Integer series, Long number, OffsetDateTime emissionDate,
            String operationDescription, String fiscalEstablishmentCity,
            String stateCode, String randomCode, String operationDirection,
            String destinationType, String printFormat, String emissionType,
            String checkDigit, String environment, String purpose,
            String finalConsumer, String presenceIndicator, String processType,
            String processVersion) {
        return new NfeEmission(
                "emission-1", "REF-1", OperationType.TRANSFER, EmissionStatus.RECEIVED,
                model, series, number, emissionDate,
                operationDescription, fiscalEstablishmentCity,
                stateCode, randomCode, operationDirection, destinationType, printFormat,
                emissionType, checkDigit, environment, purpose, finalConsumer,
                presenceIndicator, processType, processVersion,
                issuer(), recipient(), List.of(item()),
                totals(), taxTotals(), null, null, transport(), payment());
    }

    private static Issuer issuer() {
        return new Issuer("Acme Ltd", "12345678000199", "123456789",
                new Address("Main St", "100", "Sao Paulo", "SP", "01310100", "Centro", "3550308"),
                "Acme Comercio Ltda", "3");
    }

    private static Recipient recipient() {
        return new Recipient("Beta Corp", "98765432000188",
                new Address("Rua B", "200", "Sao Paulo", "SP", "01310200", "Centro", "3550308"),
                RecipientIeStatus.CONTRIBUTOR);
    }

    private static NfeItem item() {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN", new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                null, null,
                new ItemTaxation(
                        new IcmsTax("00", "3",
                                new BigDecimal("10.50"), new BigDecimal("18.00"), new BigDecimal("1.89"),
                                new BigDecimal("2.00"), new BigDecimal("0.21"),
                                null, null, null),
                        null, null, null, null, null),
                null);
    }

    private static Totals totals() {
        return new Totals(
                new BigDecimal("10.50"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("10.50"));
    }

    private static TaxTotals taxTotals() {
        return new TaxTotals(
                new IcmsTotals(
                        new BigDecimal("10.50"), new BigDecimal("1.89"), new BigDecimal("0.00"),
                        new BigDecimal("0.21"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00")),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("2.10"),
                null, null);
    }

    private static Transport transport() {
        return new Transport("0", "12345678000199", "Acme Carrier",
                new Address("Port St", "1", "Santos", "SP", "11013000", "Centro", "3548500"));
    }

    private static Payment payment() {
        return new Payment(List.of(
                new PaymentDetail("17", new BigDecimal("10.50"), PaymentIndicator.IMMEDIATE,
                        LocalDate.of(2026, 9, 21))));
    }
}
