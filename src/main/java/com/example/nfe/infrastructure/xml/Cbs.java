package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the RTC {@code gCBS} group (CBS taxation), mapped
 * from the domain {@link com.example.nfe.domain.CbsTax}.
 * <p>
 * Verified against the official PL_010f schema ({@code TCIBS_NFe} type):
 * {@code pCBS}, {@code gDif} (optional), {@code gDevTrib} (optional),
 * {@code gRed} (optional), {@code vCBS} — in that order.
 * {@code gALCZFMCBS} (ALC/ZFM) is not modelled by the domain and remains
 * absent.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Cbs {

    @XmlElement(name = "pCBS")
    private java.math.BigDecimal rate;

    @XmlElement(name = "gDif")
    private Gdif gdif;

    @XmlElement(name = "gDevTrib")
    private GdevTrib gdevTrib;

    @XmlElement(name = "gRed")
    private Gred gred;

    @XmlElement(name = "vCBS")
    private java.math.BigDecimal amount;

    public java.math.BigDecimal getRate() {
        return rate;
    }

    public void setRate(java.math.BigDecimal rate) {
        this.rate = rate;
    }

    public Gdif getGdif() {
        return gdif;
    }

    public void setGdif(Gdif gdif) {
        this.gdif = gdif;
    }

    public GdevTrib getGdevTrib() {
        return gdevTrib;
    }

    public void setGdevTrib(GdevTrib gdevTrib) {
        this.gdevTrib = gdevTrib;
    }

    public Gred getGred() {
        return gred;
    }

    public void setGred(Gred gred) {
        this.gred = gred;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }
}
