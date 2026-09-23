package com.example.nfe.infrastructure.xml.signature;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/** XMLDSig {@code ds:KeyInfo} element, holding X509Data. */
@XmlAccessorType(XmlAccessType.FIELD)
public class KeyInfo {

    @XmlElement(name = "X509Data")
    private X509Data x509Data;

    public X509Data getX509Data() {
        return x509Data;
    }

    public void setX509Data(X509Data x509Data) {
        this.x509Data = x509Data;
    }
}
