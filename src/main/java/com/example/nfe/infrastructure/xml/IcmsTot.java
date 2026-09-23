package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the NF-e {@code ICMSTot} group, in the exact
 * PL_010f sequence for the fields the domain provides: vBC, vICMS,
 * vICMSDeson, vFCPUFDest, vICMSUFDest, vICMSUFRemet, vFCP, vBCST, vST,
 * vFCPST, vFCPSTRet, vProd, vFrete, vSeg, vDesc, vII, vIPI, vIPIDevol,
 * vPIS, vCOFINS, vOutro, vNF, vTotTrib.
 * <p>
 * Monophase fields (qBCMono, vICMSMono, qBCMonoReten, vICMSMonoReten,
 * qBCMonoRet, vICMSMonoRet) are omitted because the domain does not provide
 * them. Values are mapped explicitly from the domain — nothing is
 * calculated or derived.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IcmsTot {

    @XmlElement(name = "vBC")
    private BigDecimal base;

    @XmlElement(name = "vICMS")
    private BigDecimal amount;

    @XmlElement(name = "vICMSDeson")
    private BigDecimal desonerationAmount;

    @XmlElement(name = "vFCPUFDest")
    private BigDecimal fcpUfDestination;

    @XmlElement(name = "vICMSUFDest")
    private BigDecimal ufDestination;

    @XmlElement(name = "vICMSUFRemet")
    private BigDecimal ufSender;

    @XmlElement(name = "vFCP")
    private BigDecimal fcp;

    @XmlElement(name = "vBCST")
    private BigDecimal stBase;

    @XmlElement(name = "vST")
    private BigDecimal stAmount;

    @XmlElement(name = "vFCPST")
    private BigDecimal fcpStAmount;

    @XmlElement(name = "vFCPSTRet")
    private BigDecimal fcpStRetained;

    @XmlElement(name = "vProd")
    private BigDecimal itemsTotal;

    @XmlElement(name = "vFrete")
    private BigDecimal freight;

    @XmlElement(name = "vSeg")
    private BigDecimal insurance;

    @XmlElement(name = "vDesc")
    private BigDecimal discount;

    @XmlElement(name = "vII")
    private BigDecimal importTaxAmount;

    @XmlElement(name = "vIPI")
    private BigDecimal ipiAmount;

    @XmlElement(name = "vIPIDevol")
    private BigDecimal ipiDevolvedAmount;

    @XmlElement(name = "vPIS")
    private BigDecimal pisAmount;

    @XmlElement(name = "vCOFINS")
    private BigDecimal cofinsAmount;

    @XmlElement(name = "vOutro")
    private BigDecimal otherCharges;

    @XmlElement(name = "vNF")
    private BigDecimal totalValue;

    @XmlElement(name = "vTotTrib")
    private BigDecimal totalTaxValue;

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getDesonerationAmount() {
        return desonerationAmount;
    }

    public void setDesonerationAmount(BigDecimal desonerationAmount) {
        this.desonerationAmount = desonerationAmount;
    }

    public BigDecimal getFcpUfDestination() {
        return fcpUfDestination;
    }

    public void setFcpUfDestination(BigDecimal fcpUfDestination) {
        this.fcpUfDestination = fcpUfDestination;
    }

    public BigDecimal getUfDestination() {
        return ufDestination;
    }

    public void setUfDestination(BigDecimal ufDestination) {
        this.ufDestination = ufDestination;
    }

    public BigDecimal getUfSender() {
        return ufSender;
    }

    public void setUfSender(BigDecimal ufSender) {
        this.ufSender = ufSender;
    }

    public BigDecimal getFcp() {
        return fcp;
    }

    public void setFcp(BigDecimal fcp) {
        this.fcp = fcp;
    }

    public BigDecimal getStBase() {
        return stBase;
    }

    public void setStBase(BigDecimal stBase) {
        this.stBase = stBase;
    }

    public BigDecimal getStAmount() {
        return stAmount;
    }

    public void setStAmount(BigDecimal stAmount) {
        this.stAmount = stAmount;
    }

    public BigDecimal getFcpStAmount() {
        return fcpStAmount;
    }

    public void setFcpStAmount(BigDecimal fcpStAmount) {
        this.fcpStAmount = fcpStAmount;
    }

    public BigDecimal getFcpStRetained() {
        return fcpStRetained;
    }

    public void setFcpStRetained(BigDecimal fcpStRetained) {
        this.fcpStRetained = fcpStRetained;
    }

    public BigDecimal getItemsTotal() {
        return itemsTotal;
    }

    public void setItemsTotal(BigDecimal itemsTotal) {
        this.itemsTotal = itemsTotal;
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

    public BigDecimal getImportTaxAmount() {
        return importTaxAmount;
    }

    public void setImportTaxAmount(BigDecimal importTaxAmount) {
        this.importTaxAmount = importTaxAmount;
    }

    public BigDecimal getIpiAmount() {
        return ipiAmount;
    }

    public void setIpiAmount(BigDecimal ipiAmount) {
        this.ipiAmount = ipiAmount;
    }

    public BigDecimal getIpiDevolvedAmount() {
        return ipiDevolvedAmount;
    }

    public void setIpiDevolvedAmount(BigDecimal ipiDevolvedAmount) {
        this.ipiDevolvedAmount = ipiDevolvedAmount;
    }

    public BigDecimal getPisAmount() {
        return pisAmount;
    }

    public void setPisAmount(BigDecimal pisAmount) {
        this.pisAmount = pisAmount;
    }

    public BigDecimal getCofinsAmount() {
        return cofinsAmount;
    }

    public void setCofinsAmount(BigDecimal cofinsAmount) {
        this.cofinsAmount = cofinsAmount;
    }

    public BigDecimal getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(BigDecimal otherCharges) {
        this.otherCharges = otherCharges;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getTotalTaxValue() {
        return totalTaxValue;
    }

    public void setTotalTaxValue(BigDecimal totalTaxValue) {
        this.totalTaxValue = totalTaxValue;
    }
}
