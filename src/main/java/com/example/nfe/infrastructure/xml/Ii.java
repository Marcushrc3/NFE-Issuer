package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the NF-e {@code II} group (Imposto de Importação),
 * mapped from the domain {@link com.example.nfe.domain.ImportTax}.
 * <p>
 * Verified against the official PL_010f v4.00 schema: the {@code II} group
 * contains exactly {@code vBC}, {@code vDespAdu}, {@code vII} and
 * {@code vIOF}. There is NO tax-rate element in this group.
 * <p>
 * Therefore {@code ImportTax.taxRate()} is deliberately NOT mapped — the
 * official schema has no {@code pII}. {@code ImportItemDetails} (customs
 * declaration/addition linkage) is a separate concept and is NOT part of this
 * group.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Ii {

    @XmlElement(name = "vBC")
    private BigDecimal taxBase;

    @XmlElement(name = "vDespAdu")
    private BigDecimal customsExpenses;

    @XmlElement(name = "vII")
    private BigDecimal taxAmount;

    @XmlElement(name = "vIOF")
    private BigDecimal iofAmount;

    public BigDecimal getTaxBase() {
        return taxBase;
    }

    public void setTaxBase(BigDecimal taxBase) {
        this.taxBase = taxBase;
    }

    public BigDecimal getCustomsExpenses() {
        return customsExpenses;
    }

    public void setCustomsExpenses(BigDecimal customsExpenses) {
        this.customsExpenses = customsExpenses;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getIofAmount() {
        return iofAmount;
    }

    public void setIofAmount(BigDecimal iofAmount) {
        this.iofAmount = iofAmount;
    }
}
