package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * Generic IPI XML representation for the current domain model
 * ({@code IpiTax}).
 * <p>
 * <strong>Important:</strong> the official NF-e schema models IPI as
 * {@code cEnq} (optional) followed by a choice between {@code IPITrib}
 * (CST + vBC/pIPI/vIPI + optional qUnid/vUnid) and {@code IPINT} (CST only).
 * The domain model is generic and carries only the tributary-style fields, so
 * this class is an intermediate, conservative representation of that subset.
 * It is NOT an XSD-conformant variant group.
 * <p>
 * Not mapped here: {@code cEnq} (not in the domain model), qUnid/vUnid
 * (not in the domain model), and the non-tributary {@code IPINT} group
 * (the domain cannot express which variant applies). Variant selection is a
 * future increment.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Ipi {

    @XmlElement(name = "CST")
    private String cst;

    @XmlElement(name = "vBC")
    private BigDecimal taxBase;

    @XmlElement(name = "pIPI")
    private BigDecimal taxRate;

    @XmlElement(name = "vIPI")
    private BigDecimal taxAmount;

    public String getCst() {
        return cst;
    }

    public void setCst(String cst) {
        this.cst = cst;
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
}
