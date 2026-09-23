package com.example.nfe.infrastructure.xml.validation;

import com.example.nfe.domain.Address;
import com.example.nfe.domain.EmissionStatus;
import com.example.nfe.domain.IbsCbsTaxation;
import com.example.nfe.domain.IbsMunicipalityTax;
import com.example.nfe.domain.IbsUfTax;
import com.example.nfe.domain.IcmsTax;
import com.example.nfe.domain.ImportTax;
import com.example.nfe.domain.IpiTax;
import com.example.nfe.domain.IsTax;
import com.example.nfe.domain.Issuer;
import com.example.nfe.domain.ItemTaxation;
import com.example.nfe.domain.NfeEmission;
import com.example.nfe.domain.NfeItem;
import com.example.nfe.domain.OperationType;
import com.example.nfe.domain.PisCofinsTax;
import com.example.nfe.domain.Recipient;
import com.example.nfe.domain.RtcTaxation;
import com.example.nfe.infrastructure.xml.NfeXmlGenerator;
import com.example.nfe.infrastructure.xml.NfeXmlMapper;
import com.example.nfe.testfixtures.NfeFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NfeXmlValidatorTest {

    private final NfeXmlValidator validator = new NfeXmlValidator();

    /**
     * Minimum required NF-e structure per the official PL_010f schema
     * (nfe_v4.00.xsd). This fixture exists ONLY to prove the validator
     * infrastructure works — the application's own generated XML is not yet
     * schema-complete (see shouldReportGeneratedXmlAsNotYetSchemaComplete).
     */
    private static final String MINIMAL_VALID_NFE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <NFe xmlns="http://www.portalfiscal.inf.br/nfe">
              <infNFe versao="4.00" Id="NFe35260912345678000199550010000000011000000001">
                <ide>
                  <cUF>35</cUF><cNF>00000001</cNF><natOp>Venda</natOp><mod>55</mod><serie>1</serie><nNF>1</nNF>
                  <dhEmi>2026-09-21T10:00:00-03:00</dhEmi><tpNF>1</tpNF><idDest>1</idDest><cMunFG>3550308</cMunFG>
                  <tpImp>1</tpImp><tpEmis>1</tpEmis><cDV>0</cDV><tpAmb>2</tpAmb><finNFe>1</finNFe>
                  <indFinal>1</indFinal><indPres>1</indPres><procEmi>0</procEmi><verProc>1.0</verProc>
                </ide>
                <emit>
                  <CNPJ>12345678000199</CNPJ><xNome>Acme Ltda</xNome>
                  <enderEmit><xLgr>Rua A</xLgr><nro>100</nro><xBairro>Centro</xBairro><cMun>3550308</cMun><xMun>Sao Paulo</xMun><UF>SP</UF><CEP>01310100</CEP><cPais>1058</cPais><xPais>BRASIL</xPais></enderEmit>
                  <IE>123456789</IE><CRT>3</CRT>
                </emit>
                <det nItem="1">
                  <prod>
                    <cProd>P-1</cProd><cEAN>SEM GTIN</cEAN><xProd>Widget</xProd><NCM>84818090</NCM><CFOP>5102</CFOP>
                    <uCom>UN</uCom><qCom>1.0000</qCom><vUnCom>10.5000000000</vUnCom><vProd>10.50</vProd>
                    <cEANTrib>SEM GTIN</cEANTrib><uTrib>UN</uTrib><qTrib>1.0000</qTrib><vUnTrib>10.5000000000</vUnTrib><indTot>1</indTot>
                  </prod>
                  <imposto>
                    <ICMS><ICMS00><orig>0</orig><CST>00</CST><modBC>3</modBC><vBC>10.50</vBC><pICMS>18.00</pICMS><vICMS>1.89</vICMS></ICMS00></ICMS>
                  </imposto>
                </det>
                <total>
                  <ICMSTot>
                    <vBC>10.50</vBC><vICMS>1.89</vICMS><vICMSDeson>0.00</vICMSDeson><vFCP>0.00</vFCP><vBCST>0.00</vBCST><vST>0.00</vST>
                    <vFCPST>0.00</vFCPST><vFCPSTRet>0.00</vFCPSTRet><vProd>10.50</vProd><vFrete>0.00</vFrete><vSeg>0.00</vSeg>
                    <vDesc>0.00</vDesc><vII>0.00</vII><vIPI>0.00</vIPI><vIPIDevol>0.00</vIPIDevol><vPIS>0.00</vPIS><vCOFINS>0.00</vCOFINS>
                    <vOutro>0.00</vOutro><vNF>10.50</vNF>
                  </ICMSTot>
                </total>
                <transp><modFrete>9</modFrete></transp>
                <pag><detPag><tPag>01</tPag><vPag>10.50</vPag></detPag></pag>
              </infNFe>
              <Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
                <SignedInfo>
                  <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315"/>
                  <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
                  <Reference URI="#NFe35260912345678000199550010000000011000000001">
                    <Transforms>
                      <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
                      <Transform Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315"/>
                    </Transforms>
                    <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
                    <DigestValue>YWJjZA==</DigestValue>
                  </Reference>
                </SignedInfo>
                <SignatureValue>YWJjZA==</SignatureValue>
                <KeyInfo><X509Data><X509Certificate>YWJjZA==</X509Certificate></X509Data></KeyInfo>
              </Signature>
            </NFe>
            """;

    @Test
    void shouldValidateMinimalValidNfeAgainstBundledXsd() {
        assertDoesNotThrow(() -> validator.validateNFe(MINIMAL_VALID_NFE));
    }

    @Test
    void shouldRejectInvalidElementName() {
        String xml = MINIMAL_VALID_NFE.replace("<xProd>Widget</xProd>", "<xProdX>Widget</xProdX>");

        assertThrows(NfeXmlValidationException.class, () -> validator.validateNFe(xml));
    }

    @Test
    void shouldRejectIncorrectElementOrder() {
        String xml = MINIMAL_VALID_NFE.replace(
                "<vUnCom>10.5000000000</vUnCom><vProd>10.50</vProd>",
                "<vProd>10.50</vProd><vUnCom>10.5000000000</vUnCom>");

        assertThrows(NfeXmlValidationException.class, () -> validator.validateNFe(xml));
    }

    @Test
    void shouldRejectMalformedNumericValue() {
        String xml = MINIMAL_VALID_NFE.replace("<vProd>10.50</vProd>", "<vProd>abc</vProd>");

        assertThrows(NfeXmlValidationException.class, () -> validator.validateNFe(xml));
    }

    @Test
    void shouldRejectInvalidNamespace() {
        String xml = MINIMAL_VALID_NFE.replace(
                "xmlns=\"http://www.portalfiscal.inf.br/nfe\"",
                "xmlns=\"http://invalid.example/nfe\"");

        assertThrows(NfeXmlValidationException.class, () -> validator.validateNFe(xml));
    }

    @Test
    void shouldRejectBlankXml() {
        assertThrows(NfeXmlValidationException.class, () -> validator.validateNFe("   "));
    }

    @Test
    void shouldReportLineAndColumnInformation() {
        NfeXmlValidationException exception = assertThrows(NfeXmlValidationException.class,
                () -> validator.validateNFe(MINIMAL_VALID_NFE.replace("<vProd>10.50</vProd>", "<vProd>abc</vProd>")));

        assertTrue(exception.getLineNumber() > 0,
                "line number must be reported, was " + exception.getLineNumber());
        assertTrue(exception.getColumnNumber() > 0,
                "column number must be reported, was " + exception.getColumnNumber());
        assertTrue(exception.getMessage().contains("line"),
                "message must include line information: " + exception.getMessage());
    }

    @Test
    void shouldThrowApplicationSpecificExceptionOnly() {
        String xml = MINIMAL_VALID_NFE.replace("<mod>55</mod>", "<mod>99</mod>");

        NfeXmlValidationException exception = assertThrows(NfeXmlValidationException.class,
                () -> validator.validateNFe(xml));
        assertEquals("NfeXmlValidationException", exception.getClass().getSimpleName());
    }

    @Test
    void shouldLoadSchemasFromClasspath() {
        // A fresh validator must be able to build the Schema from classpath resources.
        NfeXmlValidator fresh = new NfeXmlValidator();

        assertDoesNotThrow(() -> fresh.validateNFe(MINIMAL_VALID_NFE));
    }

    @Test
    void shouldReportGeneratedXmlAsNotYetSchemaComplete() {
        // Documents a KNOWN state: the incremental JAXB model is not yet
        // schema-complete (e.g. mandatory ide fields, total, transp, signature
        // are not generated). This test must be updated once the generation
        // model covers the mandatory structure.
        NfeXmlGenerator generator = new NfeXmlGenerator(new NfeXmlMapper());
        String xml = generator.generate(fullTaxEmission());

        assertThrows(NfeXmlValidationException.class, () -> validator.validateNFe(xml));
    }

    @Test
    void shouldValidateInfNfePayloadOfUnsignedGeneratedDocument() {
        NfeXmlGenerator generator = new NfeXmlGenerator(new NfeXmlMapper());
        String unsignedXml = generator.generate(NfeFixtures.fullEmission());

        assertDoesNotThrow(() -> validator.validateInfNFe(unsignedXml));
        // The unsigned document cannot pass complete-NFe validation: the
        // official TNFe requires ds:Signature.
        assertThrows(NfeXmlValidationException.class, () -> validator.validateNFe(unsignedXml));
    }

    @Test
    void shouldRejectInfNfeValidationForInvalidInfNfe() {
        NfeXmlGenerator generator = new NfeXmlGenerator(new NfeXmlMapper());
        String xml = generator.generate(NfeFixtures.invalidInfNfeEmission());

        assertThrows(NfeXmlValidationException.class, () -> validator.validateInfNFe(xml));
    }

    @Test
    void shouldRejectInfNfeValidationWhenInfNfeIsMissing() {
        assertThrows(NfeXmlValidationException.class,
                () -> validator.validateInfNFe("<foo xmlns=\"http://www.portalfiscal.inf.br/nfe\"/>"));
    }

    @Test
    void shouldRejectInfNfeValidationWithDoctype() {
        String xml = "<!DOCTYPE infNFe [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<NFe xmlns=\"http://www.portalfiscal.inf.br/nfe\"><infNFe>&xxe;</infNFe></NFe>";

        assertThrows(NfeXmlValidationException.class, () -> validator.validateInfNFe(xml));
    }

    @Test
    void shouldRejectBlankXmlForInfNfeValidation() {
        assertThrows(NfeXmlValidationException.class, () -> validator.validateInfNFe("   "));
    }

    private static NfeEmission fullTaxEmission() {
        Address address = new Address("Main St", "100", "Sao Paulo", "SP", "01310-100", "Centro", "3550308");
        Issuer issuer = new Issuer("Acme Ltd", "12345678000199", "123456789", address, null, null);
        Recipient recipient = new Recipient("Beta Corp", "98765432000188", address, null);

        IcmsTax icms = new IcmsTax("00", "3",
                new BigDecimal("100.00"), new BigDecimal("18.00"), new BigDecimal("18.00"),
                new BigDecimal("2.00"), new BigDecimal("2.00"), null, null, null);
        IpiTax ipi = new IpiTax("50",
                new BigDecimal("100.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));
        PisCofinsTax pisCofins = new PisCofinsTax(
                "01", new BigDecimal("100.00"), new BigDecimal("1.65"), new BigDecimal("1.65"),
                "01", new BigDecimal("100.00"), new BigDecimal("7.60"), new BigDecimal("7.60"));
        ImportTax importTax = new ImportTax(
                new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("10.00"),
                new BigDecimal("5.00"), new BigDecimal("1.50"));
        IsTax is = new IsTax("900", "001000",
                new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("1.50"),
                "UN", new BigDecimal("2"), new BigDecimal("20.00"));
        IbsUfTax ibsUf = new IbsUfTax(new BigDecimal("10.00"), null, null, null, new BigDecimal("8.50"));
        IbsMunicipalityTax ibsMun = new IbsMunicipalityTax(new BigDecimal("5.00"), null, null, null, new BigDecimal("3.40"));
        RtcTaxation rtc = new RtcTaxation("300", "001000", "1",
                new IbsCbsTaxation(new BigDecimal("100.00"), ibsUf, ibsMun, new BigDecimal("99.99"), null, null));

        NfeItem item = new NfeItem("P-1", "Widget", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(icms, ipi, pisCofins, importTax, is, rtc),
                null);

        return new NfeEmission(
                "emission-1", "REF-1", OperationType.TRANSFER, EmissionStatus.RECEIVED,
                "55", 3, 1000L, OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializa\u00e7\u00e3o", "3550308",
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer, recipient, List.of(item),
                null, null, null, null, null,
                null);
    }
}
