package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code transp} group, in the exact PL_010f
 * sequence: modFrete (required), transporta (optional carrier group).
 * <p>
 * {@code modFrete} is mapped from the domain's plain String
 * {@code freightMode} as-is — no conversion table is invented. The official
 * enumeration (0..4, 9) is validated by the XSD layer, not by the mapper.
 * retTransp, veicTransp, reboque, vagao, balsa and vol are not provided by
 * the current domain and remain absent.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Transp {

    @XmlElement(name = "modFrete")
    private String freightMode;

    @XmlElement(name = "transporta")
    private Transporta carrier;

    public String getFreightMode() {
        return freightMode;
    }

    public void setFreightMode(String freightMode) {
        this.freightMode = freightMode;
    }

    public Transporta getCarrier() {
        return carrier;
    }

    public void setCarrier(Transporta carrier) {
        this.carrier = carrier;
    }
}
