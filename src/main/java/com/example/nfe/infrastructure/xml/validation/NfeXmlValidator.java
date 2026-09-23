package com.example.nfe.infrastructure.xml.validation;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Validates generated NF-e XML against the official PL_010f XSD schemas
 * bundled in the application classpath ({@code xsd/PL_010f/}).
 * <p>
 * Two explicit validation boundaries are offered — callers state which one
 * they mean, no boolean flags:
 * <ul>
 *   <li>{@link #validateInfNFe(String)} — validates the {@code infNFe}
 *       payload of an UNSIGNED document. The official {@code leiauteNFe}
 *       defines {@code infNFe} only as a local element with an anonymous
 *       type inside {@code TNFe} (which requires {@code ds:Signature}), so
 *       the real {@code infNFe} element is parsed namespace-aware and
 *       validated against the official schema inside a wrapper whose
 *       signature is a structurally valid, validation-only skeleton. The
 *       {@code infNFe} rules themselves are never weakened.</li>
 *   <li>{@link #validateNFe(String)} — validates the complete SIGNED
 *       document against {@code nfe_v4.00.xsd}, including the mandatory
 *       {@code ds:Signature}.</li>
 * </ul>
 * The schema is loaded once from the classpath via a
 * {@link LSResourceResolver} that resolves only the bundled schema files —
 * no filesystem or network access is ever attempted, so validation works
 * when the application is packaged as a JAR and offline.
 * <p>
 * XML parsing is hardened: DOCTYPE declarations are rejected and external
 * entity/DTD/schema resolution is disabled (XXE protection).
 */
@Component
public class NfeXmlValidator {

    private static final String SCHEMA_BASE = "xsd/PL_010f/";
    private static final String MAIN_SCHEMA = SCHEMA_BASE + "nfe_v4.00.xsd";

    private static final String NFE_NS = "http://www.portalfiscal.inf.br/nfe";
    private static final String DS_NS = "http://www.w3.org/2000/09/xmldsig#";

    private static final String C14N_INCLUSIVE = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    private static final String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    private static final String DIGEST_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";

    private static final Set<String> ALLOWED_SCHEMA_FILES = Set.of(
            "nfe_v4.00.xsd",
            "leiauteNFe_v4.00.xsd",
            "tiposBasico_v4.00.xsd",
            "DFeTiposBasicos_v1.00.xsd",
            "xmldsig-core-schema_v1.01.xsd");

    private final Schema schema;

    public NfeXmlValidator() {
        this.schema = loadSchema();
    }

    /**
     * Validates the complete (signed) NFe document against the official
     * {@code nfe_v4.00.xsd} schema, including the mandatory signature.
     *
     * @param xml the complete XML document content
     * @throws NfeXmlValidationException if the XML is null, blank or does not
     *                                   conform to the official schema
     */
    public void validateNFe(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new NfeXmlValidationException("XML content is null or blank");
        }
        validate(new SAXSource(createSecureXmlReader(), new InputSource(new StringReader(xml))));
    }

    /**
     * Validates the {@code infNFe} payload of an UNSIGNED document against
     * the official schema types. The document must contain an
     * {@code infNFe} element in the official namespace; no signature is
     * required (or examined).
     *
     * @param xml the generated XML document content (NFe wrapper, unsigned)
     * @throws NfeXmlValidationException if the XML is null, blank, has no
     *                                   infNFe element or does not conform
     */
    public void validateInfNFe(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new NfeXmlValidationException("XML content is null or blank");
        }
        Document document = parseSecurely(xml);
        NodeList infNfeNodes = document.getElementsByTagNameNS(NFE_NS, "infNFe");
        if (infNfeNodes.getLength() == 0) {
            throw new NfeXmlValidationException("infNFe element not found in the generated XML");
        }
        Element infNfe = (Element) infNfeNodes.item(0);

        // The official TNFe requires ds:Signature, which an unsigned document
        // does not carry. Validate the REAL infNFe element inside a wrapper
        // with a structurally valid signature skeleton (validation-only
        // artifact — never returned, never transmitted). The infNFe content
        // is validated by the official types at full strength.
        Document validationDocument = buildValidationDocument(infNfe);
        validate(new DOMSource(validationDocument));
    }

    private void validate(Source source) {
        AtomicReference<SAXParseException> firstError = new AtomicReference<>();
        try {
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    // warnings do not fail validation
                }

                @Override
                public void error(SAXParseException exception) {
                    firstError.compareAndSet(null, exception);
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    firstError.compareAndSet(null, exception);
                }
            });
            validator.validate(source);
        } catch (SAXParseException e) {
            throw toValidationException(e);
        } catch (SAXException | IOException e) {
            throw new NfeXmlValidationException("Failed to validate NF-e XML: " + e.getMessage(), e);
        }

        SAXParseException error = firstError.get();
        if (error != null) {
            throw toValidationException(error);
        }
    }

    private static Document parseSecurely(String xml) {
        try {
            return createSecureDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (SAXException | IOException e) {
            throw new NfeXmlValidationException("Failed to parse NF-e XML: " + e.getMessage(), e);
        }
    }

    /**
     * Builds an NFe wrapper around the real infNFe element with a signature
     * skeleton that satisfies the official ds:Signature STRUCTURE (the
     * xmldsig schema checks structure only, never cryptography). This is a
     * validation-only document — it is never returned or transmitted.
     */
    private static Document buildValidationDocument(Element infNfe) {
        Document document = createSecureDocumentBuilder().newDocument();
        Element nfe = document.createElementNS(NFE_NS, "NFe");
        document.appendChild(nfe);
        nfe.appendChild(document.importNode(infNfe, true));
        nfe.appendChild(buildSignatureSkeleton(document));
        return document;
    }

    private static Element buildSignatureSkeleton(Document document) {
        Element signature = document.createElementNS(DS_NS, "Signature");

        Element signedInfo = document.createElementNS(DS_NS, "SignedInfo");

        Element canonicalizationMethod = document.createElementNS(DS_NS, "CanonicalizationMethod");
        canonicalizationMethod.setAttribute("Algorithm", C14N_INCLUSIVE);
        signedInfo.appendChild(canonicalizationMethod);

        Element signatureMethod = document.createElementNS(DS_NS, "SignatureMethod");
        signatureMethod.setAttribute("Algorithm", RSA_SHA1);
        signedInfo.appendChild(signatureMethod);

        Element reference = document.createElementNS(DS_NS, "Reference");
        reference.setAttribute("URI", "#validation-skeleton");
        Element transforms = document.createElementNS(DS_NS, "Transforms");
        Element envelopedTransform = document.createElementNS(DS_NS, "Transform");
        envelopedTransform.setAttribute("Algorithm",
                "http://www.w3.org/2000/09/xmldsig#enveloped-signature");
        Element c14nTransform = document.createElementNS(DS_NS, "Transform");
        c14nTransform.setAttribute("Algorithm", C14N_INCLUSIVE);
        transforms.appendChild(envelopedTransform);
        transforms.appendChild(c14nTransform);
        reference.appendChild(transforms);
        Element digestMethod = document.createElementNS(DS_NS, "DigestMethod");
        digestMethod.setAttribute("Algorithm", DIGEST_SHA1);
        reference.appendChild(digestMethod);
        reference.appendChild(document.createElementNS(DS_NS, "DigestValue"));
        signedInfo.appendChild(reference);

        signature.appendChild(signedInfo);
        signature.appendChild(document.createElementNS(DS_NS, "SignatureValue"));

        Element keyInfo = document.createElementNS(DS_NS, "KeyInfo");
        Element x509Data = document.createElementNS(DS_NS, "X509Data");
        x509Data.appendChild(document.createElementNS(DS_NS, "X509Certificate"));
        keyInfo.appendChild(x509Data);
        signature.appendChild(keyInfo);
        return signature;
    }

    private NfeXmlValidationException toValidationException(SAXParseException e) {
        return new NfeXmlValidationException(
                e.getMessage() == null ? "Schema validation failed" : e.getMessage(),
                e.getLineNumber(),
                e.getColumnNumber(),
                e);
    }

    private Schema loadSchema() {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (SAXException e) {
            throw new IllegalStateException("Unable to harden the schema factory", e);
        }
        factory.setResourceResolver(new ClasspathSchemaResolver());
        try (InputStream schemaStream = openClasspathResource(MAIN_SCHEMA)) {
            return factory.newSchema(new SAXSource(createSecureXmlReader(),
                    new InputSource(schemaStream)));
        } catch (SAXException e) {
            throw new IllegalStateException("Failed to load the official NF-e XSD schema from classpath", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read the official NF-e XSD schema resource", e);
        }
    }

    private static InputStream openClasspathResource(String path) {
        InputStream stream = NfeXmlValidator.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Schema resource not found on classpath: " + path);
        }
        return stream;
    }

    private XMLReader createSecureXmlReader() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            try {
                reader.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                reader.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            } catch (SAXException ignored) {
                // best effort: property unsupported by this parser
            }
            return reader;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IllegalStateException("Unable to configure a secure XML parser", e);
        }
    }

    private static DocumentBuilder createSecureDocumentBuilder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to configure a secure XML DOM parser", e);
        }
    }

    /**
     * Resolves schema imports/includes ONLY to the official XSD files bundled
     * in the classpath. Never touches the filesystem or the network.
     */
    private static final class ClasspathSchemaResolver implements LSResourceResolver {

        @Override
        public LSInput resolveResource(String type, String namespaceURI,
                                       String publicId, String systemId, String baseURI) {
            String fileName = systemId == null ? null : systemId.substring(systemId.lastIndexOf('/') + 1);
            if (fileName == null || !ALLOWED_SCHEMA_FILES.contains(fileName)) {
                throw new IllegalStateException(
                        "Refusing to resolve external schema resource: " + systemId);
            }
            return new ClasspathLsInput(openClasspathResource(SCHEMA_BASE + fileName), fileName);
        }
    }

    private static final class ClasspathLsInput implements LSInput {

        private final InputStream byteStream;
        private String systemId;

        ClasspathLsInput(InputStream byteStream, String systemId) {
            this.byteStream = byteStream;
            this.systemId = systemId;
        }

        @Override
        public Reader getCharacterStream() {
            return null;
        }

        @Override
        public void setCharacterStream(Reader characterStream) {
            // unused
        }

        @Override
        public InputStream getByteStream() {
            return byteStream;
        }

        @Override
        public void setByteStream(InputStream byteStream) {
            // unused
        }

        @Override
        public String getStringData() {
            return null;
        }

        @Override
        public void setStringData(String stringData) {
            // unused
        }

        @Override
        public String getSystemId() {
            return systemId;
        }

        @Override
        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public void setPublicId(String publicId) {
            // unused
        }

        @Override
        public String getBaseURI() {
            return null;
        }

        @Override
        public void setBaseURI(String baseURI) {
            // unused
        }

        @Override
        public String getEncoding() {
            return null;
        }

        @Override
        public void setEncoding(String encoding) {
            // unused
        }

        @Override
        public boolean getCertifiedText() {
            return false;
        }

        @Override
        public void setCertifiedText(boolean certifiedText) {
            // unused
        }
    }
}
