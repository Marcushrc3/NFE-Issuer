package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the NF-e {@code detPag} group, in the exact
 * PL_010f sequence for the fields the domain provides: indPag, tPag, vPag,
 * dPag. xPag, CNPJPag, UFPag and card are not provided by the domain and
 * remain absent. {@code vPag} is mapped from the explicit domain amount —
 * never calculated.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DetPag {

    @XmlElement(name = "indPag")
    private String indicator;

    @XmlElement(name = "tPag")
    private String paymentMethod;

    @XmlElement(name = "vPag")
    private BigDecimal amount;

    @XmlElement(name = "dPag")
    private String paymentDate;

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
}
