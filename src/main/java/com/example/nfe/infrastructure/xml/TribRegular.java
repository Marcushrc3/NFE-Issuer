package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the RTC {@code gTribRegular} group (taxation as it
 * would be if the resolutive/suspensive condition were not met), mapped from
 * the domain {@link com.example.nfe.domain.RegularTaxation}.
 * <p>
 * Verified against the official PL_010f schema ({@code TTribRegular} type):
 * {@code CSTReg}, {@code cClassTribReg}, {@code pAliqEfetRegIBSUF},
 * {@code vTribRegIBSUF}, {@code pAliqEfetRegIBSMun}, {@code vTribRegIBSMun},
 * {@code pAliqEfetRegCBS}, {@code vTribRegCBS} — all required, in that
 * order.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TribRegular {

    @XmlElement(name = "CSTReg")
    private String cst;

    @XmlElement(name = "cClassTribReg")
    private String cClassTrib;

    @XmlElement(name = "pAliqEfetRegIBSUF")
    private BigDecimal ibsUfEffectiveRate;

    @XmlElement(name = "vTribRegIBSUF")
    private BigDecimal ibsUfTaxAmount;

    @XmlElement(name = "pAliqEfetRegIBSMun")
    private BigDecimal ibsMunicipalityEffectiveRate;

    @XmlElement(name = "vTribRegIBSMun")
    private BigDecimal ibsMunicipalityTaxAmount;

    @XmlElement(name = "pAliqEfetRegCBS")
    private BigDecimal cbsEffectiveRate;

    @XmlElement(name = "vTribRegCBS")
    private BigDecimal cbsTaxAmount;

    public String getCst() {
        return cst;
    }

    public void setCst(String cst) {
        this.cst = cst;
    }

    public String getCClassTrib() {
        return cClassTrib;
    }

    public void setCClassTrib(String cClassTrib) {
        this.cClassTrib = cClassTrib;
    }

    public BigDecimal getIbsUfEffectiveRate() {
        return ibsUfEffectiveRate;
    }

    public void setIbsUfEffectiveRate(BigDecimal ibsUfEffectiveRate) {
        this.ibsUfEffectiveRate = ibsUfEffectiveRate;
    }

    public BigDecimal getIbsUfTaxAmount() {
        return ibsUfTaxAmount;
    }

    public void setIbsUfTaxAmount(BigDecimal ibsUfTaxAmount) {
        this.ibsUfTaxAmount = ibsUfTaxAmount;
    }

    public BigDecimal getIbsMunicipalityEffectiveRate() {
        return ibsMunicipalityEffectiveRate;
    }

    public void setIbsMunicipalityEffectiveRate(BigDecimal ibsMunicipalityEffectiveRate) {
        this.ibsMunicipalityEffectiveRate = ibsMunicipalityEffectiveRate;
    }

    public BigDecimal getIbsMunicipalityTaxAmount() {
        return ibsMunicipalityTaxAmount;
    }

    public void setIbsMunicipalityTaxAmount(BigDecimal ibsMunicipalityTaxAmount) {
        this.ibsMunicipalityTaxAmount = ibsMunicipalityTaxAmount;
    }

    public BigDecimal getCbsEffectiveRate() {
        return cbsEffectiveRate;
    }

    public void setCbsEffectiveRate(BigDecimal cbsEffectiveRate) {
        this.cbsEffectiveRate = cbsEffectiveRate;
    }

    public BigDecimal getCbsTaxAmount() {
        return cbsTaxAmount;
    }

    public void setCbsTaxAmount(BigDecimal cbsTaxAmount) {
        this.cbsTaxAmount = cbsTaxAmount;
    }
}
