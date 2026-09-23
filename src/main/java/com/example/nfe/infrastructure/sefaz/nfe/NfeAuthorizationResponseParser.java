package com.example.nfe.infrastructure.sefaz.nfe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Securely parses the {@code retEnviNFe} payload of the official
 * {@code NFeAutorizacao4} response (inside {@code nfeResultMsg}).
 * <p>
 * XML security protections are hard-enabled: DOCTYPE declarations are
 * rejected, external DTDs/entities are forbidden, XInclude and entity
 * expansion are disabled and secure processing is on. Element lookup is
 * namespace-agnostic by local name but scoped to direct children, matching
 * the official layout ({@code retEnviNFe} → {@code protNFe/infProt} or
 * {@code infRec}).
 */
final class NfeAuthorizationResponseParser {

    private NfeAuthorizationResponseParser() {
    }

    /**
     * Extracted official response fields. All values are nullable except
     * where the official layout marks them mandatory — safety checks for
     * mandatory fields are performed by the caller.
     */
    record Parsed(
            Integer cStat,
            String xMotivo,
            String tpAmb,
            String nRec,
            String nProt,
            Integer protCStat,
            String protXMotivo,
            String chNFe,
            String digVal) {
    }

    /**
     * @param soapResponse the full SOAP response body
     * @throws IllegalArgumentException when the response is not well-formed
     *                                  XML or does not contain retEnviNFe
     */
    static Parsed parse(String soapResponse) {
        Document document = parseSecurely(soapResponse);
        Element retEnviNFe = findFirstDescendant(document.getDocumentElement(), "retEnviNFe");
        if (retEnviNFe == null) {
            throw new IllegalArgumentException("SOAP response does not contain retEnviNFe");
        }

        Element infRec = directChild(retEnviNFe, "infRec");
        Element protNFe = directChild(retEnviNFe, "protNFe");

        String nProt = null;
        Integer protCStat = null;
        String protXMotivo = null;
        String chNFe = null;
        String digVal = null;
        if (protNFe != null) {
            Element infProt = directChild(protNFe, "infProt");
            if (infProt != null) {
                nProt = directChildText(infProt, "nProt");
                protCStat = intOrNull(directChildText(infProt, "cStat"));
                protXMotivo = directChildText(infProt, "xMotivo");
                chNFe = directChildText(infProt, "chNFe");
                digVal = directChildText(infProt, "digVal");
            }
        }

        return new Parsed(
                intOrNull(directChildText(retEnviNFe, "cStat")),
                directChildText(retEnviNFe, "xMotivo"),
                directChildText(retEnviNFe, "tpAmb"),
                infRec == null ? null : directChildText(infRec, "nRec"),
                nProt,
                protCStat,
                protXMotivo,
                chNFe,
                digVal);
    }

    private static Document parseSecurely(String soapResponse) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Secure XML parser features are not supported by this JDK", e);
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to create secure XML parser", e);
        }
        try {
            return builder.parse(new ByteArrayInputStream(soapResponse.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException("Response is not well-formed XML", e);
        }
    }

    private static Element findFirstDescendant(Element root, String localName) {
        NodeList nodes = root.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    private static Element directChild(Element parent, String localName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getLocalName())) {
                return (Element) node;
            }
        }
        return null;
    }

    private static String directChildText(Element parent, String localName) {
        Element child = directChild(parent, localName);
        return child == null ? null : child.getTextContent().trim();
    }

    private static Integer intOrNull(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
