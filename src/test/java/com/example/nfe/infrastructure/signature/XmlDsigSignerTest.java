package com.example.nfe.infrastructure.signature;

import com.example.nfe.domain.Address;
import com.example.nfe.domain.EmissionStatus;
import com.example.nfe.domain.IcmsTax;
import com.example.nfe.domain.IcmsTotals;
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
import com.example.nfe.domain.TaxTotals;
import com.example.nfe.domain.Totals;
import com.example.nfe.domain.Transport;
import com.example.nfe.infrastructure.xml.NfeXmlGenerator;
import com.example.nfe.infrastructure.xml.NfeXmlMapper;
import com.example.nfe.infrastructure.xml.validation.NfeXmlValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real cryptographic signing tests: a test-only self-signed certificate is
 * generated at runtime (never committed, never production). Verification is
 * done with the standard XMLDSig API against the certificate public key —
 * not by inspecting SignatureValue contents.
 */
class XmlDsigSignerTest {

    private static final String NF_NAMESPACE = "http://www.portalfiscal.inf.br/nfe";
    private static final String DS_NAMESPACE = "http://www.w3.org/2000/09/xmldsig#";
    private static final String EXPECTED_ID = "NFe35260912345678000199550030000010001000000018";

    private static SigningCredentials testCredentials;
    private static String unsignedXml;

    @BeforeAll
    static void setUp() throws Exception {
        testCredentials = SelfSignedTestCertificate.load();
        unsignedXml = new NfeXmlGenerator(new NfeXmlMapper()).generate(fullEmission());
    }

    @Test
    void shouldProduceCryptographicallyValidSignature() throws Exception {
        String signedXml = new XmlDsigSigner(testCredentials).sign(unsignedXml);

        assertTrue(verify(signedXml, testCredentials), "signature must verify with the certificate public key");
    }

    @Test
    void shouldReferenceTheActualInfNfeId() throws Exception {
        String signedXml = new XmlDsigSigner(testCredentials).sign(unsignedXml);
        Document document = parse(signedXml);

        String uri = xpath(document, "/*/*[2]/*[1]/*[3]/@URI");
        assertEquals("#" + EXPECTED_ID, uri);

        assertEquals(EXPECTED_ID, xpath(document, "/*/*[1]/@Id"));
    }

    @Test
    void shouldPlaceSignatureAsLastChildOfNfeInDsNamespace() throws Exception {
        String signedXml = new XmlDsigSigner(testCredentials).sign(unsignedXml);
        Document document = parse(signedXml);

        assertEquals("Signature", xpath(document, "local-name(/*/*[2])"));
        assertEquals(DS_NAMESPACE, xpath(document, "namespace-uri(/*/*[2])"));
        assertEquals(2.0, xpathNumber(document, "count(/*/*)"));
    }

    @Test
    void shouldGenerateDigestSignatureAndCertificate() throws Exception {
        String signedXml = new XmlDsigSigner(testCredentials).sign(unsignedXml);
        Document document = parse(signedXml);

        String digestValue = xpath(document, "/*/*[2]/*[1]/*[3]/*[3]");
        String signatureValue = xpath(document, "/*/*[2]/*[2]");
        String certificate = xpath(document, "/*/*[2]/*[3]/*[1]/*[1]");
        assertNotNull(digestValue, "DigestValue must be generated");
        assertNotNull(signatureValue, "SignatureValue must be generated");
        assertNotNull(certificate, "X509Certificate must be present");
        assertFalse(digestValue.isBlank());
        assertFalse(signatureValue.isBlank());
        assertFalse(certificate.isBlank());
    }

    @Test
    void shouldFailVerificationAfterTampering() throws Exception {
        String signedXml = new XmlDsigSigner(testCredentials).sign(unsignedXml);
        Document document = parse(signedXml);

        // Tamper with a signed value: the issuer name.
        Element xNome = (Element) document.getElementsByTagNameNS(NF_NAMESPACE, "xNome").item(0);
        xNome.setTextContent("Tampered Issuer");

        assertFalse(verify(serialize(document), testCredentials),
                "verification must fail after the signed content is modified");
    }

    @Test
    void shouldValidateSignedXmlAgainstOfficialXsd() throws Exception {
        String signedXml = new XmlDsigSigner(testCredentials).sign(unsignedXml);

        new NfeXmlValidator().validateNFe(signedXml);
    }

    @Test
    void shouldRejectMissingCredentialsExplicitly() {
        assertThrows(IllegalArgumentException.class, () -> new XmlDsigSigner(null));
        assertThrows(IllegalArgumentException.class,
                () -> new SigningCredentials(null, null));
    }

    @Test
    void shouldFailExplicitlyWhenInfNfeIdIsMissing() {
        String xmlWithoutId = unsignedXml.replaceFirst(" Id=\"[^\"]*\"", "");
        XmlDsigSigner signer = new XmlDsigSigner(testCredentials);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> signer.sign(xmlWithoutId));
        assertTrue(exception.getMessage().contains("Id"));
    }

    private static NfeEmission fullEmission() {
        Issuer issuer = new Issuer("Acme Ltd", "12345678000199", "123456789",
                new Address("Main St", "100", "Sao Paulo", "SP", "01310100", "Centro", "3550308"),
                "Acme Comercio Ltda", "3");
        Recipient recipient = new Recipient("Beta Corp", "98765432000188",
                new Address("Rua B", "200", "Sao Paulo", "SP", "01310200", "Centro", "3550308"),
                RecipientIeStatus.CONTRIBUTOR);
        NfeItem item = new NfeItem(
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
        Totals totals = new Totals(
                new BigDecimal("10.50"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("10.50"));
        TaxTotals taxTotals = new TaxTotals(
                new IcmsTotals(
                        new BigDecimal("10.50"), new BigDecimal("1.89"), new BigDecimal("0.00"),
                        new BigDecimal("0.21"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00")),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("2.10"),
                null, null);
        Transport transport = new Transport("0", "12345678000199", "Acme Carrier",
                new Address("Port St", "1", "Santos", "SP", "11013000", "Centro", "3548500"));
        Payment payment = new Payment(List.of(
                new PaymentDetail("17", new BigDecimal("10.50"), PaymentIndicator.IMMEDIATE,
                        LocalDate.of(2026, 9, 21))));
        return new NfeEmission(
                "emission-1", "REF-1", OperationType.TRANSFER, EmissionStatus.RECEIVED,
                "55", 3, 1000L, OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializacao", "3550308",
                "35", "00000001", "1", "1", "1", "1", "8", "2", "1", "1", "1", "0", "1.0",
                issuer, recipient, List.of(item),
                totals, taxTotals, null, null, transport, payment);
    }

    private static boolean verify(String xml, SigningCredentials credentials) throws Exception {
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

    private static String serialize(Document document) throws Exception {
        javax.xml.transform.Transformer transformer =
                javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        java.io.StringWriter writer = new java.io.StringWriter();
        transformer.transform(new javax.xml.transform.dom.DOMSource(document),
                new javax.xml.transform.stream.StreamResult(writer));
        return writer.toString();
    }

    private static String xpath(Document document, String expression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String value = (String) xpath.evaluate(expression, document, XPathConstants.STRING);
        return value == null || value.isBlank() ? null : value;
    }

    private static double xpathNumber(Document document, String expression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Double) xpath.evaluate(expression, document, XPathConstants.NUMBER);
    }
}
