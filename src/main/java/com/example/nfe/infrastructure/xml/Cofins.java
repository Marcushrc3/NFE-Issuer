package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * Generic COFINS XML representation for the current domain model
 * ({@code PisCofinsTax}).
 * <p>
 * <strong>Important:</strong> the official NF-e schema models COFINS as a
 * choice of variant groups: {@code COFINSAliq} (CST/vBC/pCOFINS/vCOFINS),
 * {@code COFINSQtde} (CST/qBCProd/vAliqProd/vCOFINS), {@code COFINSNT}
 * (CST only), {@code COFINSOutr} (CST/vBC/pCOFINS/vCOFINS + optional
 * qBCProd/vAliqProd) and {@code COFINSST}. The domain model is generic and
 * carries only the tributary (Aliq/Outr-style) fields, so this class is an
 * intermediate, conservative representation of that subset. It is NOT an
 * XSD-conformant variant group; variant selection is a future increment.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Cofins {

    @XmlElement(name = "CST")
    private String cst;

    @XmlElement(name = "vBC")
    private BigDecimal taxBase;

    @XmlElement(name = "pCOFINS")
    private BigDecimal taxRate;

    @XmlElement(name = "vCOFINS")
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
