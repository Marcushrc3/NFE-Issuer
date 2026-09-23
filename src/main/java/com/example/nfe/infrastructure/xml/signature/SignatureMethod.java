package com.example.nfe.infrastructure.xml.signature;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/** XMLDSig signature-method element (Algorithm attribute only). */
@XmlAccessorType(XmlAccessType.FIELD)
public class SignatureMethod {

    @XmlAttribute(name = "Algorithm")
    private String algorithm;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
