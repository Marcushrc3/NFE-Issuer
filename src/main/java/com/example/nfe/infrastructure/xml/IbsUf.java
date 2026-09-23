package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the RTC {@code gIBSUF} group (IBS of the state /
 * UF), mapped from the domain {@link com.example.nfe.domain.IbsUfTax}.
 * <p>
 * Verified against the official PL_010f schema ({@code TCIBS_NFe} type):
 * {@code pIBSUF}, {@code gDif} (optional), {@code gDevTrib} (optional),
 * {@code gRed} (optional), {@code vIBSUF} — in that order.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IbsUf {

    @XmlElement(name = "pIBSUF")
    private BigDecimal rate;

    @XmlElement(name = "gDif")
    private Gdif gdif;

    @XmlElement(name = "gDevTrib")
    private GdevTrib gdevTrib;

    @XmlElement(name = "gRed")
    private Gred gred;

    @XmlElement(name = "vIBSUF")
    private BigDecimal amount;

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
