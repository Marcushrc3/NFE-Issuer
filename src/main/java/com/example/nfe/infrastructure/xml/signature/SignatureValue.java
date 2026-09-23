package com.example.nfe.infrastructure.xml.signature;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlValue;

/**
 * JAXB representation of the XMLDSig {@code ds:SignatureValue} element
 * (base64-encoded signature bytes).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SignatureValue {

    @XmlValue
    private byte[] value;

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
