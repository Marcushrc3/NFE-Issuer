package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the RTC {@code gDif} group (diferimento), mapped
 * from the domain {@link com.example.nfe.domain.RtcDeferral}.
 * <p>
 * Verified against the official PL_010f schema ({@code TDif} type):
 * {@code pDif} then {@code vDif}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Gdif {

    @XmlElement(name = "pDif")
    private BigDecimal rate;

    @XmlElement(name = "vDif")
    private BigDecimal amount;

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
