package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the NF-e {@code IS} group (Imposto Seletivo),
 * mapped from the domain {@link com.example.nfe.domain.IsTax}.
 * <p>
 * Verified against the official PL_010f schema ({@code TIS} type in
 * DFeTiposBasicos_v1.00.xsd): {@code CSTIS}, {@code cClassTribIS}, then an
 * optional sequence with {@code vBCIS}, {@code pIS}, {@code adRemIS},
 * {@code uTrib}/{@code qTrib} and {@code vIS} — in that order.
 * <p>
 * All eight {@code IsTax} fields have an exact correspondence and are mapped.
 * No {@code pISEspec} element exists in the schema.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Is {

    @XmlElement(name = "CSTIS")
    private String cst;

    @XmlElement(name = "cClassTribIS")
    private String cClassTrib;

    @XmlElement(name = "vBCIS")
    private BigDecimal taxBase;

    @XmlElement(name = "pIS")
    private BigDecimal rate;

    @XmlElement(name = "adRemIS")
    private BigDecimal adRemRate;

    @XmlElement(name = "uTrib")
    private String taxableUnit;

    @XmlElement(name = "qTrib")
    private BigDecimal taxableQuantity;

    @XmlElement(name = "vIS")
    private BigDecimal amount;

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

    public BigDecimal getTaxBase() {
        return taxBase;
    }

    public void setTaxBase(BigDecimal taxBase) {
        this.taxBase = taxBase;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getAdRemRate() {
        return adRemRate;
    }

    public void setAdRemRate(BigDecimal adRemRate) {
        this.adRemRate = adRemRate;
    }

    public String getTaxableUnit() {
        return taxableUnit;
    }

    public void setTaxableUnit(String taxableUnit) {
        this.taxableUnit = taxableUnit;
    }

    public BigDecimal getTaxableQuantity() {
        return taxableQuantity;
    }

    public void setTaxableQuantity(BigDecimal taxableQuantity) {
        this.taxableQuantity = taxableQuantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
