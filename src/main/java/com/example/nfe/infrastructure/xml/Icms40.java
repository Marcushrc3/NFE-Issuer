package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the official {@code ICMS40} variant (CST 40/41/50
 * — isenta, não tributada ou suspensão), in the exact PL_010f sequence:
 * orig, CST, vICMSDeson, motDesICMS. All elements are required by the
 * schema.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Icms40 {

    @XmlElement(name = "orig")
    private String origin;

    @XmlElement(name = "CST")
    private String cst;

    @XmlElement(name = "vICMSDeson")
    private BigDecimal desonerationAmount;

    @XmlElement(name = "motDesICMS")
    private String desonerationReason;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCst() {
        return cst;
    }

    public void setCst(String cst) {
        this.cst = cst;
    }

    public BigDecimal getDesonerationAmount() {
        return desonerationAmount;
    }

    public void setDesonerationAmount(BigDecimal desonerationAmount) {
        this.desonerationAmount = desonerationAmount;
    }

    public String getDesonerationReason() {
        return desonerationReason;
    }

    public void setDesonerationReason(String desonerationReason) {
        this.desonerationReason = desonerationReason;
    }
}
