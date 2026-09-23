package com.example.nfe.application.service;

import com.example.nfe.api.dto.ExportDetailsDto;
import com.example.nfe.api.dto.ImportDetailsDto;
import com.example.nfe.api.dto.IssuerDto;
import com.example.nfe.api.dto.ItemDto;
import com.example.nfe.api.dto.NfeEmissionRequest;
import com.example.nfe.api.dto.NfeEmissionResponse;
import com.example.nfe.api.dto.PaymentDetailDto;
import com.example.nfe.api.dto.PaymentDto;
import com.example.nfe.api.dto.RecipientDto;
import com.example.nfe.api.dto.SefazAuthorizationResponseDto;
import com.example.nfe.api.dto.SefazTransmissionResponseDto;
import com.example.nfe.application.sefaz.SefazAuthorizationResult;
import com.example.nfe.application.sefaz.SefazAuthorizationStatus;
import com.example.nfe.application.sefaz.SefazNotConfiguredException;
import com.example.nfe.application.sefaz.SefazTransmissionResult;
import com.example.nfe.application.sefaz.SefazTransmissionStatus;
import com.example.nfe.application.sefaz.SefazTransmitter;
import com.example.nfe.domain.Address;
import com.example.nfe.domain.IbsCbsTaxation;
import com.example.nfe.domain.IbsMunicipalityTax;
import com.example.nfe.domain.IbsUfTax;
import com.example.nfe.domain.IcmsStTax;
import com.example.nfe.domain.IcmsTax;
import com.example.nfe.domain.ImportTax;
import com.example.nfe.domain.IpiTax;
import com.example.nfe.domain.IsTax;
import com.example.nfe.domain.ItemTaxation;
import com.example.nfe.domain.PisCofinsTax;
import com.example.nfe.domain.RtcTaxation;
import com.example.nfe.domain.EmissionStatus;
import com.example.nfe.domain.NfeEmission;
import com.example.nfe.domain.NfeItem;
import com.example.nfe.domain.OperationType;
import com.example.nfe.domain.PaymentDetail;
import com.example.nfe.domain.PaymentIndicator;
import com.example.nfe.domain.RecipientIeStatus;
import com.example.nfe.infrastructure.signature.XmlSigner;
import com.example.nfe.infrastructure.xml.NfeXmlGenerator;
import com.example.nfe.infrastructure.xml.validation.NfeXmlValidationException;
import com.example.nfe.infrastructure.xml.validation.NfeXmlValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NfeEmissionServiceTest {

    private static final String GENERATED_XML = "<NFe><infNFe Id=\"NFeTest\"/></NFe>";
    private static final String SIGNED_XML = "<NFe><infNFe Id=\"NFeTest\"/><Signature/></NFe>";
    private static final SefazAuthorizationResult AUTHORIZATION = new SefazAuthorizationResult(
            SefazAuthorizationStatus.AUTHORIZED,
            100,
            "Autorizado o uso da NF-e",
            "135260000000123",
            "35260912345678000199550030000010001000000018",
            "dGVzdA==");

    private static final SefazTransmissionResult AUTHORIZED_RESULT = new SefazTransmissionResult(
            SefazTransmissionStatus.AUTHORIZED,
            103,
            "Lote recebido com sucesso",
            null,
            AUTHORIZATION,
            "<raw/>");

    private static final SefazTransmissionResult REJECTED_RESULT = new SefazTransmissionResult(
            SefazTransmissionStatus.REJECTED,
            103,
            "Lote recebido com sucesso",
            null,
            new SefazAuthorizationResult(
                    SefazAuthorizationStatus.REJECTED,
                    110,
                    "Uso Denegado",
                    "135260000000124",
                    null,
                    null),
            "<raw/>");

    private static final SefazTransmissionResult PROCESSING_RESULT = new SefazTransmissionResult(
            SefazTransmissionStatus.PROCESSING,
            103,
            "Lote recebido com sucesso",
            "351000012345678",
            null,
            "<raw/>");

    private static final SefazTransmissionResult BATCH_REJECTED_RESULT = new SefazTransmissionResult(
            SefazTransmissionStatus.REJECTED,
            225,
            "Rejeicao: Falha no Schema XML do lote",
            null,
            null,
            "<raw/>");

    private static final SefazTransmissionResult ERROR_RESULT = new SefazTransmissionResult(
            SefazTransmissionStatus.ERROR,
            null,
            "SEFAZ transport failure: ConnectException",
            null,
            null,
            null);

    private final NfeXmlGenerator xmlGenerator = mock(NfeXmlGenerator.class);
    private final NfeXmlValidator xmlValidator = mock(NfeXmlValidator.class);
    private final XmlSigner xmlSigner = mock(XmlSigner.class);
    private final SefazTransmitter sefazTransmitter = mock(SefazTransmitter.class);

    private final NfeEmissionService service =
            new NfeEmissionService(xmlGenerator, xmlValidator, Optional.empty(), Optional.empty());
    private final NfeEmissionService signingService =
            new NfeEmissionService(xmlGenerator, xmlValidator, Optional.of(xmlSigner), Optional.of(sefazTransmitter));
    private final NfeEmissionService signedOnlyService =
            new NfeEmissionService(xmlGenerator, xmlValidator, Optional.of(xmlSigner), Optional.empty());

    @Test
    void shouldReturnXmlGeneratedResponseForXmlOnly() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);

        NfeEmissionResponse response = service.issue(xmlOnlyRequest());

        assertNotNull(response.emissionId());
        assertFalse(response.emissionId().isBlank());
        assertEquals(ProcessingMode.XML_ONLY, response.processingMode());
        assertEquals(ProcessingStatus.XML_GENERATED, response.status());
        assertEquals("NF-e XML generated and XSD-validated", response.message());
        assertEquals(GENERATED_XML, response.xml());
        assertNull(response.transmission());
        verify(xmlValidator).validateInfNFe(GENERATED_XML);
        verifyNoInteractions(xmlSigner);
        verifyNoInteractions(sefazTransmitter);
    }

    @Test
    void shouldGenerateUniqueEmissionIdsForEachRequest() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        NfeEmissionRequest request = xmlOnlyRequest();

        NfeEmissionResponse first = service.issue(request);
        NfeEmissionResponse second = service.issue(request);

        assertNotEquals(first.emissionId(), second.emissionId());
    }

    @Test
    void shouldReturnSignedResponseForEmit() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(GENERATED_XML)).thenReturn(SIGNED_XML);
        when(sefazTransmitter.transmit(SIGNED_XML)).thenReturn(AUTHORIZED_RESULT);

        NfeEmissionResponse response = signingService.issue(emitRequest());

        assertEquals(ProcessingMode.EMIT, response.processingMode());
        assertEquals(ProcessingStatus.AUTHORIZED, response.status());
        assertEquals("NF-e signed and submitted to the SEFAZ transmission boundary", response.message());
        assertEquals(SIGNED_XML, response.xml());
        assertNotNull(response.transmission());
        assertEquals(SefazTransmissionStatus.AUTHORIZED, response.transmission().status());
        assertEquals(103, response.transmission().code());
        assertNotNull(response.transmission().authorization());
        assertEquals(SefazAuthorizationStatus.AUTHORIZED, response.transmission().authorization().status());
        verify(xmlValidator).validateNFe(SIGNED_XML);
        verify(xmlSigner).sign(GENERATED_XML);
        verify(sefazTransmitter).transmit(SIGNED_XML);
    }

    @Test
    void shouldSignTheExactGeneratedXml() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(any())).thenReturn(SIGNED_XML);
        when(sefazTransmitter.transmit(any())).thenReturn(AUTHORIZED_RESULT);

        signingService.issue(emitRequest());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(xmlSigner).sign(captor.capture());
        assertEquals(GENERATED_XML, captor.getValue());
    }

    @Test
    void shouldPassExactSignedXmlToTransmitter() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(any())).thenReturn(SIGNED_XML);
        when(sefazTransmitter.transmit(any())).thenReturn(AUTHORIZED_RESULT);

        signingService.issue(emitRequest());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sefazTransmitter).transmit(captor.capture());
        assertEquals(SIGNED_XML, captor.getValue());
    }

    @Test
    void shouldPropagateTransmissionResult() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(any())).thenReturn(SIGNED_XML);
        when(sefazTransmitter.transmit(any())).thenReturn(AUTHORIZED_RESULT);

        NfeEmissionResponse response = signingService.issue(emitRequest());

        SefazTransmissionResponseDto result = response.transmission();
        assertNotNull(result);
        assertEquals(SefazTransmissionStatus.AUTHORIZED, result.status());
        assertEquals(103, result.code());
        assertEquals("Lote recebido com sucesso", result.message());
        assertNull(result.receipt());

        SefazAuthorizationResponseDto authorization = result.authorization();
        assertNotNull(authorization);
        assertEquals(SefazAuthorizationStatus.AUTHORIZED, authorization.status());
        assertEquals(100, authorization.code());
        assertEquals("Autorizado o uso da NF-e", authorization.message());
        assertEquals("135260000000123", authorization.protocol());
        assertEquals("35260912345678000199550030000010001000000018", authorization.accessKey());
        assertEquals("dGVzdA==", authorization.digestValue());
    }

    @Test
    void shouldMapRejectedAuthorizationToRejectedResponse() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(any())).thenReturn(SIGNED_XML);
        when(sefazTransmitter.transmit(any())).thenReturn(REJECTED_RESULT);

        NfeEmissionResponse response = signingService.issue(emitRequest());

        assertEquals(ProcessingStatus.REJECTED, response.status());
        SefazTransmissionResponseDto transmission = response.transmission();
        assertNotNull(transmission);
        assertEquals(SefazTransmissionStatus.REJECTED, transmission.status());
        assertEquals(103, transmission.code());
        SefazAuthorizationResponseDto authorization = transmission.authorization();
        assertNotNull(authorization);
        assertEquals(SefazAuthorizationStatus.REJECTED, authorization.status());
        assertEquals(110, authorization.code());
        assertEquals("Uso Denegado", authorization.message());
        assertEquals("135260000000124", authorization.protocol());
    }

    @Test
    void shouldMapProcessingTransmissionToProcessingResponse() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(any())).thenReturn(SIGNED_XML);
        when(sefazTransmitter.transmit(any())).thenReturn(PROCESSING_RESULT);

        NfeEmissionResponse response = signingService.issue(emitRequest());

        assertEquals(ProcessingStatus.PROCESSING, response.status());
        SefazTransmissionResponseDto transmission = response.transmission();
        assertNotNull(transmission);
        assertEquals(SefazTransmissionStatus.PROCESSING, transmission.status());
        assertEquals("351000012345678", transmission.receipt());
        assertNull(transmission.authorization());
    }

    @Test
    void shouldMapBatchRejectionWithoutFabricatingAuthorization() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(any())).thenReturn(SIGNED_XML);
        when(sefazTransmitter.transmit(any())).thenReturn(BATCH_REJECTED_RESULT);

        NfeEmissionResponse response = signingService.issue(emitRequest());

        assertEquals(ProcessingStatus.REJECTED, response.status());
        SefazTransmissionResponseDto transmission = response.transmission();
        assertNotNull(transmission);
        assertEquals(225, transmission.code());
        assertEquals("Rejeicao: Falha no Schema XML do lote", transmission.message());
        assertNull(transmission.authorization());
    }

    @Test
    void shouldFailEmitWhenTransmissionReturnsError() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(any())).thenReturn(SIGNED_XML);
        when(sefazTransmitter.transmit(any())).thenReturn(ERROR_RESULT);

        SefazTransmissionFailedException exception =
                assertThrows(SefazTransmissionFailedException.class, () -> signingService.issue(emitRequest()));
        assertEquals("SEFAZ transport failure: ConnectException", exception.getMessage());
        verify(sefazTransmitter).transmit(SIGNED_XML);
    }

    @Test
    void shouldFailEmitWithoutSigner() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);

        assertThrows(SigningNotConfiguredException.class, () -> service.issue(emitRequest()));
        verify(xmlValidator, never()).validateNFe(any());
        verify(xmlValidator, never()).validateInfNFe(any());
        verifyNoInteractions(xmlSigner);
        verifyNoInteractions(sefazTransmitter);
    }

    @Test
    void shouldFailEmitWithoutTransmitter() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(any())).thenReturn(SIGNED_XML);

        assertThrows(SefazNotConfiguredException.class, () -> signedOnlyService.issue(emitRequest()));
        verify(xmlSigner).sign(GENERATED_XML);
        verifyNoInteractions(sefazTransmitter);
    }

    @Test
    void shouldNotTransmitWhenSignedXmlFailsValidation() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        when(xmlSigner.sign(GENERATED_XML)).thenReturn(SIGNED_XML);
        doThrow(new NfeXmlValidationException("invalid XML")).when(xmlValidator).validateNFe(SIGNED_XML);

        assertThrows(NfeXmlValidationException.class, () -> signingService.issue(emitRequest()));
        verify(xmlSigner).sign(GENERATED_XML);
        verify(sefazTransmitter, never()).transmit(any());
    }

    @Test
    void shouldNotTransmitWhenSigningFails() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);
        doThrow(new IllegalStateException("signing failed")).when(xmlSigner).sign(any());

        assertThrows(IllegalStateException.class, () -> signingService.issue(emitRequest()));
        verify(xmlValidator, never()).validateNFe(any());
        verify(sefazTransmitter, never()).transmit(any());
    }

    @Test
    void shouldSucceedXmlOnlyWithoutSigner() {
        when(xmlGenerator.generate(any())).thenReturn(GENERATED_XML);

        NfeEmissionResponse response = service.issue(xmlOnlyRequest());

        assertEquals(ProcessingStatus.XML_GENERATED, response.status());
        assertEquals(GENERATED_XML, response.xml());
    }

    @Test
    void shouldMapImportRequestToDomain() {
        NfeEmission emission = service.toDomain("emission-1", importRequest());

        assertEquals("emission-1", emission.emissionId());
        assertEquals("REF-001", emission.externalReference());
        assertEquals(OperationType.IMPORT, emission.operationType());
        assertEquals(EmissionStatus.RECEIVED, emission.status());
        assertEquals("Acme Ltd", emission.issuer().name());
        assertEquals("12345678000199", emission.issuer().document());
        assertNull(emission.issuer().stateRegistration());
        assertNull(emission.issuer().address());
        assertEquals("Beta Corp", emission.recipient().name());
        assertEquals("98765432000188", emission.recipient().document());
        assertNull(emission.recipient().address());
        assertEquals(1, emission.items().size());

        NfeItem item = emission.items().get(0);
        assertEquals("P-1", item.productCode());
        assertEquals("Widget", item.description());
        assertEquals("84818090", item.ncm());
        assertEquals("UN", item.unit());
        assertEquals(new BigDecimal("1"), item.quantity());
        assertEquals(new BigDecimal("10.50"), item.unitValue());
        assertEquals(new BigDecimal("10.50"), item.totalValue());
        assertEquals("5102", item.cfop());
        assertEquals("0", item.origin());
        assertNull(item.taxation());

        assertNotNull(emission.importDetails());
        assertEquals("DI-123", emission.importDetails().declarationNumber());
        assertEquals(LocalDate.of(2026, 1, 15), emission.importDetails().declarationDate());
        assertEquals("Porto de Santos", emission.importDetails().clearancePlace());
        assertNull(emission.exportDetails());
        assertEquals(new BigDecimal("10.50"), emission.totals().itemsTotal());
        assertEquals(new BigDecimal("10.50"), emission.totals().totalValue());
        assertNull(emission.taxTotals());
        assertNull(emission.transport());
    }

    @Test
    void shouldMapExportRequestToDomain() {
        NfeEmission emission = service.toDomain("emission-2", exportRequest());

        assertEquals(OperationType.EXPORT, emission.operationType());
        assertNotNull(emission.exportDetails());
        assertEquals("United States", emission.exportDetails().destinationCountry());
        assertNull(emission.importDetails());
        assertEquals(new BigDecimal("10.50"), emission.totals().itemsTotal());
        assertEquals(new BigDecimal("10.50"), emission.totals().totalValue());
        assertNull(emission.taxTotals());
        assertNull(emission.transport());
    }

    @Test
    void shouldMapTransferRequestToDomain() {
        NfeEmission emission = service.toDomain("emission-3", transferRequest());

        assertEquals(OperationType.TRANSFER, emission.operationType());
        assertNull(emission.importDetails());
        assertNull(emission.exportDetails());
    }

    @Test
    void shouldMapHeaderFieldsToDomain() {
        NfeEmission emission = service.toDomain("emission-4", requestWithHeader());

        assertEquals("55", emission.model());
        assertEquals(3, emission.series());
        assertEquals(1000L, emission.number());
        assertEquals(OffsetDateTime.parse("2026-09-21T10:30:00-03:00"), emission.emissionDate());
        assertEquals("Remessa para industrialização", emission.operationDescription());
        assertEquals("3550308", emission.fiscalEstablishmentCity());
    }

    @Test
    void shouldAllowHeaderFieldsToBeNull() {
        NfeEmission emission = service.toDomain("emission-5", transferRequest());

        assertNull(emission.model());
        assertNull(emission.series());
        assertNull(emission.number());
        assertNull(emission.emissionDate());
        assertNull(emission.operationDescription());
        assertNull(emission.fiscalEstablishmentCity());
    }

    @Test
    void shouldMapIdentificationFieldsToDomain() {
        NfeEmission emission = service.toDomain("emission-7", requestWithIdentification());

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
    void shouldAllowIdentificationFieldsToBeNull() {
        NfeEmission emission = service.toDomain("emission-8", transferRequest());

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

    @Test
    void shouldMapIssuerTradeNameAndTaxRegimeToDomain() {
        NfeEmission emission = service.toDomain("emission-9", requestWithIssuerIdentity());

        assertEquals("Acme Comercio Ltda", emission.issuer().tradeName());
        assertEquals("3", emission.issuer().taxRegime());
    }

    @Test
    void shouldAllowIssuerTradeNameAndTaxRegimeToBeNull() {
        NfeEmission emission = service.toDomain("emission-10", transferRequest());

        assertNull(emission.issuer().tradeName());
        assertNull(emission.issuer().taxRegime());
    }

    @Test
    void shouldMapRecipientIeStatusToDomain() {
        NfeEmission emission = service.toDomain("emission-11", requestWithRecipientIeStatus());

        assertEquals(RecipientIeStatus.EXEMPT, emission.recipient().ieStatus());
    }

    @Test
    void shouldAllowRecipientIeStatusToBeNull() {
        NfeEmission emission = service.toDomain("emission-12", transferRequest());

        assertNull(emission.recipient().ieStatus());
    }

    @Test
    void shouldMapIssuerAddressToDomain() {
        NfeEmission emission = service.toDomain("emission-15", requestWithAddresses());

        Address address = emission.issuer().address();
        assertNotNull(address);
        assertEquals("Main St", address.street());
        assertEquals("100", address.number());
        assertEquals("Sao Paulo", address.city());
        assertEquals("SP", address.state());
        assertEquals("01310100", address.zipCode());
        assertEquals("Centro", address.neighborhood());
        assertEquals("3550308", address.municipalityCode());
    }

    @Test
    void shouldMapRecipientAddressToDomain() {
        NfeEmission emission = service.toDomain("emission-16", requestWithAddresses());

        Address address = emission.recipient().address();
        assertNotNull(address);
        assertEquals("Rua B", address.street());
        assertEquals("200", address.number());
        assertEquals("Sao Paulo", address.city());
        assertEquals("SP", address.state());
        assertEquals("01310200", address.zipCode());
        assertEquals("Centro", address.neighborhood());
        assertEquals("3550308", address.municipalityCode());
    }

    @Test
    void shouldKeepNullAddressesNullWithoutSynthesis() {
        NfeEmission emission = service.toDomain("emission-17", transferRequest());

        assertNull(emission.issuer().address());
        assertNull(emission.recipient().address());
    }

    @Test
    void shouldMapProductTributaryFieldsToDomain() {
        NfeEmission emission = service.toDomain("emission-18", requestWithProductTributaryFields());

        NfeItem item = emission.items().get(0);
        assertEquals("7891234567895", item.cEan());
        assertEquals("7899876543210", item.cEanTrib());
        assertEquals("UN", item.tributaryUnit());
        assertEquals(new BigDecimal("1"), item.tributaryQuantity());
        assertEquals(new BigDecimal("10.50"), item.tributaryUnitValue());
        assertEquals(Boolean.TRUE, item.indTot());
    }

    @Test
    void shouldKeepOmittedProductTributaryFieldsNull() {
        NfeEmission emission = service.toDomain("emission-19", transferRequest());

        NfeItem item = emission.items().get(0);
        assertNull(item.cEan());
        assertNull(item.cEanTrib());
        assertNull(item.tributaryUnit());
        assertNull(item.tributaryQuantity());
        assertNull(item.tributaryUnitValue());
        assertNull(item.indTot());
    }

    @Test
    void shouldMapItemTaxationToDomain() {
        ItemTaxation expected = itemTaxation();
        NfeEmission emission = service.toDomain("emission-20", requestWithItemTaxation());

        NfeItem item = emission.items().get(0);
        assertNotNull(item.taxation());
        assertEquals(expected, item.taxation(),
                "taxation must pass through without transformation");
        assertEquals("00", item.taxation().icms().cst());
        assertEquals("3", item.taxation().icms().modBc());
        assertEquals(new BigDecimal("1.89"), item.taxation().icms().taxAmount());
        assertNotNull(item.taxation().icms().st());
        assertEquals("50", item.taxation().ipi().cst());
        assertEquals("01", item.taxation().pisCofins().pisCst());
        assertEquals("01", item.taxation().pisCofins().cofinsCst());
        assertEquals(new BigDecimal("1.05"), item.taxation().importTax().taxAmount());
        assertEquals("1", item.taxation().is().cst());
        assertEquals("1", item.taxation().rtc().cst());
        assertEquals(new BigDecimal("0.95"), item.taxation().rtc().ibsCbs().ibsAmount());
    }

    @Test
    void shouldKeepOmittedTaxationNull() {
        NfeEmission emission = service.toDomain("emission-21", transferRequest());

        assertNull(emission.items().get(0).taxation());
    }

    @Test
    void shouldMapPaymentToDomain() {
        NfeEmission emission = service.toDomain("emission-13", requestWithPayment());

        assertNotNull(emission.payment());
        assertEquals(2, emission.payment().details().size());
        PaymentDetail first = emission.payment().details().get(0);
        assertEquals("17", first.paymentMethod());
        assertEquals(new BigDecimal("9.99"), first.amount());
        assertEquals(PaymentIndicator.IMMEDIATE, first.indicator());
        assertEquals(LocalDate.of(2026, 9, 21), first.paymentDate());
        assertEquals("01", emission.payment().details().get(1).paymentMethod());
    }

    @Test
    void shouldAllowPaymentToBeNull() {
        NfeEmission emission = service.toDomain("emission-14", transferRequest());

        assertNull(emission.payment());
    }

    @Test
    void shouldPreserveEmissionDateOffset() {
        OffsetDateTime expected = OffsetDateTime.parse("2026-09-21T10:30:00-03:00");

        NfeEmission emission = service.toDomain("emission-6", requestWithHeader());

        assertEquals(expected, emission.emissionDate());
        assertEquals(expected.getOffset(), emission.emissionDate().getOffset());
    }

    @Test
    void shouldRejectImportRequestWithoutImportDetails() {
        NfeEmissionRequest request = new NfeEmissionRequest(
                "REF-001", OperationType.IMPORT, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(), recipient(), items(), null, null, null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.issue(request));

        assertTrue(exception.getMessage().contains("importDetails"));
    }

    private static NfeEmissionRequest importRequest() {
        return new NfeEmissionRequest(
                "REF-001",
                OperationType.IMPORT,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                items(),
                new ImportDetailsDto("DI-123", LocalDate.of(2026, 1, 15), "Porto de Santos"),
                null,
                null,
                null);
    }

    private static NfeEmissionRequest exportRequest() {
        return new NfeEmissionRequest(
                "REF-002",
                OperationType.EXPORT,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                items(),
                null,
                new ExportDetailsDto("United States"),
                null,
                null);
    }

    private static NfeEmissionRequest transferRequest() {
        return new NfeEmissionRequest(
                "REF-003",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                items(),
                null,
                null,
                null,
                null);
    }

    private static NfeEmissionRequest requestWithHeader() {
        return new NfeEmissionRequest(
                "REF-004",
                OperationType.TRANSFER,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrialização",
                "3550308",
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                items(),
                null,
                null,
                null,
                null);
    }

    private static NfeEmissionRequest requestWithIdentification() {
        return new NfeEmissionRequest(
                "REF-005",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                "35", "00000001", "1", "1", "1", "1", "0", "2", "1", "1", "1", "0", "1.0",
                issuer(),
                recipient(),
                items(),
                null,
                null,
                null,
                null);
    }

    private static NfeEmissionRequest requestWithIssuerIdentity() {
        return new NfeEmissionRequest(
                "REF-006",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                new IssuerDto("Acme Ltd", "12345678000199", "Acme Comercio Ltda", "3"),
                recipient(),
                items(),
                null,
                null,
                null,
                null);
    }

    private static NfeEmissionRequest requestWithRecipientIeStatus() {
        return new NfeEmissionRequest(
                "REF-007",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                new RecipientDto("Beta Corp", "98765432000188", RecipientIeStatus.EXEMPT),
                items(),
                null,
                null,
                null,
                null);
    }

    private static NfeEmissionRequest requestWithPayment() {
        return new NfeEmissionRequest(
                "REF-008",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                items(),
                null,
                null,
                new PaymentDto(List.of(
                        new PaymentDetailDto("17", new BigDecimal("9.99"),
                                PaymentIndicator.IMMEDIATE, LocalDate.of(2026, 9, 21)),
                        new PaymentDetailDto("01", new BigDecimal("0.61"), null, null))),
                null);
    }

    private static NfeEmissionRequest requestWithAddresses() {
        return new NfeEmissionRequest(
                "REF-011",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                new IssuerDto("Acme Ltd", "12345678000199", "Acme Comercio Ltda", "3",
                        new Address("Main St", "100", "Sao Paulo", "SP", "01310100", "Centro", "3550308")),
                new RecipientDto("Beta Corp", "98765432000188", RecipientIeStatus.EXEMPT,
                        new Address("Rua B", "200", "Sao Paulo", "SP", "01310200", "Centro", "3550308")),
                items(),
                null,
                null,
                null,
                null);
    }

    private static NfeEmissionRequest requestWithProductTributaryFields() {
        return new NfeEmissionRequest(
                "REF-012",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                List.of(new ItemDto(
                        "P-1",
                        "Widget",
                        "84818090",
                        "7891234567895",
                        "7899876543210",
                        "UN",
                        new BigDecimal("1"),
                        new BigDecimal("10.50"),
                        new BigDecimal("10.50"),
                        "UN",
                        new BigDecimal("1"),
                        new BigDecimal("10.50"),
                        "5102",
                        "0",
                        true,
                        null)),
                null,
                null,
                null,
                null);
    }

    private static NfeEmissionRequest requestWithItemTaxation() {
        return new NfeEmissionRequest(
                "REF-013",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                List.of(new ItemDto(
                        "P-1",
                        "Widget",
                        "84818090",
                        "7891234567895",
                        "7899876543210",
                        "UN",
                        new BigDecimal("1"),
                        new BigDecimal("10.50"),
                        new BigDecimal("10.50"),
                        "UN",
                        new BigDecimal("1"),
                        new BigDecimal("10.50"),
                        "5102",
                        "0",
                        true,
                        itemTaxation())),
                null,
                null,
                null,
                null);
    }

    private static ItemTaxation itemTaxation() {
        return new ItemTaxation(
                new IcmsTax("00", "3",
                        new BigDecimal("10.50"), new BigDecimal("18.00"), new BigDecimal("1.89"),
                        new BigDecimal("2.00"), new BigDecimal("0.21"),
                        new IcmsStTax("3", new BigDecimal("10.00"), new BigDecimal("0.00"),
                                new BigDecimal("10.50"), new BigDecimal("18.00"),
                                new BigDecimal("1.89"), new BigDecimal("10.50"),
                                new BigDecimal("2.00"), new BigDecimal("0.21")),
                        null, null),
                new IpiTax("50", new BigDecimal("10.50"), new BigDecimal("5.00"), new BigDecimal("0.53")),
                new PisCofinsTax("01", new BigDecimal("10.50"), new BigDecimal("1.65"), new BigDecimal("0.17"),
                        "01", new BigDecimal("10.50"), new BigDecimal("7.60"), new BigDecimal("0.80")),
                new ImportTax(new BigDecimal("10.50"), new BigDecimal("10.00"), new BigDecimal("1.05"),
                        new BigDecimal("0.10"), new BigDecimal("0.05")),
                new IsTax("1", "01", new BigDecimal("10.50"), new BigDecimal("5.00"), null,
                        "UN", new BigDecimal("1"), new BigDecimal("0.53")),
                new RtcTaxation("1", "01", "0",
                        new IbsCbsTaxation(new BigDecimal("10.50"),
                                new IbsUfTax(new BigDecimal("8.80"), null, null, null, new BigDecimal("0.92")),
                                new IbsMunicipalityTax(new BigDecimal("0.20"), null, null, null, new BigDecimal("0.02")),
                                new BigDecimal("0.95"), null, null)));
    }

    private static NfeEmissionRequest xmlOnlyRequest() {
        return new NfeEmissionRequest(
                "REF-009",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                items(),
                null,
                null,
                null,
                ProcessingMode.XML_ONLY);
    }

    private static NfeEmissionRequest emitRequest() {
        return new NfeEmissionRequest(
                "REF-010",
                OperationType.TRANSFER,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                items(),
                null,
                null,
                null,
                ProcessingMode.EMIT);
    }

    private static IssuerDto issuer() {
        return new IssuerDto("Acme Ltd", "12345678000199", null, null);
    }

    private static RecipientDto recipient() {
        return new RecipientDto("Beta Corp", "98765432000188", null);
    }

    private static List<ItemDto> items() {
        return List.of(new ItemDto(
                "P-1",
                "Widget",
                "84818090",
                "UN",
                new BigDecimal("1"),
                new BigDecimal("10.50"),
                new BigDecimal("10.50"),
                "5102",
                "0"));
    }
}
