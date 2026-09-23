package com.example.nfe.infrastructure.xml.signature;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the XMLDSig {@code ds:Signature} structure
 * (xmldsig-core-schema_v1.01.xsd, bundled). Structural only: this class
 * holds the signature elements but does not create cryptographic values —
 * that is the responsibility of an {@code XmlSigner} implementation.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Signature {

    @XmlElement(name = "SignedInfo")
    private SignedInfo signedInfo;

    @XmlElement(name = "SignatureValue")
    private SignatureValue signatureValue;

    @XmlElement(name = "KeyInfo")
    private KeyInfo keyInfo;

    public SignedInfo getSignedInfo() {
        return signedInfo;
    }

    public void setSignedInfo(SignedInfo signedInfo) {
        this.signedInfo = signedInfo;
    }

    public SignatureValue getSignatureValue() {
        return signatureValue;
    }

    public void setSignatureValue(SignatureValue signatureValue) {
        this.signatureValue = signatureValue;
    }

    public KeyInfo getKeyInfo() {
        return keyInfo;
    }

    public void setKeyInfo(KeyInfo keyInfo) {
        this.keyInfo = keyInfo;
    }
}
