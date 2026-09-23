package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the official {@code ICMS00} variant (CST 00 —
 * tributada integralmente), in the exact PL_010f sequence: orig, CST,
 * modBC, vBC, pICMS, vICMS, pFCP, vFCP. All elements are required by the
 * schema.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Icms00 {

    @XmlElement(name = "orig")
    private String origin;

    @XmlElement(name = "CST")
    private String cst;

    @XmlElement(name = "modBC")
    private String modBc;

    @XmlElement(name = "vBC")
    private BigDecimal taxBase;

    @XmlElement(name = "pICMS")
    private BigDecimal taxRate;

    @XmlElement(name = "vICMS")
    private BigDecimal taxAmount;

    @XmlElement(name = "pFCP")
    private BigDecimal fcpRate;

    @XmlElement(name = "vFCP")
    private BigDecimal fcpAmount;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCst() {
        return cst;
    }

    public void setCst(String cst) {
        this.cst = cst;
    }

    public String getModBc() {
        return modBc;
    }

    public void setModBc(String modBc) {
        this.modBc = modBc;
    }

    public BigDecimal getTaxBase() {
        return taxBase;
    }

    public void setTaxBase(BigDecimal taxBase) {
        this.taxBase = taxBase;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getFcpRate() {
        return fcpRate;
    }

    public void setFcpRate(BigDecimal fcpRate) {
        this.fcpRate = fcpRate;
    }

    public BigDecimal getFcpAmount() {
        return fcpAmount;
    }

    public void setFcpAmount(BigDecimal fcpAmount) {
        this.fcpAmount = fcpAmount;
    }
}
