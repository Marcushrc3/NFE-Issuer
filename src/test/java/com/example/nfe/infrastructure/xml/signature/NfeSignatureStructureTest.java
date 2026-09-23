package com.example.nfe.infrastructure.xml.signature;

import com.example.nfe.infrastructure.xml.InfNfe;
import com.example.nfe.infrastructure.xml.Nfe;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Structural tests for the XMLDSig JAXB foundation. The placeholder byte
 * values used here are explicitly NOT cryptographic values — these tests
 * pin down element names, namespaces and order only. Real signature
 * creation is the responsibility of a future {@code XmlSigner}
 * implementation.
 */
class NfeSignatureStructureTest {

    @Test
    void shouldMarshalSignatureAfterInfNfeInDsNamespace() throws Exception {
        Nfe nfe = new Nfe();
        InfNfe infNfe = new InfNfe();
        infNfe.setVersao("4.00");
        infNfe.setId("NFe35260912345678000199550030000010001000000018");
        nfe.setInfNfe(infNfe);
        nfe.setSignature(structuralSignature());

        Document document = parse(marshal(nfe));
        XPath xpath = XPathFactory.newInstance().newXPath();

        assertEquals("Signature", xpath.evaluate("local-name(/*/*[2])", document, XPathConstants.STRING));
        assertEquals("http://www.w3.org/2000/09/xmldsig#",
                xpath.evaluate("namespace-uri(/*/*[2])", document, XPathConstants.STRING));
        assertEquals("SignedInfo", xpath.evaluate("local-name(/*/*[2]/*[1])", document, XPathConstants.STRING));
        assertEquals("SignatureValue", xpath.evaluate("local-name(/*/*[2]/*[2])", document, XPathConstants.STRING));
        assertEquals("KeyInfo", xpath.evaluate("local-name(/*/*[2]/*[3])", document, XPathConstants.STRING));
    }

    @Test
    void shouldMarshalSignedInfoAndReferenceStructure() throws Exception {
        Nfe nfe = new Nfe();
        nfe.setInfNfe(new InfNfe());
        nfe.setSignature(structuralSignature());

        Document document = parse(marshal(nfe));
        XPath xpath = XPathFactory.newInstance().newXPath();

        assertEquals("CanonicalizationMethod",
                xpath.evaluate("local-name(/*/*[2]/*[1]/*[1])", document, XPathConstants.STRING));
        assertEquals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
                xpath.evaluate("/*/*[2]/*[1]/*[1]/@Algorithm", document, XPathConstants.STRING));
        assertEquals("SignatureMethod",
                xpath.evaluate("local-name(/*/*[2]/*[1]/*[2])", document, XPathConstants.STRING));
        assertEquals("Reference",
                xpath.evaluate("local-name(/*/*[2]/*[1]/*[3])", document, XPathConstants.STRING));
        assertEquals("#NFe35260912345678000199550030000010001000000018",
                xpath.evaluate("/*/*[2]/*[1]/*[3]/@URI", document, XPathConstants.STRING));
        assertEquals("Transforms",
                xpath.evaluate("local-name(/*/*[2]/*[1]/*[3]/*[1])", document, XPathConstants.STRING));
        assertEquals("DigestMethod",
                xpath.evaluate("local-name(/*/*[2]/*[1]/*[3]/*[2])", document, XPathConstants.STRING));
        assertEquals("DigestValue",
                xpath.evaluate("local-name(/*/*[2]/*[1]/*[3]/*[3])", document, XPathConstants.STRING));
        assertEquals("X509Data",
                xpath.evaluate("local-name(/*/*[2]/*[3]/*[1])", document, XPathConstants.STRING));
        assertEquals("X509Certificate",
                xpath.evaluate("local-name(/*/*[2]/*[3]/*[1]/*[1])", document, XPathConstants.STRING));
    }

    @Test
    void shouldMarshalTransformsInOrder() throws Exception {
        Nfe nfe = new Nfe();
        nfe.setInfNfe(new InfNfe());
        nfe.setSignature(structuralSignature());

        Document document = parse(marshal(nfe));
        XPath xpath = XPathFactory.newInstance().newXPath();

        assertEquals(2.0, xpath.evaluate("count(/*/*[2]/*[1]/*[3]/*[1]/*)",
                document, XPathConstants.NUMBER));
        assertEquals("http://www.w3.org/2000/09/xmldsig#enveloped-signature",
                xpath.evaluate("/*/*[2]/*[1]/*[3]/*[1]/*[1]/@Algorithm", document, XPathConstants.STRING));
        assertEquals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
                xpath.evaluate("/*/*[2]/*[1]/*[3]/*[1]/*[2]/@Algorithm", document, XPathConstants.STRING));
    }

    private static Signature structuralSignature() {
        CanonicalizationMethod canonicalization = new CanonicalizationMethod();
        canonicalization.setAlgorithm("http://www.w3.org/TR/2001/REC-xml-c14n-20010315");

        SignatureMethod signatureMethod = new SignatureMethod();
        signatureMethod.setAlgorithm("http://www.w3.org/2000/09/xmldsig#rsa-sha1");

        Transforms transforms = new Transforms();
        Transform enveloped = new Transform();
        enveloped.setAlgorithm("http://www.w3.org/2000/09/xmldsig#enveloped-signature");
        transforms.getTransforms().add(enveloped);
        Transform c14n = new Transform();
        c14n.setAlgorithm("http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
        transforms.getTransforms().add(c14n);

        DigestMethod digestMethod = new DigestMethod();
        digestMethod.setAlgorithm("http://www.w3.org/2000/09/xmldsig#sha1");

        Reference reference = new Reference();
        reference.setUri("#NFe35260912345678000199550030000010001000000018");
        reference.setTransforms(transforms);
        reference.setDigestMethod(digestMethod);
        reference.setDigestValue(new byte[]{1, 2, 3}); // structural placeholder — NOT a real digest

        SignedInfo signedInfo = new SignedInfo();
        signedInfo.setCanonicalizationMethod(canonicalization);
        signedInfo.setSignatureMethod(signatureMethod);
        signedInfo.setReference(reference);

        SignatureValue signatureValue = new SignatureValue();
        signatureValue.setValue(new byte[]{4, 5, 6}); // structural placeholder — NOT a real signature

        X509Data x509Data = new X509Data();
        x509Data.setX509Certificate(new byte[]{7, 8, 9}); // structural placeholder

        KeyInfo keyInfo = new KeyInfo();
        keyInfo.setX509Data(x509Data);

        Signature signature = new Signature();
        signature.setSignedInfo(signedInfo);
        signature.setSignatureValue(signatureValue);
        signature.setKeyInfo(keyInfo);
        return signature;
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static String marshal(Nfe nfe) throws Exception {
        JAXBContext context = JAXBContext.newInstance(Nfe.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(nfe, writer);
        return writer.toString();
    }
}
