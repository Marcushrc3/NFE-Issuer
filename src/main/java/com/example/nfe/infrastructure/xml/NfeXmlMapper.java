package com.example.nfe.infrastructure.xml;

import com.example.nfe.application.accesskey.NfeAccessKeyGenerator;
import com.example.nfe.domain.Address;
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
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps the domain {@link NfeEmission} aggregate into the JAXB XML model.
 * Mapping logic lives here — never inside domain classes.
 */
@Component
public class NfeXmlMapper {

    /**
     * Official NF-e schema version (TVerNFe, pattern {@code 4\.00}).
     * XML-layer constant — deliberately not part of the domain.
     */
    private static final String NF_VERSION = "4.00";

    private static final DateTimeFormatter EMISSION_DATE_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public Nfe toXml(NfeEmission emission) {
        Ide ide = new Ide();
        ide.setStateCode(emission.stateCode());
        ide.setRandomCode(emission.randomCode());
        ide.setOperationDescription(emission.operationDescription());
        ide.setModel(emission.model());
        ide.setSeries(emission.series() == null ? null : String.valueOf(emission.series()));
        ide.setNumber(emission.number() == null ? null : String.valueOf(emission.number()));
        ide.setEmissionDate(emission.emissionDate() == null
                ? null
                : EMISSION_DATE_FORMAT.format(emission.emissionDate()));
        ide.setOperationDirection(emission.operationDirection());
        ide.setDestinationType(emission.destinationType());
        ide.setFiscalEstablishmentCity(emission.fiscalEstablishmentCity());
        ide.setPrintFormat(emission.printFormat());
        ide.setEmissionType(emission.emissionType());
        ide.setEnvironment(emission.environment());
        ide.setPurpose(emission.purpose());
        ide.setFinalConsumer(emission.finalConsumer());
        ide.setPresenceIndicator(emission.presenceIndicator());
        ide.setProcessType(emission.processType());
        ide.setProcessVersion(emission.processVersion());

        InfNfe infNfe = new InfNfe();
        infNfe.setVersao(NF_VERSION);
        String accessKey = NfeAccessKeyGenerator.generate(emission);
        ide.setCheckDigit(accessKey == null ? null : accessKey.substring(accessKey.length() - 1));
        infNfe.setId(accessKey == null ? null : "NFe" + accessKey);
        infNfe.setIde(ide);
        infNfe.setEmit(toEmit(emission.issuer()));
        infNfe.setDest(toDest(emission.recipient()));
        infNfe.setDets(toDets(emission.items()));
        infNfe.setTotal(toTotal(emission));
        infNfe.setTransp(toTransp(emission.transport()));
        infNfe.setPag(toPag(emission.payment()));

        Nfe nfe = new Nfe();
        nfe.setInfNfe(infNfe);
        return nfe;
    }

    private List<Det> toDets(List<NfeItem> items) {
        List<Det> dets = new ArrayList<>();
        if (items == null) {
            return dets;
        }
        int position = 1;
        for (NfeItem item : items) {
            Det det = new Det();
            det.setNItem(position++);
            det.setProd(toProd(item));
            det.setImposto(toImposto(item.taxation(), item.origin()));
            dets.add(det);
        }
        return dets;
    }

    /**
     * Builds the {@code total}/{@code ICMSTot} group from explicit domain
     * values only — nothing is calculated or derived (vNF is never
     * recomputed, tax amounts are never summed). A {@code total} group is
     * only generated when the emission carries totals or tax totals.
     */
    private Total toTotal(NfeEmission emission) {
        Totals totals = emission.totals();
        TaxTotals taxTotals = emission.taxTotals();
        if (totals == null && taxTotals == null) {
            return null;
        }
        IcmsTot icmsTot = new IcmsTot();
        IcmsTotals icms = taxTotals == null ? null : taxTotals.icms();
        if (icms != null) {
            icmsTot.setBase(icms.base());
            icmsTot.setAmount(icms.amount());
            icmsTot.setDesonerationAmount(icms.desonerationAmount());
            icmsTot.setFcpUfDestination(icms.fcpUfDestination());
            icmsTot.setUfDestination(icms.ufDestination());
            icmsTot.setUfSender(icms.ufSender());
            icmsTot.setFcp(icms.fcp());
            icmsTot.setStBase(icms.stBase());
            icmsTot.setStAmount(icms.stAmount());
            icmsTot.setFcpStAmount(icms.fcpStAmount());
            icmsTot.setFcpStRetained(icms.fcpStRetained());
        }
        if (totals != null) {
            icmsTot.setItemsTotal(totals.itemsTotal());
            icmsTot.setFreight(totals.freight());
            icmsTot.setInsurance(totals.insurance());
            icmsTot.setDiscount(totals.discount());
            icmsTot.setOtherCharges(totals.otherCharges());
            icmsTot.setTotalValue(totals.totalValue());
        }
        if (taxTotals != null) {
            icmsTot.setImportTaxAmount(taxTotals.importTaxAmount());
            icmsTot.setIpiAmount(taxTotals.ipiAmount());
            icmsTot.setIpiDevolvedAmount(taxTotals.ipiDevolvedAmount());
            icmsTot.setPisAmount(taxTotals.pisAmount());
            icmsTot.setCofinsAmount(taxTotals.cofinsAmount());
            icmsTot.setTotalTaxValue(taxTotals.totalTaxValue());
        }
        Total total = new Total();
        total.setIcmsTot(icmsTot);
        return total;
    }

    /**
     * Builds the {@code transp} group from the semantic domain
     * {@link Transport}. No element is emitted for null data: a null
     * transport produces no {@code transp} at all; a carrier group is only
     * emitted when it carries document, name or address data. The carrier
     * document is mapped to {@code CNPJ} provisionally (the domain does not
     * distinguish document types) and {@code freightMode} is passed through
     * as-is — the official modFrete enumeration (0..4, 9) is enforced by
     * the XSD layer, not by the mapper.
     */
    private Transp toTransp(Transport transport) {
        if (transport == null) {
            return null;
        }
        Transp transp = new Transp();
        transp.setFreightMode(transport.freightMode());
        transp.setCarrier(toTransporta(transport));
        return transp;
    }

    private Transporta toTransporta(Transport transport) {
        Address address = transport.deliveryAddress();
        if (transport.carrierDocument() == null && transport.carrierName() == null
                && address == null) {
            return null;
        }
        Transporta xml = new Transporta();
        xml.setDocument(transport.carrierDocument());
        xml.setName(transport.carrierName());
        if (address != null) {
            xml.setStreet(address.street());
            xml.setCity(address.city());
            xml.setState(address.state());
        }
        return xml;
    }

    /**
     * Builds the {@code pag} group from the semantic domain {@link Payment}.
     * A null payment produces no {@code pag} element; each {@code detPag}
     * carries the explicit domain values only — {@code vPag} is never
     * calculated or derived from totals.
     */
    private Pag toPag(Payment payment) {
        if (payment == null) {
            return null;
        }
        Pag pag = new Pag();
        pag.setDetPags(payment.details().stream()
                .map(this::toDetPag)
                .toList());
        return pag;
    }

    private DetPag toDetPag(PaymentDetail detail) {
        DetPag xml = new DetPag();
        xml.setIndicator(toIndPag(detail.indicator()));
        xml.setPaymentMethod(detail.paymentMethod());
        xml.setAmount(detail.amount());
        xml.setPaymentDate(detail.paymentDate() == null
                ? null
                : DateTimeFormatter.ISO_LOCAL_DATE.format(detail.paymentDate()));
        return xml;
    }

    /**
     * Maps the domain payment indicator to the official {@code indPag}
     * numeric code. The codes exist ONLY here — the domain carries business
     * meaning without XML concepts.
     */
    private String toIndPag(PaymentIndicator indicator) {
        if (indicator == null) {
            return null;
        }
        return switch (indicator) {
            case IMMEDIATE -> "0";
            case DEFERRED -> "1";
        };
    }

    private Prod toProd(NfeItem item) {
        Prod prod = new Prod();
        prod.setProductCode(item.productCode());
        prod.setDescription(item.description());
        prod.setNcm(item.ncm());
        prod.setEan(item.cEan());
        prod.setEanTrib(item.cEanTrib());
        prod.setUnit(item.unit());
        prod.setQuantity(item.quantity());
        prod.setUnitValue(item.unitValue());
        prod.setTotalValue(item.totalValue());
        prod.setTributaryUnit(item.tributaryUnit());
        prod.setTributaryQuantity(item.tributaryQuantity());
        prod.setTributaryUnitValue(item.tributaryUnitValue());
        prod.setFreight(item.freight());
        prod.setInsurance(item.insurance());
        prod.setDiscount(item.discount());
        prod.setOtherCharges(item.otherCharges());
        prod.setCfop(item.cfop());
        prod.setCest(item.cest());
        prod.setIndTot(item.indTot() == null ? null : (item.indTot() ? "1" : "0"));
        prod.setXPed(item.xPed());
        prod.setNItemPed(item.nItemPed());
        return prod;
    }

    /**
     * Builds the {@code imposto} group only when there is something to map.
     * This increment maps ICMS, IPI, II, PIS, COFINS, IS and the IBSCBS
     * core; a taxation without any of them (or no taxation at all) produces
     * no {@code imposto} element — never an empty {@code <imposto/>}.
     * Individual tax groups are only added when their domain counterpart
     * exists.
     */
    private Imposto toImposto(ItemTaxation taxation, String origin) {
        if (taxation == null) {
            return null;
        }
        Icms icms = taxation.icms() == null ? null : toIcms(taxation.icms(), origin);
        Ipi ipi = taxation.ipi() == null ? null : toIpi(taxation.ipi());
        Ii ii = taxation.importTax() == null ? null : toIi(taxation.importTax());
        PisCofinsTax pisCofins = taxation.pisCofins();
        Pis pis = pisCofins == null ? null : toPis(pisCofins);
        Cofins cofins = pisCofins == null ? null : toCofins(pisCofins);
        Is is = taxation.is() == null ? null : toIs(taxation.is());
        IbsCbs ibsCbs = toIbsCbs(taxation.rtc());
        if (icms == null && ipi == null && ii == null
                && pis == null && cofins == null && is == null && ibsCbs == null) {
            return null;
        }
        Imposto imposto = new Imposto();
        imposto.setIcms(icms);
        imposto.setIpi(ipi);
        imposto.setIi(ii);
        imposto.setPis(pis);
        imposto.setCofins(cofins);
        imposto.setIs(is);
        imposto.setIbsCbs(ibsCbs);
        return imposto;
    }

    /**
     * Core IBSCBS mapping (verified {@code TTribNFe}/{@code TCIBS_NFe}):
     * CST, cClassTrib, indDoacao and gIBSCBS (vBC + gIBSUF). Deferred to
     * future increments: gIBSMun/vIBS/gCBS, gTribRegular, gALCZFMCBS,
     * monophase, credit and reversal groups. An all-null RTC produces no
     * element.
     */
    private IbsCbs toIbsCbs(RtcTaxation rtc) {
        if (rtc == null || rtc.ibsCbs() == null) {
            return null;
        }
        GibsCbs gibsCbs = toGibsCbs(rtc.ibsCbs());
        if (rtc.cst() == null && rtc.cClassTrib() == null
                && rtc.indDoacao() == null && gibsCbs == null) {
            return null;
        }
        IbsCbs xml = new IbsCbs();
        xml.setCst(rtc.cst());
        xml.setCClassTrib(rtc.cClassTrib());
        xml.setIndDoacao(rtc.indDoacao());
        xml.setGibsCbs(gibsCbs);
        return xml;
    }

    /**
     * gIBSCBS mapping (verified {@code TCIBS_NFe}): {@code vBC},
     * {@code gIBSUF}, {@code gIBSMun} and {@code vIBS} in this increment.
     * No empty group is generated. IMPORTANT: {@code vIBS} is mapped from
     * the domain's explicit {@code ibsAmount} — it is NEVER calculated from
     * {@code vIBSUF} + {@code vIBSMun}. Fiscal calculations do not belong to
     * the XML infrastructure layer.
     */
    private GibsCbs toGibsCbs(IbsCbsTaxation ibsCbs) {
        if (ibsCbs == null) {
            return null;
        }
        IbsUf ibsUf = toIbsUf(ibsCbs.ibsUf());
        IbsMun ibsMun = toIbsMun(ibsCbs.ibsMunicipality());
        if (ibsCbs.taxBase() == null && ibsUf == null
                && ibsMun == null && ibsCbs.ibsAmount() == null) {
            return null;
        }
        GibsCbs xml = new GibsCbs();
        xml.setTaxBase(ibsCbs.taxBase());
        xml.setIbsUf(ibsUf);
        xml.setIbsMun(ibsMun);
        xml.setIbsAmount(ibsCbs.ibsAmount());
        return xml;
    }

    /**
     * gIBSMun mapping (verified {@code TCIBS_NFe}): pIBSMun, gDif, gDevTrib,
     * gRed, vIBSMun. Same conditional-group behavior as gIBSUF: groups are
     * only generated when their domain counterpart exists and carries data.
     */
    private IbsMun toIbsMun(IbsMunicipalityTax ibsMunicipality) {
        if (ibsMunicipality == null) {
            return null;
        }
        Gdif gdif = toGdif(ibsMunicipality.deferral());
        GdevTrib gdevTrib = toGdevTrib(ibsMunicipality.devolution());
        Gred gred = toGred(ibsMunicipality.reduction());
        if (ibsMunicipality.rate() == null && ibsMunicipality.amount() == null
                && gdif == null && gdevTrib == null && gred == null) {
            return null;
        }
        IbsMun xml = new IbsMun();
        xml.setRate(ibsMunicipality.rate());
        xml.setGdif(gdif);
        xml.setGdevTrib(gdevTrib);
        xml.setGred(gred);
        xml.setAmount(ibsMunicipality.amount());
        return xml;
    }

    /**
     * gIBSUF mapping (verified {@code TCIBS_NFe}): pIBSUF, gDif, gDevTrib,
     * gRed, vIBSUF. Conditional groups are only generated when their domain
     * counterpart exists and carries data. No calculations; values are
     * preserved exactly.
     */
    private IbsUf toIbsUf(IbsUfTax ibsUf) {
        if (ibsUf == null) {
            return null;
        }
        Gdif gdif = toGdif(ibsUf.deferral());
        GdevTrib gdevTrib = toGdevTrib(ibsUf.devolution());
        Gred gred = toGred(ibsUf.reduction());
        if (ibsUf.rate() == null && ibsUf.amount() == null
                && gdif == null && gdevTrib == null && gred == null) {
            return null;
        }
        IbsUf xml = new IbsUf();
        xml.setRate(ibsUf.rate());
        xml.setGdif(gdif);
        xml.setGdevTrib(gdevTrib);
        xml.setGred(gred);
        xml.setAmount(ibsUf.amount());
        return xml;
    }

    private Gdif toGdif(RtcDeferral deferral) {
        if (deferral == null || (deferral.rate() == null && deferral.amount() == null)) {
            return null;
        }
        Gdif xml = new Gdif();
        xml.setRate(deferral.rate());
        xml.setAmount(deferral.amount());
        return xml;
    }

    private GdevTrib toGdevTrib(RtcDevolution devolution) {
        if (devolution == null || (devolution.rate() == null && devolution.amount() == null)) {
            return null;
        }
        GdevTrib xml = new GdevTrib();
        xml.setRate(devolution.rate());
        xml.setAmount(devolution.amount());
        return xml;
    }

    private Gred toGred(RtcReduction reduction) {
        if (reduction == null
                || (reduction.reductionRate() == null && reduction.effectiveRate() == null)) {
            return null;
        }
        Gred xml = new Gred();
        xml.setReductionRate(reduction.reductionRate());
        xml.setEffectiveRate(reduction.effectiveRate());
        return xml;
    }

    /**
     * IS mapping verified against the official {@code TIS} type:
     * CSTIS, cClassTribIS, vBCIS, pIS, adRemIS, uTrib/qTrib, vIS. All eight
     * {@code IsTax} fields map exactly. An all-null {@code IsTax} produces
     * no element.
     */
    private Is toIs(IsTax isTax) {
        if (isTax.cst() == null && isTax.cClassTrib() == null
                && isTax.taxBase() == null && isTax.rate() == null
                && isTax.adRemRate() == null && isTax.taxableUnit() == null
                && isTax.taxableQuantity() == null && isTax.amount() == null) {
            return null;
        }
        Is xml = new Is();
        xml.setCst(isTax.cst());
        xml.setCClassTrib(isTax.cClassTrib());
        xml.setTaxBase(isTax.taxBase());
        xml.setRate(isTax.rate());
        xml.setAdRemRate(isTax.adRemRate());
        xml.setTaxableUnit(isTax.taxableUnit());
        xml.setTaxableQuantity(isTax.taxableQuantity());
        xml.setAmount(isTax.amount());
        return xml;
    }

    /**
     * Conservative II mapping. The official schema's {@code II} group has no
     * tax-rate element, so {@code ImportTax.taxRate()} is deliberately
     * unmapped. {@code ImportItemDetails} is a separate concept and is not
     * mapped here. An all-null {@code ImportTax} produces no element.
     */
    private Ii toIi(ImportTax importTax) {
        if (importTax.taxBase() == null && importTax.taxAmount() == null
                && importTax.customsExpenses() == null && importTax.iofAmount() == null) {
            return null;
        }
        Ii xml = new Ii();
        xml.setTaxBase(importTax.taxBase());
        xml.setCustomsExpenses(importTax.customsExpenses());
        xml.setTaxAmount(importTax.taxAmount());
        xml.setIofAmount(importTax.iofAmount());
        return xml;
    }

    /**
     * Conservative tributary-style PIS mapping. The variant choice
     * ({@code PISAliq}/{@code PISQtde}/{@code PISNT}/{@code PISOutr}/
     * {@code PISST}) cannot be expressed by the generic domain model and is
     * deliberately left unsupported. An empty PIS half produces no element.
     */
    private Pis toPis(PisCofinsTax tax) {
        if (tax.pisCst() == null && tax.pisBase() == null
                && tax.pisRate() == null && tax.pisAmount() == null) {
            return null;
        }
        Pis xml = new Pis();
        xml.setCst(tax.pisCst());
        xml.setTaxBase(tax.pisBase());
        xml.setTaxRate(tax.pisRate());
        xml.setTaxAmount(tax.pisAmount());
        return xml;
    }

    /**
     * Conservative tributary-style COFINS mapping. The variant choice
     * ({@code COFINSAliq}/{@code COFINSQtde}/{@code COFINSNT}/
     * {@code COFINSOutr}/{@code COFINSST}) cannot be expressed by the generic
     * domain model and is deliberately left unsupported. An empty COFINS half
     * produces no element.
     */
    private Cofins toCofins(PisCofinsTax tax) {
        if (tax.cofinsCst() == null && tax.cofinsBase() == null
                && tax.cofinsRate() == null && tax.cofinsAmount() == null) {
            return null;
        }
        Cofins xml = new Cofins();
        xml.setCst(tax.cofinsCst());
        xml.setTaxBase(tax.cofinsBase());
        xml.setTaxRate(tax.cofinsRate());
        xml.setTaxAmount(tax.cofinsAmount());
        return xml;
    }

    /**
     * Conservative tributary-style IPI mapping. {@code cEnq} and the
     * {@code IPITrib}/{@code IPINT} variant choice are not representable by
     * the generic domain model and are deliberately left unmapped.
     */
    private Ipi toIpi(IpiTax ipi) {
        Ipi xml = new Ipi();
        xml.setCst(ipi.cst());
        xml.setTaxBase(ipi.taxBase());
        xml.setTaxRate(ipi.taxRate());
        xml.setTaxAmount(ipi.taxAmount());
        return xml;
    }

    /**
     * Selects the official ICMS variant group from the domain CST and maps
     * the applicable domain fields into it. The numeric XML codes and the
     * variant choice live here — never in the domain.
     * <p>
     * Supported variants (verified against PL_010f):
     * <ul>
     *   <li>CST 00 → ICMS00 (tributada integralmente)</li>
     *   <li>CST 30 → ICMS30 (isenta/não tributada com ST)</li>
     *   <li>CST 40/41/50 → ICMS40 (isenta/não tributada/suspensão)</li>
     * </ul>
     * Any other CST fails explicitly — no silent, schema-invalid XML.
     */
    private Icms toIcms(IcmsTax icms, String origin) {
        Icms xml = new Icms();
        switch (icms.cst()) {
            case "00" -> xml.setIcms00(toIcms00(icms, origin));
            case "30" -> xml.setIcms30(toIcms30(icms, origin));
            case "40", "41", "50" -> xml.setIcms40(toIcms40(icms, origin));
            default -> throw new IllegalStateException(
                    "ICMS CST not supported for XML variant mapping: " + icms.cst());
        }
        return xml;
    }

    /**
     * ICMS00 (CST 00): orig, CST, modBC, vBC, pICMS, vICMS, pFCP, vFCP.
     * ST and desoneração data do not belong to this variant and are
     * rejected explicitly instead of being silently dropped.
     */
    private Icms00 toIcms00(IcmsTax icms, String origin) {
        if (icms.st() != null) {
            throw new IllegalStateException("ICMS00 does not support ICMS ST data");
        }
        if (icms.desonerationAmount() != null || icms.desonerationReason() != null) {
            throw new IllegalStateException("ICMS00 does not support desoneração data");
        }
        Icms00 xml = new Icms00();
        xml.setOrigin(origin);
        xml.setCst(icms.cst());
        xml.setModBc(icms.modBc());
        xml.setTaxBase(icms.taxBase());
        xml.setTaxRate(icms.taxRate());
        xml.setTaxAmount(icms.taxAmount());
        xml.setFcpRate(icms.fcpRate());
        xml.setFcpAmount(icms.fcpAmount());
        return xml;
    }

    /**
     * ICMS30 (CST 30): ST block plus desoneração (vICMSDeson/motDesICMS).
     * The ST and desoneração data are required by the schema, so the mapper
     * fails explicitly when the domain does not supply them.
     */
    private Icms30 toIcms30(IcmsTax icms, String origin) {
        IcmsStTax st = icms.st();
        if (st == null) {
            throw new IllegalStateException("ICMS30 requires ICMS ST data (IcmsTax.st)");
        }
        if (icms.desonerationAmount() == null || icms.desonerationReason() == null) {
            throw new IllegalStateException(
                    "ICMS30 requires desoneração data (vICMSDeson/motDesICMS)");
        }
        Icms30 xml = new Icms30();
        xml.setOrigin(origin);
        xml.setCst(icms.cst());
        xml.setModBcSt(st.modBcSt());
        xml.setStMarginRate(st.stMarginRate());
        xml.setStReductionRate(st.stReductionRate());
        xml.setStBase(st.stBase());
        xml.setStRate(st.stRate());
        xml.setStAmount(st.stAmount());
        xml.setFcpStBase(st.fcpStBase());
        xml.setFcpStRate(st.fcpStRate());
        xml.setFcpStAmount(st.fcpStAmount());
        xml.setDesonerationAmount(icms.desonerationAmount());
        xml.setDesonerationReason(icms.desonerationReason());
        return xml;
    }

    /**
     * ICMS40 (CST 40/41/50): desoneração only. The desoneração data are
     * required by the schema, so the mapper fails explicitly when the
     * domain does not supply them.
     */
    private Icms40 toIcms40(IcmsTax icms, String origin) {
        if (icms.desonerationAmount() == null || icms.desonerationReason() == null) {
            throw new IllegalStateException(
                    "ICMS40 requires desoneração data (vICMSDeson/motDesICMS)");
        }
        Icms40 xml = new Icms40();
        xml.setOrigin(origin);
        xml.setCst(icms.cst());
        xml.setDesonerationAmount(icms.desonerationAmount());
        xml.setDesonerationReason(icms.desonerationReason());
        return xml;
    }

    private Emit toEmit(Issuer issuer) {
        if (issuer == null) {
            return null;
        }
        Emit emit = new Emit();
        emit.setName(issuer.name());
        emit.setDocument(issuer.document());
        emit.setTradeName(issuer.tradeName());
        emit.setAddress(toEnderEmit(issuer.address()));
        emit.setStateRegistration(issuer.stateRegistration());
        emit.setTaxRegime(issuer.taxRegime());
        return emit;
    }

    private Dest toDest(Recipient recipient) {
        if (recipient == null) {
            return null;
        }
        Dest dest = new Dest();
        dest.setName(recipient.name());
        dest.setDocument(recipient.document());
        dest.setAddress(toEnderDest(recipient.address()));
        dest.setIeStatusIndicator(toIndIeDest(recipient.ieStatus()));
        return dest;
    }

    /**
     * Maps the domain recipient IE status to the official {@code indIEDest}
     * numeric code. The codes exist ONLY here — the domain carries business
     * meaning without XML concepts.
     */
    private String toIndIeDest(RecipientIeStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case CONTRIBUTOR -> "1";
            case EXEMPT -> "2";
            case NOT_CONTRIBUTOR -> "9";
        };
    }

    private EnderEmit toEnderEmit(Address address) {
        if (address == null) {
            return null;
        }
        EnderEmit ender = new EnderEmit();
        ender.setStreet(address.street());
        ender.setNumber(address.number());
        ender.setNeighborhood(address.neighborhood());
        ender.setMunicipalityCode(address.municipalityCode());
        ender.setCity(address.city());
        ender.setState(address.state());
        ender.setZipCode(address.zipCode());
        return ender;
    }

    private EnderDest toEnderDest(Address address) {
        if (address == null) {
            return null;
        }
        EnderDest ender = new EnderDest();
        ender.setStreet(address.street());
        ender.setNumber(address.number());
        ender.setNeighborhood(address.neighborhood());
        ender.setMunicipalityCode(address.municipalityCode());
        ender.setCity(address.city());
        ender.setState(address.state());
        ender.setZipCode(address.zipCode());
        return ender;
    }
}
