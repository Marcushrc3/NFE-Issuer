package com.example.nfe.infrastructure.xml.signature;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the XMLDSig {@code ds:SignedInfo} element:
 * CanonicalizationMethod, SignatureMethod and Reference.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SignedInfo {

    @XmlElement(name = "CanonicalizationMethod")
    private CanonicalizationMethod canonicalizationMethod;

    @XmlElement(name = "SignatureMethod")
    private SignatureMethod signatureMethod;

    @XmlElement(name = "Reference")
    private Reference reference;

    public CanonicalizationMethod getCanonicalizationMethod() {
        return canonicalizationMethod;
    }

    public void setCanonicalizationMethod(CanonicalizationMethod canonicalizationMethod) {
        this.canonicalizationMethod = canonicalizationMethod;
    }

    public SignatureMethod getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(SignatureMethod signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }
}
