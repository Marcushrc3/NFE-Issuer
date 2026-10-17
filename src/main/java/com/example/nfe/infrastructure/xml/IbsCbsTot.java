package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the NF-e {@code total}/{@code IBSCBSTot} group
 * (IBS/CBS totals), mapped from the domain
 * {@link com.example.nfe.domain.IbsCbsTotals}.
 * <p>
 * Verified against the official PL_010f schema ({@code TIBSCBSMonoTot}
 * type): only {@code vBCIBSCBS} is representable by the current domain.
 * The {@code gIBS}/{@code gCBS} total groups require presumed-credit
 * totals ({@code vCredPres}/{@code vCredPresCondSus}) which the domain
 * does not model and are therefore never emitted here.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IbsCbsTot {

    @XmlElement(name = "vBCIBSCBS")
    private BigDecimal base;

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }
}
