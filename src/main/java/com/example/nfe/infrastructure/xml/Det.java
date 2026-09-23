package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code det} group (product + taxes).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Det {

    @XmlAttribute(name = "nItem")
    private Integer nItem;

    @XmlElement(name = "prod")
    private Prod prod;

    @XmlElement(name = "imposto")
    private Imposto imposto;

    public Integer getNItem() {
        return nItem;
    }

    public void setNItem(Integer nItem) {
        this.nItem = nItem;
    }

    public Prod getProd() {
        return prod;
    }

    public void setProd(Prod prod) {
        this.prod = prod;
    }

    public Imposto getImposto() {
        return imposto;
    }

    public void setImposto(Imposto imposto) {
        this.imposto = imposto;
    }
}
