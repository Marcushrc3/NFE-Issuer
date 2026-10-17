package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the NF-e {@code total}/{@code ISTot} group
 * (Imposto Seletivo totals), mapped from the domain
 * {@link com.example.nfe.domain.IsTotals}.
 * <p>
 * Verified against the official PL_010f schema ({@code TISTot} type):
 * {@code vIS} — the total Imposto Seletivo value.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Istot {

    @XmlElement(name = "vIS")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
