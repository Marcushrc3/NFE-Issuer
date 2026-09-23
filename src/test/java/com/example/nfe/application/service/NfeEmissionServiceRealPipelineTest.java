package com.example.nfe.application.service;

import com.example.nfe.api.dto.NfeEmissionResponse;
import com.example.nfe.application.sefaz.SefazAuthorizationResult;
import com.example.nfe.application.sefaz.SefazAuthorizationStatus;
import com.example.nfe.application.sefaz.SefazTransmissionResult;
import com.example.nfe.application.sefaz.SefazTransmissionStatus;
import com.example.nfe.application.sefaz.SefazTransmitter;
import com.example.nfe.domain.NfeEmission;
import com.example.nfe.infrastructure.signature.SelfSignedTestCertificate;
import com.example.nfe.infrastructure.signature.SigningCredentials;
import com.example.nfe.infrastructure.signature.XmlDsigSigner;
import com.example.nfe.infrastructure.signature.XmlSigner;
import com.example.nfe.infrastructure.xml.NfeXmlGenerator;
import com.example.nfe.infrastructure.xml.NfeXmlMapper;
import com.example.nfe.infrastructure.xml.validation.NfeXmlValidationException;
import com.example.nfe.infrastructure.xml.validation.NfeXmlValidator;
import com.example.nfe.testfixtures.NfeFixtures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Real-component pipeline tests: the REAL generator, validator and signer
 * (with the TEST-ONLY PKCS#12) are wired into {@link NfeEmissionService};
 * only the SEFAZ transmitter is mocked. These tests prove the corrected
 * validation/signing order end-to-end — mock-only service tests cannot.
 */
class NfeEmissionServiceRealPipelineTest {

    private static final String NF_NAMESPACE = "http://www.portalfiscal.inf.br/nfe";
    private static final String DS_NAMESPACE = "http://www.w3.org/2000/09/xmldsig#";

    private static final NfeXmlGenerator GENERATOR = new NfeXmlGenerator(new NfeXmlMapper());
    private static final NfeXmlValidator VALIDATOR = new NfeXmlValidator();

    private static final SefazTransmissionResult AUTHORIZED_RESULT = new SefazTransmissionResult(
            SefazTransmissionStatus.AUTHORIZED,
            103,
            "Lote recebido com sucesso",
            null,
            new SefazAuthorizationResult(
                    SefazAuthorizationStatus.AUTHORIZED,
                    100,
                    "Autorizado o uso da NF-e",
                    "135260000000123",
                    "35260912345678000199550030000010001000000018",
                    "dGVzdA=="),
            "<raw/>");

    private static SigningCredentials testCredentials;

    private final SefazTransmitter transmitter = mock(SefazTransmitter.class);

    @BeforeAll
    static void loadTestCredentials() throws Exception {
        testCredentials = SelfSignedTestCertificate.load();
    }

    @Test
    void xmlOnlyRealPipelineSucceedsWithoutSignerOrTransmitter() {
        NfeEmissionService service =
                new NfeEmissionService(GENERATOR, VALIDATOR, Optional.empty(), Optional.empty());

        NfeEmissionResponse response =
                service.issue("emission-1", NfeFixtures.fullEmission(), ProcessingMode.XML_ONLY);

        assertEquals(ProcessingStatus.XML_GENERATED, response.status());
        assertNotNull(response.xml());
        assertFalse(response.xml().contains("Signature"),
                "XML_ONLY must return an unsigned document");
        assertNull(response.transmission());
    }

    @Test
    void xmlOnlyInvalidInfNfeFailsWithoutSigning() {
        XmlSigner signer = mock(XmlSigner.class);
        NfeEmissionService service =
                new NfeEmissionService(GENERATOR, VALIDATOR, Optional.of(signer), Optional.empty());

        assertThrows(NfeXmlValidationException.class,
                () -> service.issue("emission-2", NfeFixtures.invalidInfNfeEmission(), ProcessingMode.XML_ONLY));
        verifyNoInteractions(signer);
    }

    @Test
    void emitRealPipelineSignsValidatesAndTransmitsExactSignedXml() throws Exception {
        NfeEmissionService service = new NfeEmissionService(GENERATOR, VALIDATOR,
                Optional.of(new XmlDsigSigner(testCredentials)), Optional.of(transmitter));
        when(transmitter.transmit(any())).thenReturn(AUTHORIZED_RESULT);

        NfeEmissionResponse response =
                service.issue("emission-3", NfeFixtures.fullEmission(), ProcessingMode.EMIT);

        assertEquals(ProcessingStatus.AUTHORIZED, response.status());
        String signedXml = response.xml();
        assertTrue(signedXml.contains("Signature"), "EMIT must return a signed document");
        assertTrue(verifySignature(signedXml, testCredentials),
                "signature must verify with the test certificate public key");
        VALIDATOR.validateNFe(signedXml);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(transmitter).transmit(captor.capture());
        assertEquals(signedXml, captor.getValue(),
                "the transmitter must receive exactly the validated signed XML");
    }

    @Test
    void emitDoesNotTransmitWhenSignedXmlFailsValidation() {
        NfeEmissionService service = new NfeEmissionService(GENERATOR, VALIDATOR,
                Optional.of(new XmlDsigSigner(testCredentials)), Optional.of(transmitter));

        assertThrows(NfeXmlValidationException.class,
                () -> service.issue("emission-4", NfeFixtures.signableButInvalidNfeEmission(), ProcessingMode.EMIT));
        verify(transmitter, never()).transmit(any());
    }

    @Test
    void emitSigningFailureNeverValidatesOrTransmits() {
        XmlSigner failingSigner = mock(XmlSigner.class);
        NfeXmlValidator validator = mock(NfeXmlValidator.class);
        when(failingSigner.sign(any())).thenThrow(new IllegalStateException("signing failed"));
        NfeEmissionService service = new NfeEmissionService(GENERATOR, validator,
                Optional.of(failingSigner), Optional.of(transmitter));

        assertThrows(IllegalStateException.class,
                () -> service.issue("emission-5", NfeFixtures.fullEmission(), ProcessingMode.EMIT));
        verify(validator, never()).validateNFe(any());
        verify(transmitter, never()).transmit(any());
    }

    @Test
    void emitNeverRegeneratesTheSignedXml() {
        // The exact signed XML returned to the caller must be the one
        // transmitted: no regeneration, no re-signing, no modification.
        NfeEmissionService service = new NfeEmissionService(GENERATOR, VALIDATOR,
                Optional.of(new XmlDsigSigner(testCredentials)), Optional.of(transmitter));
        when(transmitter.transmit(any())).thenReturn(AUTHORIZED_RESULT);

        NfeEmissionResponse response =
                service.issue("emission-6", NfeFixtures.fullEmission(), ProcessingMode.EMIT);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(transmitter).transmit(captor.capture());
        assertEquals(response.xml(), captor.getValue());
    }

    private static boolean verifySignature(String xml, SigningCredentials credentials) throws Exception {
        Document document = parse(xml);
        Element infNfe = (Element) document.getElementsByTagNameNS(NF_NAMESPACE, "infNFe").item(0);
        infNfe.setIdAttribute("Id", true);
        Node signatureNode = document.getElementsByTagNameNS(DS_NAMESPACE, "Signature").item(0);
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        DOMValidateContext validateContext =
                new DOMValidateContext(credentials.certificate().getPublicKey(), signatureNode);
        // The official NF-e standard mandates rsa-sha1/sha1, which the JDK
        // secure-validation policy rejects by default. Disable it here —
        // verification-only, test-scoped.
        validateContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);
        XMLSignature signature = factory.unmarshalXMLSignature(validateContext);
        return signature.validate(validateContext);
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }
}
