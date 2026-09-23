package com.example.nfe.infrastructure.xml;

import com.example.nfe.infrastructure.xml.signature.Signature;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * JAXB representation of the NF-e root element. This is an XML-layer class;
 * the domain model stays free of JAXB annotations.
 * <p>
 * The {@code ds:Signature} slot (required by the official schema, after the
 * optional infNFeSupl) is populated exclusively by a signing implementation
 * ({@code XmlSigner}) — the mapper never fabricates signatures.
 */
@XmlRootElement(name = "NFe")
@XmlAccessorType(XmlAccessType.FIELD)
public class Nfe {

    @XmlElement(name = "infNFe")
    private InfNfe infNfe;

    @XmlElement(name = "Signature", namespace = "http://www.w3.org/2000/09/xmldsig#")
    private Signature signature;

    public InfNfe getInfNfe() {
        return infNfe;
    }

    public void setInfNfe(InfNfe infNfe) {
        this.infNfe = infNfe;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }
}
