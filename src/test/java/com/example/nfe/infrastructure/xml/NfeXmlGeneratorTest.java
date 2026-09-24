package com.example.nfe.infrastructure.xml;

import com.example.nfe.domain.Address;
import com.example.nfe.domain.EmissionStatus;
import com.example.nfe.domain.IbsCbsTaxation;
import com.example.nfe.domain.IbsMunicipalityTax;
import com.example.nfe.domain.IbsUfTax;
import com.example.nfe.domain.IcmsStTax;
import com.example.nfe.domain.IcmsTax;
import com.example.nfe.domain.IcmsTotals;
import com.example.nfe.domain.ImportTax;
import com.example.nfe.domain.IpiTax;
import com.example.nfe.domain.IsTax;
import com.example.nfe.domain.Issuer;
import com.example.nfe.domain.ItemTaxation;
import com.example.nfe.domain.NfeEmission;
import com.example.nfe.domain.NfeItem;
import com.example.nfe.domain.OperationType;
import com.example.nfe.domain.Payment;
import com.example.nfe.domain.PaymentDetail;
import com.example.nfe.domain.PaymentIndicator;
import com.example.nfe.domain.PisCofinsTax;
import com.example.nfe.domain.Recipient;
import com.example.nfe.domain.RecipientIeStatus;
import com.example.nfe.domain.RtcDeferral;
import com.example.nfe.domain.RtcDevolution;
import com.example.nfe.domain.RtcReduction;
import com.example.nfe.domain.RtcTaxation;
import com.example.nfe.domain.TaxTotals;
import com.example.nfe.domain.Totals;
import com.example.nfe.domain.Transport;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NfeXmlGeneratorTest {

    private static final String NF_NAMESPACE = "http://www.portalfiscal.inf.br/nfe";

    private final NfeXmlGenerator generator = new NfeXmlGenerator(new NfeXmlMapper());

    @Test
    void shouldGenerateNfeRootElement() {
        String xml = generator.generate(emissionWithHeader());

        assertTrue(xml.contains("<NFe"), "expected NFe root element");
        assertTrue(xml.contains(NF_NAMESPACE), "expected NF-e namespace");
    }

    @Test
    void shouldGenerateInfNfeElement() {
        String xml = generator.generate(emissionWithHeader());

        assertTrue(xml.contains("<infNFe versao=\"4.00\""), "expected infNFe element with versao attribute");
        assertTrue(xml.contains("<ide>"), "expected ide element");
    }

    @Test
    void shouldSetInfNfeVersionAttribute() throws Exception {
        String xml = generator.generate(emissionWithHeader());
        Document document = parse(xml);

        assertEquals("4.00", xpath(document, "/NFe/infNFe/@versao"));
    }

    @Test
    void shouldSetInfNfeIdFromAccessKey() throws Exception {
        String xml = generator.generate(emissionWithFullIde());
        Document document = parse(xml);

        assertEquals("NFe35260912345678000199550030000010001000000019",
                xpath(document, "/NFe/infNFe/@Id"));
    }

    @Test
    void shouldNotSetInfNfeIdWhenAccessKeyComponentsAreMissing() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(item())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/@Id"));
    }

    @Test
    void shouldMapHeaderFieldsToXml() {
        String xml = generator.generate(emissionWithHeader());

        assertTrue(xml.contains("<mod>55</mod>"));
        assertTrue(xml.contains("<serie>3</serie>"));
        assertTrue(xml.contains("<nNF>1000</nNF>"));
        assertTrue(xml.contains("<dhEmi>2026-09-21T10:30:00-03:00</dhEmi>"));
        assertTrue(xml.contains("<natOp>Remessa para industrialização</natOp>"));
        assertTrue(xml.contains("<cMunFG>3550308</cMunFG>"));
    }

    @Test
    void shouldHandleNullableHeaderFields() {
        String xml = generator.generate(emissionWithNullHeader());

        assertTrue(xml.contains("<NFe"), "root must still be generated");
        assertFalse(xml.contains("<mod>"), "null fields must be omitted");
        assertFalse(xml.contains("<dhEmi>"), "null emissionDate must be omitted");
    }

    @Test
    void shouldKeepDomainFreeOfXmlAnnotations() {
        assertNull(NfeEmission.class.getAnnotation(XmlRootElement.class));
        assertNull(NfeItem.class.getAnnotation(XmlRootElement.class));
        assertTrue(NfeEmission.class.getAnnotations().length == 0,
                "NfeEmission must not carry any annotations");
    }

    @Test
    void shouldMapIssuerToXml() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("Acme Ltd", xpath(document, "/NFe/infNFe/emit/xNome"));
        assertEquals("12345678000199", xpath(document, "/NFe/infNFe/emit/CNPJ"));
    }

    @Test
    void shouldMapRecipientToXml() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("Beta Corp", xpath(document, "/NFe/infNFe/dest/xNome"));
        assertEquals("98765432000188", xpath(document, "/NFe/infNFe/dest/CNPJ"));
    }

    @Test
    void shouldMapIssuerAddressToXml() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("Main St", xpath(document, "/NFe/infNFe/emit/enderEmit/xLgr"));
        assertEquals("100", xpath(document, "/NFe/infNFe/emit/enderEmit/nro"));
        assertEquals("Sao Paulo", xpath(document, "/NFe/infNFe/emit/enderEmit/xMun"));
        assertEquals("SP", xpath(document, "/NFe/infNFe/emit/enderEmit/UF"));
        assertEquals("01310-100", xpath(document, "/NFe/infNFe/emit/enderEmit/CEP"));
    }

    @Test
    void shouldMapRecipientAddressToXml() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("Main St", xpath(document, "/NFe/infNFe/dest/enderDest/xLgr"));
        assertEquals("100", xpath(document, "/NFe/infNFe/dest/enderDest/nro"));
        assertEquals("Sao Paulo", xpath(document, "/NFe/infNFe/dest/enderDest/xMun"));
        assertEquals("SP", xpath(document, "/NFe/infNFe/dest/enderDest/UF"));
        assertEquals("01310-100", xpath(document, "/NFe/infNFe/dest/enderDest/CEP"));
    }

    @Test
    void shouldHandleNullIssuerAddress() throws Exception {
        String xml = generator.generate(emissionWithParties(issuerWithoutAddress(), recipient()));
        Document document = parse(xml);

        assertEquals("Acme Ltd", xpath(document, "/NFe/infNFe/emit/xNome"));
        assertNull(xpath(document, "/NFe/infNFe/emit/enderEmit"));
    }

    @Test
    void shouldHandleNullRecipientAddress() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipientWithoutAddress()));
        Document document = parse(xml);

        assertEquals("Beta Corp", xpath(document, "/NFe/infNFe/dest/xNome"));
        assertNull(xpath(document, "/NFe/infNFe/dest/enderDest"));
    }

    @Test
    void shouldPreserveExistingIdeMapping() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("55", xpath(document, "/NFe/infNFe/ide/mod"));
        assertEquals("1000", xpath(document, "/NFe/infNFe/ide/nNF"));
        assertEquals("Remessa para industrializa\u00e7\u00e3o", xpath(document, "/NFe/infNFe/ide/natOp"));
    }

    @Test
    void shouldMapAllIdeIdentificationFields() throws Exception {
        String xml = generator.generate(emissionWithFullIde());
        Document document = parse(xml);

        assertEquals("35", xpath(document, "/NFe/infNFe/ide/cUF"));
        assertEquals("00000001", xpath(document, "/NFe/infNFe/ide/cNF"));
        assertEquals("1", xpath(document, "/NFe/infNFe/ide/tpNF"));
        assertEquals("1", xpath(document, "/NFe/infNFe/ide/idDest"));
        assertEquals("1", xpath(document, "/NFe/infNFe/ide/tpImp"));
        assertEquals("1", xpath(document, "/NFe/infNFe/ide/tpEmis"));
        assertEquals("9", xpath(document, "/NFe/infNFe/ide/cDV"));
        assertEquals("2", xpath(document, "/NFe/infNFe/ide/tpAmb"));
        assertEquals("1", xpath(document, "/NFe/infNFe/ide/finNFe"));
        assertEquals("1", xpath(document, "/NFe/infNFe/ide/indFinal"));
        assertEquals("1", xpath(document, "/NFe/infNFe/ide/indPres"));
        assertEquals("0", xpath(document, "/NFe/infNFe/ide/procEmi"));
        assertEquals("1.0", xpath(document, "/NFe/infNFe/ide/verProc"));
    }

    @Test
    void shouldUseOfficialCheckDigitForAccessKeyAndIde() throws Exception {
        String xml = generator.generate(emissionWithFullIde());
        Document document = parse(xml);

        String accessKey = xpath(document, "/NFe/infNFe/@Id").replace("NFe", "");
        String ideCdv = xpath(document, "/NFe/infNFe/ide/cDV");

        assertEquals("9", ideCdv);
        assertEquals("9", String.valueOf(accessKey.charAt(accessKey.length() - 1)));
    }

    @Test
    void shouldPreserveIdeElementOrder() throws Exception {
        String xml = generator.generate(emissionWithFullIde());
        Document document = parse(xml);

        assertEquals("cUF", xpath(document, "name(/NFe/infNFe/ide/*[1])"));
        assertEquals("cNF", xpath(document, "name(/NFe/infNFe/ide/*[2])"));
        assertEquals("natOp", xpath(document, "name(/NFe/infNFe/ide/*[3])"));
        assertEquals("mod", xpath(document, "name(/NFe/infNFe/ide/*[4])"));
        assertEquals("serie", xpath(document, "name(/NFe/infNFe/ide/*[5])"));
        assertEquals("nNF", xpath(document, "name(/NFe/infNFe/ide/*[6])"));
        assertEquals("dhEmi", xpath(document, "name(/NFe/infNFe/ide/*[7])"));
        assertEquals("tpNF", xpath(document, "name(/NFe/infNFe/ide/*[8])"));
        assertEquals("idDest", xpath(document, "name(/NFe/infNFe/ide/*[9])"));
        assertEquals("cMunFG", xpath(document, "name(/NFe/infNFe/ide/*[10])"));
        assertEquals("tpImp", xpath(document, "name(/NFe/infNFe/ide/*[11])"));
        assertEquals("tpEmis", xpath(document, "name(/NFe/infNFe/ide/*[12])"));
        assertEquals("cDV", xpath(document, "name(/NFe/infNFe/ide/*[13])"));
        assertEquals("tpAmb", xpath(document, "name(/NFe/infNFe/ide/*[14])"));
        assertEquals("finNFe", xpath(document, "name(/NFe/infNFe/ide/*[15])"));
        assertEquals("indFinal", xpath(document, "name(/NFe/infNFe/ide/*[16])"));
        assertEquals("indPres", xpath(document, "name(/NFe/infNFe/ide/*[17])"));
        assertEquals("procEmi", xpath(document, "name(/NFe/infNFe/ide/*[18])"));
        assertEquals("verProc", xpath(document, "name(/NFe/infNFe/ide/*[19])"));
    }

    @Test
    void shouldNotEmitNullIdeIdentificationFields() throws Exception {
        String xml = generator.generate(emissionWithHeader());
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/ide/cUF"));
        assertNull(xpath(document, "/NFe/infNFe/ide/cNF"));
        assertNull(xpath(document, "/NFe/infNFe/ide/tpNF"));
        assertNull(xpath(document, "/NFe/infNFe/ide/idDest"));
        assertNull(xpath(document, "/NFe/infNFe/ide/tpImp"));
        assertNull(xpath(document, "/NFe/infNFe/ide/tpEmis"));
        assertNull(xpath(document, "/NFe/infNFe/ide/cDV"));
        assertNull(xpath(document, "/NFe/infNFe/ide/tpAmb"));
        assertNull(xpath(document, "/NFe/infNFe/ide/finNFe"));
        assertNull(xpath(document, "/NFe/infNFe/ide/indFinal"));
        assertNull(xpath(document, "/NFe/infNFe/ide/indPres"));
        assertNull(xpath(document, "/NFe/infNFe/ide/procEmi"));
        assertNull(xpath(document, "/NFe/infNFe/ide/verProc"));
        assertTrue(xml.contains("<mod>55</mod>"), "existing ide fields must remain");
    }

    @Test
    void shouldGenerateOneDetForEachItem() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1(), fullItem2())));
        Document document = parse(xml);

        assertEquals(2.0, countXpath(document, "count(/NFe/infNFe/det)"));
    }

    @Test
    void shouldMapItemSequence() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1(), fullItem2())));
        Document document = parse(xml);

        assertEquals("1", xpath(document, "/NFe/infNFe/det[1]/@nItem"));
        assertEquals("2", xpath(document, "/NFe/infNFe/det[2]/@nItem"));
    }

    @Test
    void shouldMapProductIdentification() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertEquals("P-1", xpath(document, "/NFe/infNFe/det[1]/prod/cProd"));
        assertEquals("Widget", xpath(document, "/NFe/infNFe/det[1]/prod/xProd"));
        assertEquals("84818090", xpath(document, "/NFe/infNFe/det[1]/prod/NCM"));
    }

    @Test
    void shouldMapCommercialValues() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertEquals("UN", xpath(document, "/NFe/infNFe/det[1]/prod/uCom"));
        assertEquals("1", xpath(document, "/NFe/infNFe/det[1]/prod/qCom"));
        assertEquals("10.50", xpath(document, "/NFe/infNFe/det[1]/prod/vUnCom"));
        assertEquals("10.50", xpath(document, "/NFe/infNFe/det[1]/prod/vProd"));
    }

    @Test
    void shouldMapTributaryValues() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertEquals("UN", xpath(document, "/NFe/infNFe/det[1]/prod/uTrib"));
        assertEquals("1", xpath(document, "/NFe/infNFe/det[1]/prod/qTrib"));
        assertEquals("10.50", xpath(document, "/NFe/infNFe/det[1]/prod/vUnTrib"));
    }

    @Test
    void shouldMapFiscalProductFields() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertEquals("5102", xpath(document, "/NFe/infNFe/det[1]/prod/CFOP"));
        assertEquals("0101010", xpath(document, "/NFe/infNFe/det[1]/prod/CEST"));
        assertEquals("1", xpath(document, "/NFe/infNFe/det[1]/prod/indTot"));
    }

    @Test
    void shouldMapPurchaseOrderFields() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertEquals("PO-2026-000123", xpath(document, "/NFe/infNFe/det[1]/prod/xPed"));
        assertEquals("00010", xpath(document, "/NFe/infNFe/det[1]/prod/nItemPed"));
    }

    @Test
    void shouldMapEanFields() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertEquals("7891234567895", xpath(document, "/NFe/infNFe/det[1]/prod/cEAN"));
        assertEquals("7899876543210", xpath(document, "/NFe/infNFe/det[1]/prod/cEANTrib"));
    }

    @Test
    void shouldPreserveProdElementOrder() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithCharges())));
        Document document = parse(xml);

        assertEquals("cProd", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[1])"));
        assertEquals("cEAN", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[2])"));
        assertEquals("xProd", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[3])"));
        assertEquals("NCM", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[4])"));
        assertEquals("CEST", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[5])"));
        assertEquals("CFOP", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[6])"));
        assertEquals("uCom", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[7])"));
        assertEquals("qCom", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[8])"));
        assertEquals("vUnCom", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[9])"));
        assertEquals("vProd", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[10])"));
        assertEquals("cEANTrib", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[11])"));
        assertEquals("uTrib", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[12])"));
        assertEquals("qTrib", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[13])"));
        assertEquals("vUnTrib", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[14])"));
        assertEquals("vFrete", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[15])"));
        assertEquals("vSeg", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[16])"));
        assertEquals("vDesc", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[17])"));
        assertEquals("vOutro", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[18])"));
        assertEquals("indTot", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[19])"));
        assertEquals("xPed", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[20])"));
        assertEquals("nItemPed", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[21])"));
    }

    @Test
    void shouldEmitCEanImmediatelyAfterCProd() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertEquals("cProd", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[1])"));
        assertEquals("cEAN", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[2])"));
        assertEquals("7891234567895", xpath(document, "/NFe/infNFe/det[1]/prod/cEAN"));
    }

    @Test
    void shouldEmitCestAndCfopBeforeCommercialValues() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertEquals("CEST", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[5])"));
        assertEquals("CFOP", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[6])"));
        assertEquals("uCom", xpath(document, "name(/NFe/infNFe/det[1]/prod/*[7])"));
    }

    @Test
    void shouldMapFreightInsuranceDiscountAndOtherCharges() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithCharges())));
        Document document = parse(xml);

        assertEquals("1.25", xpath(document, "/NFe/infNFe/det[1]/prod/vFrete"));
        assertEquals("0.50", xpath(document, "/NFe/infNFe/det[1]/prod/vSeg"));
        assertEquals("0.75", xpath(document, "/NFe/infNFe/det[1]/prod/vDesc"));
        assertEquals("0.10", xpath(document, "/NFe/infNFe/det[1]/prod/vOutro"));
    }

    @Test
    void shouldNotEmitNullChargesElements() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/vFrete"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/vSeg"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/vDesc"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/vOutro"));
    }

    @Test
    void shouldMapTotalsToXml() throws Exception {
        String xml = generator.generate(emissionWithTotals());
        Document document = parse(xml);

        assertEquals("10.50", xpath(document, "/NFe/infNFe/total/ICMSTot/vProd"));
        assertEquals("1.25", xpath(document, "/NFe/infNFe/total/ICMSTot/vFrete"));
        assertEquals("0.50", xpath(document, "/NFe/infNFe/total/ICMSTot/vSeg"));
        assertEquals("0.75", xpath(document, "/NFe/infNFe/total/ICMSTot/vDesc"));
        assertEquals("0.10", xpath(document, "/NFe/infNFe/total/ICMSTot/vOutro"));
        assertEquals("11.60", xpath(document, "/NFe/infNFe/total/ICMSTot/vNF"));
    }

    @Test
    void shouldMapTaxTotalsToXml() throws Exception {
        String xml = generator.generate(emissionWithTotals());
        Document document = parse(xml);

        assertEquals("10.50", xpath(document, "/NFe/infNFe/total/ICMSTot/vBC"));
        assertEquals("1.89", xpath(document, "/NFe/infNFe/total/ICMSTot/vICMS"));
        assertEquals("0.00", xpath(document, "/NFe/infNFe/total/ICMSTot/vICMSDeson"));
        assertEquals("0.21", xpath(document, "/NFe/infNFe/total/ICMSTot/vFCP"));
        assertEquals("12.00", xpath(document, "/NFe/infNFe/total/ICMSTot/vBCST"));
        assertEquals("2.16", xpath(document, "/NFe/infNFe/total/ICMSTot/vST"));
        assertEquals("0.24", xpath(document, "/NFe/infNFe/total/ICMSTot/vFCPST"));
        assertEquals("0.05", xpath(document, "/NFe/infNFe/total/ICMSTot/vFCPSTRet"));
        assertEquals("0.30", xpath(document, "/NFe/infNFe/total/ICMSTot/vII"));
        assertEquals("0.50", xpath(document, "/NFe/infNFe/total/ICMSTot/vIPI"));
        assertEquals("0.10", xpath(document, "/NFe/infNFe/total/ICMSTot/vIPIDevol"));
        assertEquals("0.40", xpath(document, "/NFe/infNFe/total/ICMSTot/vPIS"));
        assertEquals("0.60", xpath(document, "/NFe/infNFe/total/ICMSTot/vCOFINS"));
        assertEquals("4.35", xpath(document, "/NFe/infNFe/total/ICMSTot/vTotTrib"));
    }

    @Test
    void shouldPreserveIcmsTotElementOrder() throws Exception {
        String xml = generator.generate(emissionWithTotals());
        Document document = parse(xml);

        assertEquals("vBC", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[1])"));
        assertEquals("vICMS", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[2])"));
        assertEquals("vICMSDeson", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[3])"));
        assertEquals("vFCPUFDest", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[4])"));
        assertEquals("vICMSUFDest", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[5])"));
        assertEquals("vICMSUFRemet", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[6])"));
        assertEquals("vFCP", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[7])"));
        assertEquals("vBCST", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[8])"));
        assertEquals("vST", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[9])"));
        assertEquals("vFCPST", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[10])"));
        assertEquals("vFCPSTRet", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[11])"));
        assertEquals("vProd", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[12])"));
        assertEquals("vFrete", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[13])"));
        assertEquals("vSeg", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[14])"));
        assertEquals("vDesc", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[15])"));
        assertEquals("vII", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[16])"));
        assertEquals("vIPI", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[17])"));
        assertEquals("vIPIDevol", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[18])"));
        assertEquals("vPIS", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[19])"));
        assertEquals("vCOFINS", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[20])"));
        assertEquals("vOutro", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[21])"));
        assertEquals("vNF", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[22])"));
        assertEquals("vTotTrib", xpath(document, "name(/NFe/infNFe/total/ICMSTot/*[23])"));
    }

    @Test
    void shouldEmitTotalAfterDets() throws Exception {
        String xml = generator.generate(emissionWithTotals());
        Document document = parse(xml);

        assertEquals("det", xpath(document, "name(/NFe/infNFe/*[4])"));
        assertEquals("total", xpath(document, "name(/NFe/infNFe/*[5])"));
        assertEquals("ICMSTot", xpath(document, "name(/NFe/infNFe/total/*[1])"));
    }

    @Test
    void shouldNotEmitTotalWhenAbsent() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/total"));
    }

    @Test
    void shouldNotEmitNullOptionalTotalsFields() throws Exception {
        String xml = generator.generate(emissionWithPartialTotals());
        Document document = parse(xml);

        assertEquals("10.50", xpath(document, "/NFe/infNFe/total/ICMSTot/vProd"));
        assertEquals("10.50", xpath(document, "/NFe/infNFe/total/ICMSTot/vNF"));
        assertEquals("10.50", xpath(document, "/NFe/infNFe/total/ICMSTot/vBC"));
        assertEquals("1.89", xpath(document, "/NFe/infNFe/total/ICMSTot/vICMS"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vFrete"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vSeg"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vDesc"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vOutro"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vFCPUFDest"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vICMSUFDest"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vICMSUFRemet"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vBCST"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vST"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vII"));
        assertNull(xpath(document, "/NFe/infNFe/total/ICMSTot/vTotTrib"));
    }

    @Test
    void shouldMapTranspFreightMode() throws Exception {
        String xml = generator.generate(emissionWithTransport());
        Document document = parse(xml);

        assertEquals("0", xpath(document, "/NFe/infNFe/transp/modFrete"));
    }

    @Test
    void shouldMapCarrierDocumentAndName() throws Exception {
        String xml = generator.generate(emissionWithTransport());
        Document document = parse(xml);

        assertEquals("12345678000199", xpath(document, "/NFe/infNFe/transp/transporta/CNPJ"));
        assertEquals("Acme Carrier", xpath(document, "/NFe/infNFe/transp/transporta/xNome"));
    }

    @Test
    void shouldMapCarrierAddress() throws Exception {
        String xml = generator.generate(emissionWithTransport());
        Document document = parse(xml);

        assertEquals("Main St", xpath(document, "/NFe/infNFe/transp/transporta/xEnder"));
        assertEquals("Sao Paulo", xpath(document, "/NFe/infNFe/transp/transporta/xMun"));
        assertEquals("SP", xpath(document, "/NFe/infNFe/transp/transporta/UF"));
    }

    @Test
    void shouldPreserveTranspElementOrder() throws Exception {
        String xml = generator.generate(emissionWithTransport());
        Document document = parse(xml);

        assertEquals("modFrete", xpath(document, "name(/NFe/infNFe/transp/*[1])"));
        assertEquals("transporta", xpath(document, "name(/NFe/infNFe/transp/*[2])"));
        assertEquals("CNPJ", xpath(document, "name(/NFe/infNFe/transp/transporta/*[1])"));
        assertEquals("xNome", xpath(document, "name(/NFe/infNFe/transp/transporta/*[2])"));
        assertEquals("xEnder", xpath(document, "name(/NFe/infNFe/transp/transporta/*[3])"));
        assertEquals("xMun", xpath(document, "name(/NFe/infNFe/transp/transporta/*[4])"));
        assertEquals("UF", xpath(document, "name(/NFe/infNFe/transp/transporta/*[5])"));
    }

    @Test
    void shouldEmitTranspAfterTotal() throws Exception {
        String xml = generator.generate(emissionWithTransport());
        Document document = parse(xml);

        assertEquals("total", xpath(document, "name(/NFe/infNFe/*[5])"));
        assertEquals("transp", xpath(document, "name(/NFe/infNFe/*[6])"));
    }

    @Test
    void shouldNotEmitTranspWhenTransportIsNull() throws Exception {
        String xml = generator.generate(emissionWithTotals());
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/transp"));
    }

    @Test
    void shouldNotEmitEmptyCarrierWhenAbsent() throws Exception {
        String xml = generator.generate(emissionWithFreightModeOnly());
        Document document = parse(xml);

        assertEquals("9", xpath(document, "/NFe/infNFe/transp/modFrete"));
        assertNull(xpath(document, "/NFe/infNFe/transp/transporta"));
    }

    @Test
    void shouldMapPaymentDetailsToXml() throws Exception {
        String xml = generator.generate(emissionWithPayment());
        Document document = parse(xml);

        assertEquals("0", xpath(document, "/NFe/infNFe/pag/detPag[1]/indPag"));
        assertEquals("17", xpath(document, "/NFe/infNFe/pag/detPag[1]/tPag"));
        assertEquals("9.99", xpath(document, "/NFe/infNFe/pag/detPag[1]/vPag"));
        assertEquals("2026-09-21", xpath(document, "/NFe/infNFe/pag/detPag[1]/dPag"));
        assertEquals("1", xpath(document, "/NFe/infNFe/pag/detPag[2]/indPag"));
        assertEquals("01", xpath(document, "/NFe/infNFe/pag/detPag[2]/tPag"));
        assertEquals("1.61", xpath(document, "/NFe/infNFe/pag/detPag[2]/vPag"));
        assertNull(xpath(document, "/NFe/infNFe/pag/detPag[2]/dPag"));
    }

    @Test
    void shouldPreservePagElementOrder() throws Exception {
        String xml = generator.generate(emissionWithPayment());
        Document document = parse(xml);

        assertEquals("detPag", xpath(document, "name(/NFe/infNFe/pag/*[1])"));
        assertEquals("detPag", xpath(document, "name(/NFe/infNFe/pag/*[2])"));
        assertEquals(2.0, countXpath(document, "count(/NFe/infNFe/pag/detPag)"));
        assertEquals("indPag", xpath(document, "name(/NFe/infNFe/pag/detPag[1]/*[1])"));
        assertEquals("tPag", xpath(document, "name(/NFe/infNFe/pag/detPag[1]/*[2])"));
        assertEquals("vPag", xpath(document, "name(/NFe/infNFe/pag/detPag[1]/*[3])"));
        assertEquals("dPag", xpath(document, "name(/NFe/infNFe/pag/detPag[1]/*[4])"));
    }

    @Test
    void shouldEmitPagAfterTransp() throws Exception {
        String xml = generator.generate(emissionWithPayment());
        Document document = parse(xml);

        assertEquals("transp", xpath(document, "name(/NFe/infNFe/*[6])"));
        assertEquals("pag", xpath(document, "name(/NFe/infNFe/*[7])"));
    }

    @Test
    void shouldNotEmitPagWhenPaymentIsNull() throws Exception {
        String xml = generator.generate(emissionWithTotals());
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/pag"));
    }

    @Test
    void shouldPreservePaymentAmountWithoutRecalculation() throws Exception {
        String xml = generator.generate(emissionWithPayment());
        Document document = parse(xml);

        assertEquals("11.60", xpath(document, "/NFe/infNFe/total/ICMSTot/vNF"));
        assertEquals("9.99", xpath(document, "/NFe/infNFe/pag/detPag[1]/vPag"));
    }

    @Test
    void shouldHandleNullableOptionalItemFields() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(minimalItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/cEAN"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/cEANTrib"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/CEST"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/uTrib"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/indTot"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/prod/xPed"));
    }

    @Test
    void shouldPreserveItemOrder() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1(), fullItem2())));
        Document document = parse(xml);

        assertEquals("P-1", xpath(document, "/NFe/infNFe/det[1]/prod/cProd"));
        assertEquals("P-2", xpath(document, "/NFe/infNFe/det[2]/prod/cProd"));
    }

    @Test
    void shouldPreserveExistingIdeEmitDestMapping() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertEquals("55", xpath(document, "/NFe/infNFe/ide/mod"));
        assertEquals("Acme Ltd", xpath(document, "/NFe/infNFe/emit/xNome"));
        assertEquals("Beta Corp", xpath(document, "/NFe/infNFe/dest/xNome"));
    }

    @Test
    void shouldMapEmitTradeNameAndTaxRegime() throws Exception {
        String xml = generator.generate(emissionWithParties(issuerWithTradeNameAndTaxRegime(), recipient()));
        Document document = parse(xml);

        assertEquals("Acme Comercio Ltda", xpath(document, "/NFe/infNFe/emit/xFant"));
        assertEquals("3", xpath(document, "/NFe/infNFe/emit/CRT"));
    }

    @Test
    void shouldPreserveEmitElementOrder() throws Exception {
        String xml = generator.generate(emissionWithParties(issuerWithTradeNameAndTaxRegime(), recipient()));
        Document document = parse(xml);

        assertEquals("CNPJ", xpath(document, "name(/NFe/infNFe/emit/*[1])"));
        assertEquals("xNome", xpath(document, "name(/NFe/infNFe/emit/*[2])"));
        assertEquals("xFant", xpath(document, "name(/NFe/infNFe/emit/*[3])"));
        assertEquals("enderEmit", xpath(document, "name(/NFe/infNFe/emit/*[4])"));
        assertEquals("IE", xpath(document, "name(/NFe/infNFe/emit/*[5])"));
        assertEquals("CRT", xpath(document, "name(/NFe/infNFe/emit/*[6])"));
    }

    @Test
    void shouldMapEmitAddressNeighborhoodAndMunicipalityCode() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("Centro", xpath(document, "/NFe/infNFe/emit/enderEmit/xBairro"));
        assertEquals("3550308", xpath(document, "/NFe/infNFe/emit/enderEmit/cMun"));
    }

    @Test
    void shouldMapDestAddressNeighborhoodAndMunicipalityCode() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("Centro", xpath(document, "/NFe/infNFe/dest/enderDest/xBairro"));
        assertEquals("3550308", xpath(document, "/NFe/infNFe/dest/enderDest/cMun"));
    }

    @Test
    void shouldNotEmitEmptyAddressElementsWhenNull() throws Exception {
        Issuer issuer = new Issuer("Acme Ltd", "12345678000199", "123456789",
                new Address("Main St", "100", "Sao Paulo", "SP", "01310-100", null, null),
                null, null);
        String xml = generator.generate(emissionWithParties(issuer, recipient()));
        Document document = parse(xml);

        assertEquals("Main St", xpath(document, "/NFe/infNFe/emit/enderEmit/xLgr"));
        assertNull(xpath(document, "/NFe/infNFe/emit/enderEmit/xBairro"));
        assertNull(xpath(document, "/NFe/infNFe/emit/enderEmit/cMun"));
    }

    @Test
    void shouldPreserveEnderEmitElementOrder() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("xLgr", xpath(document, "name(/NFe/infNFe/emit/enderEmit/*[1])"));
        assertEquals("nro", xpath(document, "name(/NFe/infNFe/emit/enderEmit/*[2])"));
        assertEquals("xBairro", xpath(document, "name(/NFe/infNFe/emit/enderEmit/*[3])"));
        assertEquals("cMun", xpath(document, "name(/NFe/infNFe/emit/enderEmit/*[4])"));
        assertEquals("xMun", xpath(document, "name(/NFe/infNFe/emit/enderEmit/*[5])"));
        assertEquals("UF", xpath(document, "name(/NFe/infNFe/emit/enderEmit/*[6])"));
        assertEquals("CEP", xpath(document, "name(/NFe/infNFe/emit/enderEmit/*[7])"));
    }

    @Test
    void shouldPreserveEnderDestElementOrder() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("xLgr", xpath(document, "name(/NFe/infNFe/dest/enderDest/*[1])"));
        assertEquals("nro", xpath(document, "name(/NFe/infNFe/dest/enderDest/*[2])"));
        assertEquals("xBairro", xpath(document, "name(/NFe/infNFe/dest/enderDest/*[3])"));
        assertEquals("cMun", xpath(document, "name(/NFe/infNFe/dest/enderDest/*[4])"));
        assertEquals("xMun", xpath(document, "name(/NFe/infNFe/dest/enderDest/*[5])"));
        assertEquals("UF", xpath(document, "name(/NFe/infNFe/dest/enderDest/*[6])"));
        assertEquals("CEP", xpath(document, "name(/NFe/infNFe/dest/enderDest/*[7])"));
    }

    @Test
    void shouldMapIndIeDestNumericCodes() throws Exception {
        Recipient contributor = new Recipient("Beta Corp", "98765432000188", address(), RecipientIeStatus.CONTRIBUTOR);
        Recipient exempt = new Recipient("Beta Corp", "98765432000188", address(), RecipientIeStatus.EXEMPT);
        Recipient notContributor = new Recipient("Beta Corp", "98765432000188", address(), RecipientIeStatus.NOT_CONTRIBUTOR);

        assertEquals("1", xpath(parse(generator.generate(emissionWithParties(issuer(), contributor))),
                "/NFe/infNFe/dest/indIEDest"));
        assertEquals("2", xpath(parse(generator.generate(emissionWithParties(issuer(), exempt))),
                "/NFe/infNFe/dest/indIEDest"));
        assertEquals("9", xpath(parse(generator.generate(emissionWithParties(issuer(), notContributor))),
                "/NFe/infNFe/dest/indIEDest"));
    }

    @Test
    void shouldPreserveDestElementOrder() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipientWithIeStatus()));
        Document document = parse(xml);

        assertEquals("CNPJ", xpath(document, "name(/NFe/infNFe/dest/*[1])"));
        assertEquals("xNome", xpath(document, "name(/NFe/infNFe/dest/*[2])"));
        assertEquals("enderDest", xpath(document, "name(/NFe/infNFe/dest/*[3])"));
        assertEquals("indIEDest", xpath(document, "name(/NFe/infNFe/dest/*[4])"));
    }

    @Test
    void shouldNotEmitIndIeDestWhenStatusIsNull() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("Beta Corp", xpath(document, "/NFe/infNFe/dest/xNome"));
        assertEquals("98765432000188", xpath(document, "/NFe/infNFe/dest/CNPJ"));
        assertNull(xpath(document, "/NFe/infNFe/dest/indIEDest"));
    }

    @Test
    void shouldNotEmitNullOptionalEmitFields() throws Exception {
        String xml = generator.generate(emissionWithParties(issuer(), recipient()));
        Document document = parse(xml);

        assertEquals("Acme Ltd", xpath(document, "/NFe/infNFe/emit/xNome"));
        assertEquals("12345678000199", xpath(document, "/NFe/infNFe/emit/CNPJ"));
        assertEquals("123456789", xpath(document, "/NFe/infNFe/emit/IE"));
        assertNull(xpath(document, "/NFe/infNFe/emit/xFant"));
        assertNull(xpath(document, "/NFe/infNFe/emit/CRT"));
    }

    @Test
    void shouldKeepIssuerWithoutOptionalFields() throws Exception {
        String xml = generator.generate(emissionWithParties(issuerWithoutAddress(), recipient()));
        Document document = parse(xml);

        assertEquals("Acme Ltd", xpath(document, "/NFe/infNFe/emit/xNome"));
        assertEquals("12345678000199", xpath(document, "/NFe/infNFe/emit/CNPJ"));
        assertNull(xpath(document, "/NFe/infNFe/emit/xFant"));
        assertNull(xpath(document, "/NFe/infNFe/emit/enderEmit"));
        assertNull(xpath(document, "/NFe/infNFe/emit/IE"));
        assertNull(xpath(document, "/NFe/infNFe/emit/CRT"));
    }

    @Test
    void shouldGenerateImpostoWhenTaxationExists() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
    }

    @Test
    void shouldNotGenerateEmptyImpostoWhenTaxationIsNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(fullItem1())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto"));
    }

    @Test
    void shouldMapIcmsCst() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
    }

    @Test
    void shouldMapIcmsBase() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("3", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/modBC"));
        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/vBC"));
    }

    @Test
    void shouldMapIcmsRate() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("18.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/pICMS"));
    }

    @Test
    void shouldMapIcmsAmount() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("18.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/vICMS"));
    }

    @Test
    void shouldMapFcp() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("2.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/pFCP"));
        assertEquals("2.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/vFCP"));
    }

    @Test
    void shouldMapIcmsStWhenAvailable() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithSt())));
        Document document = parse(xml);

        assertEquals("4", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/modBCST"));
        assertEquals("40.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/pMVAST"));
        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/pRedBCST"));
        assertEquals("120.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/vBCST"));
        assertEquals("18.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/pICMSST"));
        assertEquals("21.60", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/vICMSST"));
        assertEquals("120.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/vBCFCPST"));
        assertEquals("2.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/pFCPST"));
        assertEquals("2.40", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/vFCPST"));
    }

    @Test
    void shouldEmitCstInsideSelectedVariant() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("ICMS00", xpath(document, "name(/NFe/infNFe/det[1]/imposto/ICMS/*[1])"));
        assertEquals("orig", xpath(document, "name(/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/*[1])"));
        assertEquals("CST", xpath(document, "name(/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/*[2])"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/CST"));
    }

    @Test
    void shouldMapOrigFromItemOrigin() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("0", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/orig"));
    }

    @Test
    void shouldMapIcms40DesonerationVariant() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(icms40Item())));
        Document document = parse(xml);

        assertEquals("ICMS40", xpath(document, "name(/NFe/infNFe/det[1]/imposto/ICMS/*[1])"));
        assertEquals("40", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS40/CST"));
        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS40/vICMSDeson"));
        assertEquals("40", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS40/motDesICMS"));
    }

    @Test
    void shouldMapIcms30Desoneration() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithSt())));
        Document document = parse(xml);

        assertEquals("0.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/vICMSDeson"));
        assertEquals("40", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS30/motDesICMS"));
    }

    @Test
    void shouldRejectUnsupportedIcmsCst() {
        NfeItem item = itemWithIcms(new IcmsTax("90", "3",
                new BigDecimal("100.00"), new BigDecimal("18.00"), new BigDecimal("18.00"),
                new BigDecimal("2.00"), new BigDecimal("2.00"), null, null, null));

        assertThrows(IllegalStateException.class,
                () -> generator.generate(emissionWithItems(List.of(item))));
    }

    @Test
    void shouldRejectIcms00WithStData() {
        NfeItem item = itemWithIcms(new IcmsTax("00", "3",
                new BigDecimal("100.00"), new BigDecimal("18.00"), new BigDecimal("18.00"),
                new BigDecimal("2.00"), new BigDecimal("2.00"), icmsSt(), null, null));

        assertThrows(IllegalStateException.class,
                () -> generator.generate(emissionWithItems(List.of(item))));
    }

    @Test
    void shouldRejectIcms40WithoutDesoneration() {
        NfeItem item = itemWithIcms(new IcmsTax("40", "3", null, null, null,
                null, null, null, null, null));

        assertThrows(IllegalStateException.class,
                () -> generator.generate(emissionWithItems(List.of(item))));
    }

    @Test
    void shouldPreserveProductMapping() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("P-1", xpath(document, "/NFe/infNFe/det[1]/prod/cProd"));
        assertEquals("Widget", xpath(document, "/NFe/infNFe/det[1]/prod/xProd"));
        assertEquals("10.50", xpath(document, "/NFe/infNFe/det[1]/prod/vProd"));
    }

    @Test
    void shouldPreserveExistingIdeEmitDestMappingWithTaxation() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("55", xpath(document, "/NFe/infNFe/ide/mod"));
        assertEquals("Acme Ltd", xpath(document, "/NFe/infNFe/emit/xNome"));
        assertEquals("Beta Corp", xpath(document, "/NFe/infNFe/dest/xNome"));
    }

    @Test
    void shouldGenerateIpiWhenTaxationContainsIpi() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(ipiOnlyItem())));
        Document document = parse(xml);

        assertEquals("50", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/CST"));
    }

    @Test
    void shouldNotGenerateIpiWhenIpiIsNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IPI"));
    }

    @Test
    void shouldMapIpiFields() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIpi())));
        Document document = parse(xml);

        assertEquals("50", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/CST"));
        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/vBC"));
        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/pIPI"));
        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/vIPI"));
    }

    @Test
    void shouldPreserveBigDecimalValues() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIpi())));
        Document document = parse(xml);

        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/vBC"));
        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/pIPI"));
        assertFalse(xml.contains("E+"), "values must not be converted to scientific notation");
        assertFalse(xml.contains("E-"), "values must not be converted to scientific notation");
    }

    @Test
    void shouldPreserveExistingIcmsMapping() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIpi())));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/vBC"));
        assertEquals("18.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/vICMS"));
    }

    @Test
    void shouldGenerateIcmsAndIpiTogether() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIpi())));
        Document document = parse(xml);

        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/ICMS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IPI)"));
    }

    @Test
    void shouldNotGenerateEmptyIpi() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IPI"));
    }

    @Test
    void shouldPreserveExistingProductMappingWithIpi() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIpi())));
        Document document = parse(xml);

        assertEquals("P-1", xpath(document, "/NFe/infNFe/det[1]/prod/cProd"));
        assertEquals("Widget", xpath(document, "/NFe/infNFe/det[1]/prod/xProd"));
        assertEquals("10.50", xpath(document, "/NFe/infNFe/det[1]/prod/vProd"));
    }

    @Test
    void shouldGeneratePisWhenPisCofinsExists() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(pisCofinsOnlyItem())));
        Document document = parse(xml);

        assertEquals("01", xpath(document, "/NFe/infNFe/det[1]/imposto/PIS/CST"));
    }

    @Test
    void shouldGenerateCofinsWhenPisCofinsExists() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(pisCofinsOnlyItem())));
        Document document = parse(xml);

        assertEquals("01", xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS/CST"));
    }

    @Test
    void shouldNotGeneratePisCofinsWhenTaxIsNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/PIS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS"));
    }

    @Test
    void shouldMapPisFields() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithPisCofins())));
        Document document = parse(xml);

        assertEquals("01", xpath(document, "/NFe/infNFe/det[1]/imposto/PIS/CST"));
        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/PIS/vBC"));
        assertEquals("1.65", xpath(document, "/NFe/infNFe/det[1]/imposto/PIS/pPIS"));
        assertEquals("1.65", xpath(document, "/NFe/infNFe/det[1]/imposto/PIS/vPIS"));
    }

    @Test
    void shouldMapCofinsFields() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithPisCofins())));
        Document document = parse(xml);

        assertEquals("01", xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS/CST"));
        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS/vBC"));
        assertEquals("7.60", xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS/pCOFINS"));
        assertEquals("7.60", xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS/vCOFINS"));
    }

    @Test
    void shouldGenerateIcmsIpiPisAndCofinsTogether() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithPisCofins())));
        Document document = parse(xml);

        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/ICMS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IPI)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/PIS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/COFINS)"));
    }

    @Test
    void shouldGenerateOnlyPisCofinsWhenOtherTaxesAreNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(pisCofinsOnlyItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IPI"));
        assertEquals("01", xpath(document, "/NFe/infNFe/det[1]/imposto/PIS/CST"));
        assertEquals("01", xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS/CST"));
    }

    @Test
    void shouldNotGenerateEmptyPis() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(cofinsOnlyItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/PIS"));
        assertEquals("01", xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS/CST"));
    }

    @Test
    void shouldNotGenerateEmptyCofins() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(pisOnlyItem())));
        Document document = parse(xml);

        assertEquals("01", xpath(document, "/NFe/infNFe/det[1]/imposto/PIS/CST"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS"));
    }

    @Test
    void shouldPreserveExistingIcmsMappingWithPisCofins() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithPisCofins())));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
        assertEquals("18.00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/vICMS"));
    }

    @Test
    void shouldPreserveExistingIpiMappingWithPisCofins() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithPisCofins())));
        Document document = parse(xml);

        assertEquals("50", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/CST"));
        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/vIPI"));
    }

    @Test
    void shouldPreserveExistingProductMappingWithPisCofins() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithPisCofins())));
        Document document = parse(xml);

        assertEquals("P-1", xpath(document, "/NFe/infNFe/det[1]/prod/cProd"));
        assertEquals("Widget", xpath(document, "/NFe/infNFe/det[1]/prod/xProd"));
        assertEquals("10.50", xpath(document, "/NFe/infNFe/det[1]/prod/vProd"));
    }

    @Test
    void shouldGenerateIiWhenImportTaxExists() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIi())));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/II/vII"));
    }

    @Test
    void shouldNotGenerateIiWhenImportTaxIsNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/II"));
    }

    @Test
    void shouldMapImportTaxBase() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIi())));
        Document document = parse(xml);

        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/II/vBC"));
    }

    @Test
    void shouldNotGenerateImportTaxRateElement() throws Exception {
        // Official schema verified: the II group contains vBC, vDespAdu, vII
        // and vIOF only — there is no pII rate element. ImportTax.taxRate()
        // is deliberately unmapped.
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIi())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/II/pII"));
    }

    @Test
    void shouldMapImportTaxAmount() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIi())));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/II/vII"));
    }

    @Test
    void shouldMapCustomsExpenses() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIi())));
        Document document = parse(xml);

        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/II/vDespAdu"));
    }

    @Test
    void shouldMapIofAmount() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIi())));
        Document document = parse(xml);

        assertEquals("1.50", xpath(document, "/NFe/infNFe/det[1]/imposto/II/vIOF"));
    }

    @Test
    void shouldGenerateIiTogetherWithExistingTaxes() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIi())));
        Document document = parse(xml);

        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/ICMS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IPI)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/II)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/PIS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/COFINS)"));
    }

    @Test
    void shouldGenerateIiOnly() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(iiOnlyItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IPI"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/PIS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS"));
        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/II/vII"));
    }

    @Test
    void shouldNotGenerateEmptyIi() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(emptyImportTaxItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto"));
    }

    @Test
    void shouldPreserveExistingTaxOrder() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIi())));
        Document document = parse(xml);

        assertEquals("ICMS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/*[1])"));
        assertEquals("IPI", xpath(document, "name(/NFe/infNFe/det[1]/imposto/*[2])"));
        assertEquals("II", xpath(document, "name(/NFe/infNFe/det[1]/imposto/*[3])"));
        assertEquals("PIS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/*[4])"));
        assertEquals("COFINS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/*[5])"));
    }

    @Test
    void shouldPreserveExistingProductMappingWithIi() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIi())));
        Document document = parse(xml);

        assertEquals("P-1", xpath(document, "/NFe/infNFe/det[1]/prod/cProd"));
        assertEquals("Widget", xpath(document, "/NFe/infNFe/det[1]/prod/xProd"));
        assertEquals("10.50", xpath(document, "/NFe/infNFe/det[1]/prod/vProd"));
    }

    @Test
    void shouldGenerateIsWhenIsTaxExists() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(isOnlyItem())));
        Document document = parse(xml);

        assertEquals("20.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/vIS"));
    }

    @Test
    void shouldNotGenerateIsWhenIsIsNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IS"));
    }

    @Test
    void shouldMapAllIsFields() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIs())));
        Document document = parse(xml);

        assertEquals("900", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/CSTIS"));
        assertEquals("001000", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/cClassTribIS"));
        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/vBCIS"));
        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/pIS"));
        assertEquals("1.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/adRemIS"));
        assertEquals("UN", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/uTrib"));
        assertEquals("2", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/qTrib"));
        assertEquals("20.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/vIS"));
    }

    @Test
    void shouldGenerateIsOnly() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(isOnlyItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IPI"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/II"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/PIS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS"));
        assertEquals("20.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/vIS"));
    }

    @Test
    void shouldGenerateIcmsAndIsTogether() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(icmsIsItem())));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
        assertEquals("20.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/vIS"));
    }

    @Test
    void shouldGenerateIsWithIpiPisCofins() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIs())));
        Document document = parse(xml);

        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/ICMS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IPI)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/II)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/PIS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/COFINS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IS)"));
    }

    @Test
    void shouldNotGenerateUnsupportedIsElements() throws Exception {
        // The official TIS type has no pISEspec element.
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIs())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IS/pISEspec"));
    }

    @Test
    void shouldPreserveIsElementOrder() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIs())));
        Document document = parse(xml);

        assertEquals("CSTIS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IS/*[1])"));
        assertEquals("cClassTribIS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IS/*[2])"));
        assertEquals("vBCIS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IS/*[3])"));
        assertEquals("pIS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IS/*[4])"));
        assertEquals("adRemIS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IS/*[5])"));
        assertEquals("uTrib", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IS/*[6])"));
        assertEquals("qTrib", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IS/*[7])"));
        assertEquals("vIS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IS/*[8])"));
        assertEquals("IS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/*[6])"));
    }

    @Test
    void shouldPreserveBigDecimalValuesInIs() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithIs())));
        Document document = parse(xml);

        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/vBCIS"));
        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/pIS"));
        assertEquals("1.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/adRemIS"));
        assertEquals("20.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IS/vIS"));
        assertFalse(xml.contains("E+"), "values must not be converted to scientific notation");
    }

    @Test
    void shouldNotGenerateEmptyIs() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(emptyIsItem())));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IS"));
    }

    @Test
    void shouldGenerateIbsCbsWithCstAndCClassTrib() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(rtcOnlyItem())));
        Document document = parse(xml);

        assertEquals("300", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/CST"));
        assertEquals("001000", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/cClassTrib"));
    }

    @Test
    void shouldGenerateIbsCbsWithIndDoacao() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(rtcOnlyItem())));
        Document document = parse(xml);

        assertEquals("1", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/indDoacao"));
    }

    @Test
    void shouldGenerateIbsCbsWithVbc() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(rtcOnlyItem())));
        Document document = parse(xml);

        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vBC"));
    }

    @Test
    void shouldGenerateIbsCbsOnly() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(rtcOnlyItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IPI"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/II"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/PIS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/COFINS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IS"));
        assertEquals("300", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/CST"));
    }

    @Test
    void shouldGenerateIcmsAndIbsCbsTogether() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(icmsRtcItem())));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
        assertEquals("300", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/CST"));
    }

    @Test
    void shouldGenerateExistingTaxesAndIbsCbsTogether() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithRtc())));
        Document document = parse(xml);

        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/ICMS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IPI)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/II)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/PIS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/COFINS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS)"));
    }

    @Test
    void shouldNotGenerateIbsCbsWithoutRtc() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS"));
    }

    @Test
    void shouldNotGenerateIbsCbsWhenIbsCbsIsNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(rtcNoIbsCbsItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS"));
    }

    @Test
    void shouldNotGenerateIbsCbsWhenRtcIsNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(ipiOnlyItem())));
        Document document = parse(xml);

        assertEquals("50", xpath(document, "/NFe/infNFe/det[1]/imposto/IPI/CST"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS"));
    }

    @Test
    void shouldNotGenerateEmptyIbsCbs() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(rtcEmptyItem())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto"));
    }

    @Test
    void shouldPreserveIbsCbsElementOrder() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithRtc())));
        Document document = parse(xml);

        assertEquals("IBSCBS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/*[7])"));
        assertEquals("CST", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/*[1])"));
        assertEquals("cClassTrib", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/*[2])"));
        assertEquals("indDoacao", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/*[3])"));
        assertEquals("gIBSCBS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/*[4])"));
        assertEquals("vBC", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[1])"));
    }

    @Test
    void shouldPreserveBigDecimalValuesInIbsCbs() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithRtc())));
        Document document = parse(xml);

        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vBC"));
        assertFalse(xml.contains("E+"), "values must not be converted to scientific notation");
    }

    @Test
    void shouldNotGenerateDeferredRtcGroups() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithRtc())));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gCBS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gTribRegular"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gDif"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gDevTrib"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gRed"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gEstornoCred"));
    }

    @Test
    void shouldMapIbsUfRateOnly() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufRateOnly()))));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/vIBSUF"));
    }

    @Test
    void shouldMapIbsUfAmountOnly() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufAmountOnly()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
        assertEquals("8.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/vIBSUF"));
    }

    @Test
    void shouldMapIbsUfRateAndAmount() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufRateAmount()))));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
        assertEquals("8.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/vIBSUF"));
    }

    @Test
    void shouldMapIbsUfWithGdif() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufDif()))));
        Document document = parse(xml);

        assertEquals("2.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif/pDif"));
        assertEquals("1.60", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif/vDif"));
    }

    @Test
    void shouldMapIbsUfWithGdevTrib() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufDevTrib()))));
        Document document = parse(xml);

        assertEquals("1.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib/pDevTrib"));
        assertEquals("0.80", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib/vDevTrib"));
    }

    @Test
    void shouldMapIbsUfWithGred() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufRed()))));
        Document document = parse(xml);

        assertEquals("30.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gRed/pRedAliq"));
        assertEquals("7.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gRed/pAliqEfet"));
    }

    @Test
    void shouldMapIbsUfWithAllConditionalGroups() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufAll()))));
        Document document = parse(xml);

        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gRed)"));
    }

    @Test
    void shouldGenerateIbsCbsWithIbsUf() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(ufOnlyRtcItem(ufRateAmount()))));
        Document document = parse(xml);

        assertEquals("300", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/CST"));
        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
    }

    @Test
    void shouldGenerateIcmsWithIbsUf() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufRateAmount()))));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
    }

    @Test
    void shouldGenerateAllCurrentTaxesWithIbsUf() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithUf(ufAll()))));
        Document document = parse(xml);

        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/ICMS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IPI)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/II)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/PIS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/COFINS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF)"));
    }

    @Test
    void shouldNotGenerateIbsUfWhenNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithRtc())));
        Document document = parse(xml);

        assertEquals("300", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/CST"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF"));
    }

    @Test
    void shouldNotGenerateGdifWhenDeferralNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufNoDif()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib)"));
    }

    @Test
    void shouldNotGenerateGdevTribWhenDevolutionNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufNoDevTrib()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif)"));
    }

    @Test
    void shouldNotGenerateGredWhenReductionNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufNoRed()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gRed"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif)"));
    }

    @Test
    void shouldNotGenerateEmptyGIbsUf() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufEmpty()))));
        Document document = parse(xml);

        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vBC"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF"));
    }

    @Test
    void shouldNotGenerateEmptyGdif() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufEmptySubgroups()))));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif"));
    }

    @Test
    void shouldNotGenerateEmptyGdevTrib() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufEmptySubgroups()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib"));
    }

    @Test
    void shouldNotGenerateEmptyGred() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsUf(ufEmptySubgroups()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gRed"));
    }

    @Test
    void shouldPreserveBigDecimalValuesInIbsUf() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithUf(ufAll()))));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
        assertEquals("2.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif/pDif"));
        assertEquals("1.60", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif/vDif"));
        assertEquals("1.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib/pDevTrib"));
        assertEquals("0.80", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib/vDevTrib"));
        assertEquals("30.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gRed/pRedAliq"));
        assertEquals("7.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gRed/pAliqEfet"));
        assertEquals("8.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/vIBSUF"));
        assertFalse(xml.contains("E+"), "values must not be converted to scientific notation");
    }

    @Test
    void shouldPreserveIbsUfElementOrder() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithUf(ufAll()))));
        Document document = parse(xml);

        assertEquals("vBC", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[1])"));
        assertEquals("gIBSUF", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[2])"));
        assertEquals("pIBSUF", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/*[1])"));
        assertEquals("gDif", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/*[2])"));
        assertEquals("gDevTrib", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/*[3])"));
        assertEquals("gRed", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/*[4])"));
        assertEquals("vIBSUF", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/*[5])"));
        assertEquals("pDif", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif/*[1])"));
        assertEquals("vDif", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDif/*[2])"));
        assertEquals("pDevTrib", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib/*[1])"));
        assertEquals("vDevTrib", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gDevTrib/*[2])"));
        assertEquals("pRedAliq", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gRed/*[1])"));
        assertEquals("pAliqEfet", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/gRed/*[2])"));
    }

    @Test
    void shouldMapIbsMunRateOnly() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munRateOnly()))));
        Document document = parse(xml);

        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/vIBSMun"));
    }

    @Test
    void shouldMapIbsMunAmountOnly() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munAmountOnly()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
        assertEquals("3.40", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/vIBSMun"));
    }

    @Test
    void shouldMapIbsMunRateAndAmount() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munRateAmount()))));
        Document document = parse(xml);

        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
        assertEquals("3.40", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/vIBSMun"));
    }

    @Test
    void shouldMapIbsMunWithGdif() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munDif()))));
        Document document = parse(xml);

        assertEquals("1.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDif/pDif"));
        assertEquals("0.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDif/vDif"));
    }

    @Test
    void shouldMapIbsMunWithGdevTrib() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munDevTrib()))));
        Document document = parse(xml);

        assertEquals("0.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDevTrib/pDevTrib"));
        assertEquals("0.20", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDevTrib/vDevTrib"));
    }

    @Test
    void shouldMapIbsMunWithGred() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munRed()))));
        Document document = parse(xml);

        assertEquals("20.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gRed/pRedAliq"));
        assertEquals("4.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gRed/pAliqEfet"));
    }

    @Test
    void shouldMapIbsMunWithAllConditionalGroups() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munAll()))));
        Document document = parse(xml);

        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDif)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDevTrib)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gRed)"));
    }

    @Test
    void shouldGenerateIbsCbsWithIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(munOnlyRtcItem(munRateAmount()))));
        Document document = parse(xml);

        assertEquals("300", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/CST"));
        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
    }

    @Test
    void shouldGenerateIbsCbsWithIbsUfAndIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithUfAndMun())));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
        assertEquals("gIBSUF", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[2])"));
        assertEquals("gIBSMun", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[3])"));
    }

    @Test
    void shouldGenerateIcmsWithIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munRateAmount()))));
        Document document = parse(xml);

        assertEquals("00", xpath(document, "/NFe/infNFe/det[1]/imposto/ICMS/ICMS00/CST"));
        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
    }

    @Test
    void shouldGenerateAllCurrentTaxesWithIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithMun(munAll()))));
        Document document = parse(xml);

        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/ICMS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IPI)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/II)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/PIS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/COFINS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun)"));
    }

    @Test
    void shouldNotGenerateIbsMunWhenNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithRtc())));
        Document document = parse(xml);

        assertEquals("300", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/CST"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun"));
    }

    @Test
    void shouldNotGenerateGdifInIbsMunWhenDeferralNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munNoDif()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDif"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDevTrib)"));
    }

    @Test
    void shouldNotGenerateGdevTribInIbsMunWhenDevolutionNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munNoDevTrib()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDevTrib"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDif)"));
    }

    @Test
    void shouldNotGenerateGredInIbsMunWhenReductionNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munNoRed()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gRed"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDif)"));
    }

    @Test
    void shouldNotGenerateEmptyGIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munEmpty()))));
        Document document = parse(xml);

        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vBC"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun"));
    }

    @Test
    void shouldNotGenerateEmptyGdifInIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munEmptySubgroups()))));
        Document document = parse(xml);

        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDif"));
    }

    @Test
    void shouldNotGenerateEmptyGdevTribInIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munEmptySubgroups()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDevTrib"));
    }

    @Test
    void shouldNotGenerateEmptyGredInIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munEmptySubgroups()))));
        Document document = parse(xml);

        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gRed"));
    }

    @Test
    void shouldPreserveBigDecimalValuesInIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithMun(munAll()))));
        Document document = parse(xml);

        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
        assertEquals("1.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDif/pDif"));
        assertEquals("0.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDif/vDif"));
        assertEquals("0.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDevTrib/pDevTrib"));
        assertEquals("0.20", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gDevTrib/vDevTrib"));
        assertEquals("20.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gRed/pRedAliq"));
        assertEquals("4.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/gRed/pAliqEfet"));
        assertEquals("3.40", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/vIBSMun"));
        assertFalse(xml.contains("E+"), "values must not be converted to scientific notation");
    }

    @Test
    void shouldPreserveIbsMunElementOrder() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithIbsMun(munAll()))));
        Document document = parse(xml);

        assertEquals("vBC", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[1])"));
        assertEquals("gIBSMun", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[2])"));
        assertEquals("pIBSMun", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/*[1])"));
        assertEquals("gDif", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/*[2])"));
        assertEquals("gDevTrib", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/*[3])"));
        assertEquals("gRed", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/*[4])"));
        assertEquals("vIBSMun", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/*[5])"));
    }

    @Test
    void shouldMapVIbsWithOnlyIbsAmount() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithVIbs(null, null, new BigDecimal("99.99")))));
        Document document = parse(xml);

        assertEquals("99.99", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vIBS"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun"));
    }

    @Test
    void shouldMapVIbsWithIbsUf() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithVIbs(ufRateAmount(), null, new BigDecimal("99.99")))));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
        assertEquals("99.99", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vIBS"));
    }

    @Test
    void shouldMapVIbsWithIbsMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithVIbs(null, munRateAmount(), new BigDecimal("99.99")))));
        Document document = parse(xml);

        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
        assertEquals("99.99", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vIBS"));
    }

    @Test
    void shouldMapVIbsWithUfAndMun() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithVIbs(ufRateAmount(), munRateAmount(), new BigDecimal("99.99")))));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
        assertEquals("5.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/pIBSMun"));
        assertEquals("99.99", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vIBS"));
    }

    @Test
    void shouldMapVIbsWithAllCurrentCoreGroups() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithVIbs(ufAll(), munAll(), new BigDecimal("99.99")))));
        Document document = parse(xml);

        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vBC"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF)"));
        assertEquals(1.0, countXpath(document, "count(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun)"));
        assertEquals("99.99", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vIBS"));
    }

    @Test
    void shouldNotGenerateVIbsWhenIbsAmountNull() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithVIbs(ufRateAmount(), null, null))));
        Document document = parse(xml);

        assertEquals("10.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/pIBSUF"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vIBS"));
    }

    @Test
    void shouldPreserveBigDecimalValuesInVIbs() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithVIbs(ufAll(), munAll(), new BigDecimal("99.99")))));
        Document document = parse(xml);

        assertEquals("99.99", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vIBS"));
        assertFalse(xml.contains("E+"), "values must not be converted to scientific notation");
    }

    @Test
    void shouldNotCalculateVIbsFromUfAndMun() throws Exception {
        // uf.amount = 8.50, mun.amount = 3.40 (sum = 11.90), but ibsAmount = 99.99.
        // The mapper must use the explicit domain value, never the sum.
        String xml = generator.generate(emissionWithItems(List.of(itemWithVIbs(ufRateAmount(), munRateAmount(), new BigDecimal("99.99")))));
        Document document = parse(xml);

        assertEquals("8.50", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSUF/vIBSUF"));
        assertEquals("3.40", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/gIBSMun/vIBSMun"));
        assertEquals("99.99", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vIBS"));
    }

    @Test
    void shouldPreserveVIbsElementOrder() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(itemWithVIbs(ufRateAmount(), munRateAmount(), new BigDecimal("99.99")))));
        Document document = parse(xml);

        assertEquals("vBC", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[1])"));
        assertEquals("gIBSUF", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[2])"));
        assertEquals("gIBSMun", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[3])"));
        assertEquals("vIBS", xpath(document, "name(/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/*[4])"));
    }

    @Test
    void shouldPreserveExistingIbsCbsBehaviorWithVIbs() throws Exception {
        String xml = generator.generate(emissionWithItems(List.of(taxedItemWithRtc())));
        Document document = parse(xml);

        assertEquals("300", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/CST"));
        assertEquals("001000", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/cClassTrib"));
        assertEquals("100.00", xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vBC"));
        assertNull(xpath(document, "/NFe/infNFe/det[1]/imposto/IBSCBS/gIBSCBS/vIBS"));
    }

    private static NfeEmission emissionWithHeader() {
        return emission("55", 3, 1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrialização", "3550308");
    }

    private static NfeEmission emissionWithFullIde() {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrialização",
                "3550308",
                "35", "00000001", "1", "1", "1", "1", "9", "2", "1", "1", "1", "0", "1.0",
                issuer(),
                recipient(),
                List.of(item()),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private static NfeEmission emissionWithNullHeader() {
        return emission(null, null, null, null, null, null);
    }

    private static NfeEmission emission(
            String model,
            Integer series,
            Long number,
            OffsetDateTime emissionDate,
            String operationDescription,
            String fiscalEstablishmentCity) {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                model,
                series,
                number,
                emissionDate,
                operationDescription,
                fiscalEstablishmentCity,
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                null,
                null,
                List.of(item()),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private static NfeEmission emissionWithParties(Issuer issuer, Recipient recipient) {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializa\u00e7\u00e3o",
                "3550308",
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer,
                recipient,
                List.of(item()),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private static Address address() {
        return new Address("Main St", "100", "Sao Paulo", "SP", "01310-100", "Centro", "3550308");
    }

    private static Issuer issuer() {
        return new Issuer("Acme Ltd", "12345678000199", "123456789", address(), null, null);
    }

    private static Issuer issuerWithTradeNameAndTaxRegime() {
        return new Issuer("Acme Ltd", "12345678000199", "123456789", address(),
                "Acme Comercio Ltda", "3");
    }

    private static Issuer issuerWithoutAddress() {
        return new Issuer("Acme Ltd", "12345678000199", null, null, null, null);
    }

    private static Recipient recipient() {
        return new Recipient("Beta Corp", "98765432000188", address(), null);
    }

    private static Recipient recipientWithIeStatus() {
        return new Recipient("Beta Corp", "98765432000188", address(), RecipientIeStatus.CONTRIBUTOR);
    }

    private static Recipient recipientWithoutAddress() {
        return new Recipient("Beta Corp", "98765432000188", null, null);
    }

    private static NfeEmission emissionWithItems(List<NfeItem> items) {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializa\u00e7\u00e3o",
                "3550308",
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                items,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private static NfeEmission emissionWithTotals() {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializa\u00e7\u00e3o",
                "3550308",
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                List.of(item()),
                new Totals(new BigDecimal("10.50"), new BigDecimal("1.25"), new BigDecimal("0.50"),
                        new BigDecimal("0.75"), new BigDecimal("0.10"), new BigDecimal("11.60")),
                new TaxTotals(
                        new IcmsTotals(new BigDecimal("10.50"), new BigDecimal("1.89"), new BigDecimal("0.00"),
                                new BigDecimal("0.21"), new BigDecimal("0.10"), new BigDecimal("1.50"),
                                new BigDecimal("0.20"), new BigDecimal("12.00"), new BigDecimal("2.16"),
                                new BigDecimal("0.24"), new BigDecimal("0.05")),
                        new BigDecimal("0.30"), new BigDecimal("0.50"), new BigDecimal("0.10"),
                        new BigDecimal("0.40"), new BigDecimal("0.60"), new BigDecimal("4.35"),
                        null, null),
                null,
                null,
                null,
                null);
    }

    private static NfeEmission emissionWithPartialTotals() {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializa\u00e7\u00e3o",
                "3550308",
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                List.of(item()),
                new Totals(new BigDecimal("10.50"), null, null, null, null, new BigDecimal("10.50")),
                new TaxTotals(
                        new IcmsTotals(new BigDecimal("10.50"), new BigDecimal("1.89"), new BigDecimal("0.00"),
                                new BigDecimal("0.21"), null, null, null,
                                null, null, null, null),
                        null, null, null, null, null, null, null, null),
                null,
                null,
                null,
                null);
    }

    private static NfeEmission emissionWithTransport() {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializa\u00e7\u00e3o",
                "3550308",
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                List.of(item()),
                new Totals(new BigDecimal("10.50"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("10.50")),
                null,
                null,
                null,
                new Transport("0", "12345678000199", "Acme Carrier", address()),
                null);
    }

    private static NfeEmission emissionWithFreightModeOnly() {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializa\u00e7\u00e3o",
                "3550308",
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                List.of(item()),
                null,
                null,
                null,
                null,
                new Transport("9", null, null, null),
                null);
    }

    private static NfeEmission emissionWithPayment() {
        return new NfeEmission(
                "emission-1",
                "REF-1",
                OperationType.TRANSFER,
                EmissionStatus.RECEIVED,
                "55",
                3,
                1000L,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"),
                "Remessa para industrializa\u00e7\u00e3o",
                "3550308",
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                issuer(),
                recipient(),
                List.of(item()),
                new Totals(new BigDecimal("10.50"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                        new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("11.60")),
                null,
                null,
                null,
                new Transport("9", null, null, null),
                new Payment(List.of(
                        new PaymentDetail("17", new BigDecimal("9.99"),
                                PaymentIndicator.IMMEDIATE, LocalDate.of(2026, 9, 21)),
                        new PaymentDetail("01", new BigDecimal("1.61"),
                                PaymentIndicator.DEFERRED, null))));
    }

    private static NfeItem fullItem1() {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                null, null);
    }

    private static NfeItem fullItem2() {
        return new NfeItem(
                "P-2", "Gadget", "84713019",
                "7890000000002", "7890000000003", "0202020",
                "CX",
                new BigDecimal("2"), new BigDecimal("20.00"), new BigDecimal("40.00"),
                "UN", new BigDecimal("2"), new BigDecimal("20.00"),
                null, null, null, null,
                "5101", "0",
                false,
                "PO-2026-000124", "00020",
                null, null);
    }

    private static NfeItem minimalItem() {
        return new NfeItem(
                "P-3", "Minimal", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("5.00"), new BigDecimal("5.00"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                null, null);
    }

    private static NfeItem itemWithCharges() {
        return new NfeItem(
                "P-4", "Gadget", "84713019",
                "7890000000002", "7890000000003", "0202020",
                "UN",
                new BigDecimal("2"), new BigDecimal("5.00"), new BigDecimal("10.00"),
                "UN", new BigDecimal("2"), new BigDecimal("5.00"),
                new BigDecimal("1.25"), new BigDecimal("0.50"),
                new BigDecimal("0.75"), new BigDecimal("0.10"),
                "5102", "0",
                true,
                "PO-2026-000999", "00099",
                null, null);
    }

    private static IcmsTax coreIcms() {
        return new IcmsTax(
                "00", "3",
                new BigDecimal("100.00"), new BigDecimal("18.00"), new BigDecimal("18.00"),
                new BigDecimal("2.00"), new BigDecimal("2.00"),
                null, null, null);
    }

    private static IcmsStTax icmsSt() {
        return new IcmsStTax(
                "4",
                new BigDecimal("40.00"), new BigDecimal("10.00"),
                new BigDecimal("120.00"), new BigDecimal("18.00"), new BigDecimal("21.60"),
                new BigDecimal("120.00"), new BigDecimal("2.00"), new BigDecimal("2.40"));
    }

    private static NfeItem taxedItem() {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                new ItemTaxation(coreIcms(), null, null, null, null, null),
                null);
    }

    private static NfeItem taxedItemWithSt() {
        return new NfeItem(
                "P-4", "TaxedWidget", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(
                        new IcmsTax(
                                "30", "3",
                                new BigDecimal("100.00"), new BigDecimal("18.00"), new BigDecimal("18.00"),
                                new BigDecimal("2.00"), new BigDecimal("2.00"),
                                icmsSt(),
                                new BigDecimal("0.00"), "40"),
                        null, null, null, null, null),
                null);
    }

    private static NfeItem icms40Item() {
        return itemWithIcms(new IcmsTax("40", null, null, null, null,
                null, null, null,
                new BigDecimal("5.00"), "40"));
    }

    private static NfeItem itemWithIcms(IcmsTax icms) {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                new ItemTaxation(icms, null, null, null, null, null),
                null);
    }

    private static IpiTax ipiFixture() {
        return new IpiTax(
                "50",
                new BigDecimal("100.00"),
                new BigDecimal("5.00"),
                new BigDecimal("5.00"));
    }

    private static NfeItem taxedItemWithIpi() {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                new ItemTaxation(coreIcms(), ipiFixture(), null, null, null, null),
                null);
    }

    private static NfeItem ipiOnlyItem() {
        return new NfeItem(
                "P-5", "IpiOnly", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, ipiFixture(), null, null, null, null),
                null);
    }

    private static PisCofinsTax pisCofinsFixture() {
        return new PisCofinsTax(
                "01", new BigDecimal("100.00"), new BigDecimal("1.65"), new BigDecimal("1.65"),
                "01", new BigDecimal("100.00"), new BigDecimal("7.60"), new BigDecimal("7.60"));
    }

    private static NfeItem taxedItemWithPisCofins() {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                new ItemTaxation(coreIcms(), ipiFixture(), pisCofinsFixture(), null, null, null),
                null);
    }

    private static NfeItem pisCofinsOnlyItem() {
        return new NfeItem(
                "P-6", "PisCofinsOnly", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, null, pisCofinsFixture(), null, null, null),
                null);
    }

    private static NfeItem cofinsOnlyItem() {
        return new NfeItem(
                "P-7", "CofinsOnly", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(
                        null, null,
                        new PisCofinsTax(
                                null, null, null, null,
                                "01", new BigDecimal("100.00"), new BigDecimal("7.60"), new BigDecimal("7.60")),
                        null, null, null),
                null);
    }

    private static NfeItem pisOnlyItem() {
        return new NfeItem(
                "P-8", "PisOnly", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(
                        null, null,
                        new PisCofinsTax(
                                "01", new BigDecimal("100.00"), new BigDecimal("1.65"), new BigDecimal("1.65"),
                                null, null, null, null),
                        null, null, null),
                null);
    }

    private static ImportTax importTaxFixture() {
        return new ImportTax(
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("1.50"));
    }

    private static NfeItem taxedItemWithIi() {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                new ItemTaxation(coreIcms(), ipiFixture(), pisCofinsFixture(), importTaxFixture(), null, null),
                null);
    }

    private static NfeItem iiOnlyItem() {
        return new NfeItem(
                "P-9", "IiOnly", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, null, null, importTaxFixture(), null, null),
                null);
    }

    private static NfeItem emptyImportTaxItem() {
        return new NfeItem(
                "P-10", "EmptyIi", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, null, null, new ImportTax(null, null, null, null, null), null, null),
                null);
    }

    private static IsTax isFixture() {
        return new IsTax(
                "900", "001000",
                new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("1.50"),
                "UN", new BigDecimal("2"), new BigDecimal("20.00"));
    }

    private static NfeItem taxedItemWithIs() {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                new ItemTaxation(coreIcms(), ipiFixture(), pisCofinsFixture(), importTaxFixture(), isFixture(), null),
                null);
    }

    private static NfeItem isOnlyItem() {
        return new NfeItem(
                "P-11", "IsOnly", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, null, null, null, isFixture(), null),
                null);
    }

    private static NfeItem icmsIsItem() {
        return new NfeItem(
                "P-12", "IcmsIs", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(coreIcms(), null, null, null, isFixture(), null),
                null);
    }

    private static NfeItem emptyIsItem() {
        return new NfeItem(
                "P-13", "EmptyIs", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(coreIcms(), null, null, null,
                        new IsTax(null, null, null, null, null, null, null, null), null),
                null);
    }

    private static IbsCbsTaxation ibsCbsFixture() {
        return new IbsCbsTaxation(
                new BigDecimal("100.00"),
                null, null, null, null, null);
    }

    private static RtcTaxation rtcCore() {
        return new RtcTaxation("300", "001000", null, ibsCbsFixture());
    }

    private static RtcTaxation rtcFixture() {
        return new RtcTaxation("300", "001000", "1", ibsCbsFixture());
    }

    private static RtcTaxation rtcNoIbsCbs() {
        return new RtcTaxation("300", "001000", null, null);
    }

    private static RtcTaxation rtcEmpty() {
        return new RtcTaxation(null, null, null,
                new IbsCbsTaxation(null, null, null, null, null, null));
    }

    private static NfeItem taxedItemWithRtc() {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                new ItemTaxation(coreIcms(), ipiFixture(), pisCofinsFixture(), importTaxFixture(), isFixture(), rtcFixture()),
                null);
    }

    private static NfeItem rtcOnlyItem() {
        return new NfeItem(
                "P-14", "RtcOnly", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, null, null, null, null, rtcFixture()),
                null);
    }

    private static NfeItem icmsRtcItem() {
        return new NfeItem(
                "P-15", "IcmsRtc", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(coreIcms(), null, null, null, null, rtcCore()),
                null);
    }

    private static NfeItem rtcNoIbsCbsItem() {
        return new NfeItem(
                "P-16", "RtcNoIbsCbs", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, null, null, null, null, rtcNoIbsCbs()),
                null);
    }

    private static NfeItem rtcEmptyItem() {
        return new NfeItem(
                "P-17", "RtcEmpty", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, null, null, null, null, rtcEmpty()),
                null);
    }

    private static IbsUfTax ufRateOnly() {
        return new IbsUfTax(new BigDecimal("10.00"), null, null, null, null);
    }

    private static IbsUfTax ufAmountOnly() {
        return new IbsUfTax(null, null, null, null, new BigDecimal("8.50"));
    }

    private static IbsUfTax ufRateAmount() {
        return new IbsUfTax(new BigDecimal("10.00"), null, null, null, new BigDecimal("8.50"));
    }

    private static IbsUfTax ufDif() {
        return new IbsUfTax(null, new RtcDeferral(new BigDecimal("2.00"), new BigDecimal("1.60")),
                null, null, null);
    }

    private static IbsUfTax ufDevTrib() {
        return new IbsUfTax(null, null,
                new RtcDevolution(new BigDecimal("1.00"), new BigDecimal("0.80")),
                null, null);
    }

    private static IbsUfTax ufRed() {
        return new IbsUfTax(null, null, null,
                new RtcReduction(new BigDecimal("30.00"), new BigDecimal("7.00")),
                null);
    }

    private static IbsUfTax ufAll() {
        return new IbsUfTax(new BigDecimal("10.00"),
                new RtcDeferral(new BigDecimal("2.00"), new BigDecimal("1.60")),
                new RtcDevolution(new BigDecimal("1.00"), new BigDecimal("0.80")),
                new RtcReduction(new BigDecimal("30.00"), new BigDecimal("7.00")),
                new BigDecimal("8.50"));
    }

    private static IbsUfTax ufNoDif() {
        return new IbsUfTax(new BigDecimal("10.00"), null,
                new RtcDevolution(new BigDecimal("1.00"), new BigDecimal("0.80")),
                new RtcReduction(new BigDecimal("30.00"), new BigDecimal("7.00")),
                new BigDecimal("8.50"));
    }

    private static IbsUfTax ufNoDevTrib() {
        return new IbsUfTax(new BigDecimal("10.00"),
                new RtcDeferral(new BigDecimal("2.00"), new BigDecimal("1.60")),
                null,
                new RtcReduction(new BigDecimal("30.00"), new BigDecimal("7.00")),
                new BigDecimal("8.50"));
    }

    private static IbsUfTax ufNoRed() {
        return new IbsUfTax(new BigDecimal("10.00"),
                new RtcDeferral(new BigDecimal("2.00"), new BigDecimal("1.60")),
                new RtcDevolution(new BigDecimal("1.00"), new BigDecimal("0.80")),
                null,
                new BigDecimal("8.50"));
    }

    private static IbsUfTax ufEmpty() {
        return new IbsUfTax(null, null, null, null, null);
    }

    private static IbsUfTax ufEmptySubgroups() {
        return new IbsUfTax(new BigDecimal("10.00"),
                new RtcDeferral(null, null),
                new RtcDevolution(null, null),
                new RtcReduction(null, null),
                new BigDecimal("8.50"));
    }

    private static RtcTaxation rtcWithUf(IbsUfTax ibsUf) {
        return new RtcTaxation("300", "001000", "1",
                new IbsCbsTaxation(new BigDecimal("100.00"), ibsUf, null, null, null, null));
    }

    private static NfeItem itemWithIbsUf(IbsUfTax ibsUf) {
        return new NfeItem(
                "P-18", "IbsUf", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(coreIcms(), null, null, null, null, rtcWithUf(ibsUf)),
                null);
    }

    private static NfeItem ufOnlyRtcItem(IbsUfTax ibsUf) {
        return new NfeItem(
                "P-19", "UfOnlyRtc", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, null, null, null, null, rtcWithUf(ibsUf)),
                null);
    }

    private static NfeItem taxedItemWithUf(IbsUfTax ibsUf) {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                new ItemTaxation(coreIcms(), ipiFixture(), pisCofinsFixture(), importTaxFixture(), isFixture(), rtcWithUf(ibsUf)),
                null);
    }

    private static IbsMunicipalityTax munRateOnly() {
        return new IbsMunicipalityTax(new BigDecimal("5.00"), null, null, null, null);
    }

    private static IbsMunicipalityTax munAmountOnly() {
        return new IbsMunicipalityTax(null, null, null, null, new BigDecimal("3.40"));
    }

    private static IbsMunicipalityTax munRateAmount() {
        return new IbsMunicipalityTax(new BigDecimal("5.00"), null, null, null, new BigDecimal("3.40"));
    }

    private static IbsMunicipalityTax munDif() {
        return new IbsMunicipalityTax(null,
                new RtcDeferral(new BigDecimal("1.00"), new BigDecimal("0.50")),
                null, null, null);
    }

    private static IbsMunicipalityTax munDevTrib() {
        return new IbsMunicipalityTax(null, null,
                new RtcDevolution(new BigDecimal("0.50"), new BigDecimal("0.20")),
                null, null);
    }

    private static IbsMunicipalityTax munRed() {
        return new IbsMunicipalityTax(null, null, null,
                new RtcReduction(new BigDecimal("20.00"), new BigDecimal("4.00")),
                null);
    }

    private static IbsMunicipalityTax munAll() {
        return new IbsMunicipalityTax(new BigDecimal("5.00"),
                new RtcDeferral(new BigDecimal("1.00"), new BigDecimal("0.50")),
                new RtcDevolution(new BigDecimal("0.50"), new BigDecimal("0.20")),
                new RtcReduction(new BigDecimal("20.00"), new BigDecimal("4.00")),
                new BigDecimal("3.40"));
    }

    private static IbsMunicipalityTax munNoDif() {
        return new IbsMunicipalityTax(new BigDecimal("5.00"), null,
                new RtcDevolution(new BigDecimal("0.50"), new BigDecimal("0.20")),
                new RtcReduction(new BigDecimal("20.00"), new BigDecimal("4.00")),
                new BigDecimal("3.40"));
    }

    private static IbsMunicipalityTax munNoDevTrib() {
        return new IbsMunicipalityTax(new BigDecimal("5.00"),
                new RtcDeferral(new BigDecimal("1.00"), new BigDecimal("0.50")),
                null,
                new RtcReduction(new BigDecimal("20.00"), new BigDecimal("4.00")),
                new BigDecimal("3.40"));
    }

    private static IbsMunicipalityTax munNoRed() {
        return new IbsMunicipalityTax(new BigDecimal("5.00"),
                new RtcDeferral(new BigDecimal("1.00"), new BigDecimal("0.50")),
                new RtcDevolution(new BigDecimal("0.50"), new BigDecimal("0.20")),
                null,
                new BigDecimal("3.40"));
    }

    private static IbsMunicipalityTax munEmpty() {
        return new IbsMunicipalityTax(null, null, null, null, null);
    }

    private static IbsMunicipalityTax munEmptySubgroups() {
        return new IbsMunicipalityTax(new BigDecimal("5.00"),
                new RtcDeferral(null, null),
                new RtcDevolution(null, null),
                new RtcReduction(null, null),
                new BigDecimal("3.40"));
    }

    private static RtcTaxation rtcWithMun(IbsMunicipalityTax ibsMun) {
        return new RtcTaxation("300", "001000", "1",
                new IbsCbsTaxation(new BigDecimal("100.00"), null, ibsMun, null, null, null));
    }

    private static NfeItem itemWithIbsMun(IbsMunicipalityTax ibsMun) {
        return new NfeItem(
                "P-20", "IbsMun", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(coreIcms(), null, null, null, null, rtcWithMun(ibsMun)),
                null);
    }

    private static NfeItem munOnlyRtcItem(IbsMunicipalityTax ibsMun) {
        return new NfeItem(
                "P-21", "MunOnlyRtc", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(null, null, null, null, null, rtcWithMun(ibsMun)),
                null);
    }

    private static NfeItem taxedItemWithMun(IbsMunicipalityTax ibsMun) {
        return new NfeItem(
                "P-1", "Widget", "84818090",
                "7891234567895", "7899876543210", "0101010",
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                "UN", new BigDecimal("1"), new BigDecimal("10.50"),
                null, null, null, null,
                "5102", "0",
                true,
                "PO-2026-000123", "00010",
                new ItemTaxation(coreIcms(), ipiFixture(), pisCofinsFixture(), importTaxFixture(), isFixture(), rtcWithMun(ibsMun)),
                null);
    }

    private static NfeItem itemWithUfAndMun() {
        RtcTaxation rtc = new RtcTaxation("300", "001000", "1",
                new IbsCbsTaxation(new BigDecimal("100.00"), ufRateAmount(), munRateAmount(), null, null, null));
        return new NfeItem(
                "P-22", "UfMun", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(coreIcms(), null, null, null, null, rtc),
                null);
    }

    private static NfeItem itemWithVIbs(IbsUfTax ibsUf, IbsMunicipalityTax ibsMun, BigDecimal ibsAmount) {
        RtcTaxation rtc = new RtcTaxation("300", "001000", "1",
                new IbsCbsTaxation(new BigDecimal("100.00"), ibsUf, ibsMun, ibsAmount, null, null));
        return new NfeItem(
                "P-23", "VIbs", "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102", "0",
                null,
                null, null,
                new ItemTaxation(coreIcms(), null, null, null, null, rtc),
                null);
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static String xpath(Document document, String expression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String value = (String) xpath.evaluate(expression, document, XPathConstants.STRING);
        return value == null || value.isBlank() ? null : value;
    }

    private static double countXpath(Document document, String expression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Double) xpath.evaluate(expression, document, XPathConstants.NUMBER);
    }

    private static NfeItem item() {
        return new NfeItem(
                "P-1",
                "Widget",
                "84818090",
                null, null, null,
                "UN",
                new BigDecimal("1"),
                new BigDecimal("10.50"),
                new BigDecimal("10.50"),
                null, null, null,
                null, null, null, null,
                "5102",
                "0",
                null, null, null,
                null,
                null);
    }
}
