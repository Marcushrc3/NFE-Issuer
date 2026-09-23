package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the RTC {@code gRed} group (redução de alíquota),
 * mapped from the domain {@link com.example.nfe.domain.RtcReduction}.
 * <p>
 * Verified against the official PL_010f schema ({@code TRed} type):
 * {@code pRedAliq} then {@code pAliqEfet}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Gred {

    @XmlElement(name = "pRedAliq")
    private BigDecimal reductionRate;

    @XmlElement(name = "pAliqEfet")
    private BigDecimal effectiveRate;

    public BigDecimal getReductionRate() {
        return reductionRate;
    }

    public void setReductionRate(BigDecimal reductionRate) {
        this.reductionRate = reductionRate;
    }

    public BigDecimal getEffectiveRate() {
        return effectiveRate;
    }

    public void setEffectiveRate(BigDecimal effectiveRate) {
        this.effectiveRate = effectiveRate;
    }
}
