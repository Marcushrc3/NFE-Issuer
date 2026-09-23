package com.example.nfe.infrastructure.xml.signature;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * XMLDSig {@code ds:X509Data} element, holding the base64-encoded
 * X509 certificate. The certificate itself is always supplied by the
 * signing implementation — never embedded by the application.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class X509Data {

    @XmlElement(name = "X509Certificate")
    private byte[] x509Certificate;

    public byte[] getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(byte[] x509Certificate) {
        this.x509Certificate = x509Certificate;
    }
}
