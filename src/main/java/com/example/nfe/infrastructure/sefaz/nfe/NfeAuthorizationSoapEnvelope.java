package com.example.nfe.infrastructure.sefaz.nfe;

/**
 * Builds the SOAP 1.2 request envelope for the official NF-e authorization
 * WebService {@code NFeAutorizacao4} (operation {@code nfeAutorizacaoLote},
 * layout 4.00, synchronous batch with {@code indSinc=1}).
 * <p>
 * Contract (national WSDL, identical for every UF):
 * <pre>
 * soap12:Envelope (http://www.w3.org/2003/05/soap-envelope)
 *   soap12:Header
 *   soap12:Body
 *     nfeDadosMsg (http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4)
 *       enviNFe (http://www.portalfiscal.inf.br/nfe, versao="4.00")
 *         idLote  — 1..15 digits
 *         indSinc — 1 (synchronous authorization)
 *         NFe     — the exact signed NF-e document
 * </pre>
 * The signed document is embedded verbatim: its XML declaration (if any) is
 * stripped, but every other byte — including the XMLDSig signature — is
 * preserved untouched.
 */
final class NfeAuthorizationSoapEnvelope {

    static final String SOAP_ENV_NS = "http://www.w3.org/2003/05/soap-envelope";
    static final String WSDL_NS = "http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4";
    static final String NFE_NS = "http://www.portalfiscal.inf.br/nfe";
    static final String SERVICE_VERSION = "4.00";
    static final String SOAP_ACTION = WSDL_NS + "/nfeAutorizacaoLote";

    private NfeAuthorizationSoapEnvelope() {
    }

    /**
     * @param signedNfeXml the exact signed NF-e produced by the signing step
     * @param idLote       numeric batch identifier, 1..15 digits
     */
    static String build(String signedNfeXml, String idLote) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap12:Envelope xmlns:soap12=\"" + SOAP_ENV_NS + "\">"
                + "<soap12:Header/>"
                + "<soap12:Body>"
                + "<nfeDadosMsg xmlns=\"" + WSDL_NS + "\">"
                + "<enviNFe xmlns=\"" + NFE_NS + "\" versao=\"" + SERVICE_VERSION + "\">"
                + "<idLote>" + idLote + "</idLote>"
                + "<indSinc>1</indSinc>"
                + stripXmlDeclaration(signedNfeXml)
                + "</enviNFe>"
                + "</nfeDadosMsg>"
                + "</soap12:Body>"
                + "</soap12:Envelope>";
    }

    /**
     * Removes only the leading XML declaration of the signed document so it
     * can be embedded inside the SOAP body. Nothing else is modified.
     */
    private static String stripXmlDeclaration(String xml) {
        String trimmed = xml.trim();
        if (trimmed.startsWith("<?xml")) {
            int end = trimmed.indexOf("?>");
            if (end >= 0) {
                return trimmed.substring(end + 2);
            }
        }
        return trimmed;
    }
}
