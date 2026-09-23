package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the NF-e {@code prod} group. Field order follows
 * the exact official PL_010f sequence for the fields the domain provides:
 * cProd, cEAN, xProd, NCM, CEST, CFOP, uCom, qCom, vUnCom, vProd, cEANTrib,
 * uTrib, qTrib, vUnTrib, vFrete, vSeg, vDesc, vOutro, indTot, xPed,
 * nItemPed. Monetary and quantity values stay BigDecimal so no binary
 * floating-point artifacts appear in the XML.
 * <p>
 * Fields without a domain counterpart are omitted rather than defaulted:
 * cBarra, NVE, indEscala, CNPJFab, cBenef, gCred, EXTIPI, cBarraTrib,
 * indBemMovelUsado, DI, detExport, nFCI, rastro, infProdNFF, infProdEmb,
 * veicProd, med, arma and comb.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Prod {

    @XmlElement(name = "cProd")
    private String productCode;

    @XmlElement(name = "cEAN")
    private String ean;

    @XmlElement(name = "xProd")
    private String description;

    @XmlElement(name = "NCM")
    private String ncm;

    @XmlElement(name = "CEST")
    private String cest;

    @XmlElement(name = "CFOP")
    private String cfop;

    @XmlElement(name = "uCom")
    private String unit;

    @XmlElement(name = "qCom")
    private BigDecimal quantity;

    @XmlElement(name = "vUnCom")
    private BigDecimal unitValue;

    @XmlElement(name = "vProd")
    private BigDecimal totalValue;

    @XmlElement(name = "cEANTrib")
    private String eanTrib;

    @XmlElement(name = "uTrib")
    private String tributaryUnit;

    @XmlElement(name = "qTrib")
    private BigDecimal tributaryQuantity;

    @XmlElement(name = "vUnTrib")
    private BigDecimal tributaryUnitValue;

    @XmlElement(name = "vFrete")
    private BigDecimal freight;

    @XmlElement(name = "vSeg")
    private BigDecimal insurance;

    @XmlElement(name = "vDesc")
    private BigDecimal discount;

    @XmlElement(name = "vOutro")
    private BigDecimal otherCharges;

    @XmlElement(name = "indTot")
    private String indTot;

    @XmlElement(name = "xPed")
    private String xPed;

    @XmlElement(name = "nItemPed")
    private String nItemPed;

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNcm() {
        return ncm;
    }

    public void setNcm(String ncm) {
        this.ncm = ncm;
    }

    public String getCest() {
        return cest;
    }

    public void setCest(String cest) {
        this.cest = cest;
    }

    public String getCfop() {
        return cfop;
    }

    public void setCfop(String cfop) {
        this.cfop = cfop;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(BigDecimal unitValue) {
        this.unitValue = unitValue;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public String getEanTrib() {
        return eanTrib;
    }

    public void setEanTrib(String eanTrib) {
        this.eanTrib = eanTrib;
    }

    public String getTributaryUnit() {
        return tributaryUnit;
    }

    public void setTributaryUnit(String tributaryUnit) {
        this.tributaryUnit = tributaryUnit;
    }

    public BigDecimal getTributaryQuantity() {
        return tributaryQuantity;
    }

    public void setTributaryQuantity(BigDecimal tributaryQuantity) {
        this.tributaryQuantity = tributaryQuantity;
    }

    public BigDecimal getTributaryUnitValue() {
        return tributaryUnitValue;
    }

    public void setTributaryUnitValue(BigDecimal tributaryUnitValue) {
        this.tributaryUnitValue = tributaryUnitValue;
    }

    public BigDecimal getFreight() {
        return freight;
    }

    public void setFreight(BigDecimal freight) {
        this.freight = freight;
    }

    public BigDecimal getInsurance() {
        return insurance;
    }

    public void setInsurance(BigDecimal insurance) {
        this.insurance = insurance;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(BigDecimal otherCharges) {
        this.otherCharges = otherCharges;
    }

    public String getIndTot() {
        return indTot;
    }

    public void setIndTot(String indTot) {
        this.indTot = indTot;
    }

    public String getXPed() {
        return xPed;
    }

    public void setXPed(String xPed) {
        this.xPed = xPed;
    }

    public String getNItemPed() {
        return nItemPed;
    }

    public void setNItemPed(String nItemPed) {
        this.nItemPed = nItemPed;
    }
}
