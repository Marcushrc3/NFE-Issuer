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
import com.example.nfe.application.accesskey.NfeAccessKeyGenerator;
import com.example.nfe.application.sefaz.SefazAuthorizationResult;
import com.example.nfe.application.sefaz.SefazNotConfiguredException;
import com.example.nfe.application.sefaz.SefazTransmissionResult;
import com.example.nfe.application.sefaz.SefazTransmitter;
import com.example.nfe.domain.EmissionStatus;
import com.example.nfe.domain.ExportDetails;
import com.example.nfe.domain.ImportDetails;
import com.example.nfe.domain.Issuer;
import com.example.nfe.domain.NfeEmission;
import com.example.nfe.domain.NfeItem;
import com.example.nfe.domain.Payment;
import com.example.nfe.domain.PaymentDetail;
import com.example.nfe.domain.Recipient;
import com.example.nfe.domain.Totals;
import com.example.nfe.infrastructure.signature.XmlSigner;
import com.example.nfe.infrastructure.xml.NfeXmlGenerator;
import com.example.nfe.infrastructure.xml.validation.NfeXmlValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NfeEmissionService {

    private static final String XML_ONLY_MESSAGE = "NF-e XML generated and XSD-validated";
    private static final String EMIT_MESSAGE =
            "NF-e signed and submitted to the SEFAZ transmission boundary";

    private final NfeXmlGenerator xmlGenerator;
    private final NfeXmlValidator xmlValidator;
    private final Optional<XmlSigner> xmlSigner;
    private final Optional<SefazTransmitter> sefazTransmitter;

    /**
     * The {@link XmlSigner} and {@link SefazTransmitter} are optional:
     * XML_ONLY must work without any signing or transmission
     * configuration. EMIT fails with a controlled error when either one
     * is missing — the application never pretends a transmission happened.
     */
    public NfeEmissionService(NfeXmlGenerator xmlGenerator,
                              NfeXmlValidator xmlValidator,
                              Optional<XmlSigner> xmlSigner,
                              Optional<SefazTransmitter> sefazTransmitter) {
        this.xmlGenerator = xmlGenerator;
        this.xmlValidator = xmlValidator;
        this.xmlSigner = xmlSigner;
        this.sefazTransmitter = sefazTransmitter;
    }

    public NfeEmissionResponse issue(NfeEmissionRequest request) {
        String emissionId = UUID.randomUUID().toString();
        NfeEmission emission = toDomain(emissionId, request);
        return issue(emissionId, emission, request.processingMode());
    }

    /**
     * Processing entry point for an already-built domain emission.
     * Package-private so real-component pipeline tests can drive the exact
     * production flow (XML_ONLY or EMIT) without the API mapping layer.
     */
    NfeEmissionResponse issue(String emissionId, NfeEmission emission, ProcessingMode processingMode) {
        NfeAccessKeyGenerator.generate(emission);
        String xml = xmlGenerator.generate(emission);

        return switch (processingMode) {
            case XML_ONLY -> {
                // Unsigned flow: the official schema only accepts an NFe
                // with a mandatory signature, so validate the infNFe payload.
                xmlValidator.validateInfNFe(xml);
                yield new NfeEmissionResponse(
                        emissionId, ProcessingMode.XML_ONLY, ProcessingStatus.XML_GENERATED,
                        XML_ONLY_MESSAGE, xml, null);
            }
            case EMIT -> {
                // EMIT: sign FIRST, then validate the exact signed document
                // against the complete official schema, then transmit it.
                String signedXml = sign(xml);
                xmlValidator.validateNFe(signedXml);
                SefazTransmissionResult result = transmit(signedXml);
                yield new NfeEmissionResponse(
                        emissionId, ProcessingMode.EMIT, toProcessingStatus(result),
                        EMIT_MESSAGE, signedXml, toTransmissionDto(result));
            }
        };
    }

    /**
     * Signs the exact XML produced by the generation step — the document is
     * never regenerated or re-marshalled after validation. Missing signer
     * configuration is a controlled application error.
     */
    private String sign(String xml) {
        XmlSigner signer = xmlSigner.orElseThrow(() -> new SigningNotConfiguredException(
                "EMIT processing mode requires signing credentials, but no XmlSigner is configured"));
        return signer.sign(xml);
    }

    /**
     * Hands the exact signed XML to the SEFAZ transmission boundary.
     * Missing transmitter configuration is a controlled application error —
     * the application never fabricates an AUTHORIZED status, protocol or
     * SEFAZ response.
     */
    private SefazTransmissionResult transmit(String signedXml) {
        SefazTransmitter transmitter = sefazTransmitter.orElseThrow(() -> new SefazNotConfiguredException(
                "EMIT processing mode requires SEFAZ transmission, but no SefazTransmitter is configured"));
        return transmitter.transmit(signedXml);
    }

    /**
     * Maps the SEFAZ transmission outcome to the API-visible processing
     * status. An ERROR transmission is a controlled application error —
     * the API never fabricates an authorization result.
     */
    private ProcessingStatus toProcessingStatus(SefazTransmissionResult result) {
        return switch (result.transmissionStatus()) {
            case AUTHORIZED -> ProcessingStatus.AUTHORIZED;
            case REJECTED -> ProcessingStatus.REJECTED;
            case PROCESSING -> ProcessingStatus.PROCESSING;
            case ERROR -> throw new SefazTransmissionFailedException(
                    result.transmissionMessage() != null
                            ? result.transmissionMessage()
                            : "SEFAZ transmission failed");
        };
    }

    /**
     * Maps the application transmission result into the API DTO. The raw
     * SOAP response is deliberately excluded — it is an infrastructure
     * diagnostic artifact and never part of the REST contract.
     */
    private SefazTransmissionResponseDto toTransmissionDto(SefazTransmissionResult result) {
        SefazAuthorizationResult authorization = result.authorizationResult();
        SefazAuthorizationResponseDto authorizationDto = authorization == null
                ? null
                : new SefazAuthorizationResponseDto(
                        authorization.status(),
                        authorization.code(),
                        authorization.message(),
                        authorization.protocol(),
                        authorization.accessKey(),
                        authorization.digestValue());
        return new SefazTransmissionResponseDto(
                result.transmissionStatus(),
                result.transmissionCode(),
                result.transmissionMessage(),
                result.receipt(),
                authorizationDto);
    }

    /**
     * Maps the API request into the domain aggregate. Operation-specific section
     * invariants are enforced by {@link NfeEmission} itself. Package-private so
     * the mapping can be verified directly in tests.
     */
    NfeEmission toDomain(String emissionId, NfeEmissionRequest request) {
        List<NfeItem> items = toItems(request.items());
        Totals totals = toTotals(items);
        return new NfeEmission(
                emissionId,
                request.externalReference(),
                request.operationType(),
                EmissionStatus.RECEIVED,
                request.model(),
                request.series(),
                request.number(),
                request.emissionDate(),
                request.operationDescription(),
                request.fiscalEstablishmentCity(),
                request.stateCode(),
                request.randomCode(),
                request.operationDirection(),
                request.destinationType(),
                request.printFormat(),
                request.emissionType(),
                request.checkDigit(),
                request.environment(),
                request.purpose(),
                request.finalConsumer(),
                request.presenceIndicator(),
                request.processType(),
                request.processVersion(),
                toIssuer(request.issuer()),
                toRecipient(request.recipient()),
                items,
                totals,
                null, // taxTotals: not yet exposed by the API
                toImportDetails(request.importDetails()),
                toExportDetails(request.exportDetails()),
                null, // transport: not yet exposed by the API
                toPayment(request.payment()));
    }

    private Totals toTotals(List<NfeItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        java.math.BigDecimal itemsTotal = items.stream()
                .map(item -> item.totalValue() == null ? java.math.BigDecimal.ZERO : item.totalValue())
                .reduce(java.math.BigDecimal.ZERO,
                        (accumulator, value) -> accumulator.add(value == null
                                ? java.math.BigDecimal.ZERO
                                : value));
        return new Totals(itemsTotal, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, itemsTotal);
    }

    private Payment toPayment(PaymentDto dto) {
        if (dto == null) {
            return null;
        }
        return new Payment(dto.details().stream()
                .map(this::toPaymentDetail)
                .toList());
    }

    private PaymentDetail toPaymentDetail(PaymentDetailDto dto) {
        return new PaymentDetail(dto.paymentMethod(), dto.amount(), dto.indicator(), dto.paymentDate());
    }

    private Issuer toIssuer(IssuerDto dto) {
        return new Issuer(dto.name(), dto.document(), null, dto.address(),
                dto.tradeName(), dto.taxRegime());
    }

    private Recipient toRecipient(RecipientDto dto) {
        return new Recipient(dto.name(), dto.document(), dto.address(), dto.ieStatus());
    }

    private List<NfeItem> toItems(List<ItemDto> items) {
        return items.stream()
                .map(this::toItem)
                .toList();
    }

    private NfeItem toItem(ItemDto item) {
        return new NfeItem(
                item.productCode(),
                item.description(),
                item.ncm(),
                item.cEan(),
                item.cEanTrib(),
                null, // cest: not yet exposed by the API
                item.unit(),
                item.quantity(),
                item.unitValue(),
                item.totalValue(),
                item.tributaryUnit(),
                item.tributaryQuantity(),
                item.tributaryUnitValue(),
                null, // freight: not yet exposed by the API
                null, // insurance: not yet exposed by the API
                null, // discount: not yet exposed by the API
                null, // otherCharges: not yet exposed by the API
                item.cfop(),
                item.origin(),
                item.indTot(),
                null, // xPed: not yet exposed by the API
                null, // nItemPed: not yet exposed by the API
                item.taxation(),
                null); // importDetails: not yet exposed by the API
    }

    private ImportDetails toImportDetails(ImportDetailsDto dto) {
        return dto == null
                ? null
                : new ImportDetails(dto.declarationNumber(), dto.declarationDate(), dto.clearancePlace());
    }

    private ExportDetails toExportDetails(ExportDetailsDto dto) {
        return dto == null
                ? null
                : new ExportDetails(dto.destinationCountry(), null, null, null);
    }
}
