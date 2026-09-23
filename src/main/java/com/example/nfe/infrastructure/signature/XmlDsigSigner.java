package com.example.nfe.infrastructure.signature;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Real NF-e XML Digital Signature implementation using the standard Java
 * XMLDSig API.
 * <p>
 * Official NF-e signing parameters (per the bundled xmldsig schema and the
 * NF-e signing requirements):
 * <ul>
 *   <li>Reference URI: {@code #} + the {@code infNFe} {@code Id} attribute</li>
 *   <li>Transforms (in order): enveloped-signature, then C14N 1.0</li>
 *   <li>CanonicalizationMethod: Inclusive C14N 1.0</li>
 *   <li>SignatureMethod: RSA-SHA1</li>
 *   <li>DigestMethod: SHA-1</li>
 *   <li>KeyInfo: X509Data with the certificate</li>
 * </ul>
 * The {@code ds:Signature} element is appended as the last child of
 * {@code NFe}, exactly where the official schema expects it. The signature
 * is computed over the actual parsed document — the very XML that was
 * generated.
 * <p>
 * XML parsing is hardened (namespace aware, secure processing, no external
 * DTD/entities, no DOCTYPE) — no XXE surface.
 */
public class XmlDsigSigner implements XmlSigner {

    private static final String NF_NAMESPACE = "http://www.portalfiscal.inf.br/nfe";

    private final SigningCredentials credentials;

    public XmlDsigSigner(SigningCredentials credentials) {
        if (credentials == null) {
            throw new IllegalArgumentException("signing credentials are required");
        }
        this.credentials = credentials;
    }

    @Override
    public String sign(String xml) {
        try {
            Document document = parseSecurely(xml);
            Element infNfe = findInfNfe(document);
            if (infNfe == null) {
                throw new IllegalStateException("infNFe element not found in the document");
            }
            String id = infNfe.getAttribute("Id");
            if (id == null || id.isBlank()) {
                throw new IllegalStateException(
                        "infNFe Id attribute missing — cannot build the signature Reference URI");
            }
            // Register the Id attribute so the XMLDSig Reference resolver finds
            // the element by URI ("#Id") without a DTD/type definition.
            infNfe.setIdAttribute("Id", true);

            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");

            List<Transform> transforms = List.of(
                    factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                    factory.newTransform(CanonicalizationMethod.INCLUSIVE, (TransformParameterSpec) null));

            DigestMethod digestMethod = factory.newDigestMethod(DigestMethod.SHA1, null);
            Reference reference = factory.newReference("#" + id, digestMethod, transforms, null, null);

            SignedInfo signedInfo = factory.newSignedInfo(
                    factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                            (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
                    factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    List.of(reference));

            KeyInfoFactory keyInfoFactory = factory.getKeyInfoFactory();
            X509Data x509Data = keyInfoFactory.newX509Data(List.of(credentials.certificate()));
            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(List.of(x509Data));

            XMLSignature signature = factory.newXMLSignature(signedInfo, keyInfo);

            DOMSignContext signContext = new DOMSignContext(
                    credentials.privateKey(), document.getDocumentElement());
            signContext.setDefaultNamespacePrefix("ds");
            signature.sign(signContext);

            return serialize(document);
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("XML signing failed: " + exception.getMessage(), exception);
        }
    }

    private Document parseSecurely(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xml)));
    }

    private Element findInfNfe(Document document) {
        return (Element) document.getElementsByTagNameNS(NF_NAMESPACE, "infNFe").item(0);
    }

    private String serialize(Document document) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        // IMPORTANT: no indentation — injected whitespace text nodes would
        // change the canonical form and invalidate the digest computed
        // over the parsed document.
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}
