package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the RTC {@code gDevTrib} group (devolução de
 * tributos), mapped from the domain
 * {@link com.example.nfe.domain.RtcDevolution}.
 * <p>
 * Verified against the official PL_010f schema ({@code TDevTrib} type):
 * {@code pDevTrib} (optional) then {@code vDevTrib}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GdevTrib {

    @XmlElement(name = "pDevTrib")
    private BigDecimal rate;

    @XmlElement(name = "vDevTrib")
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
