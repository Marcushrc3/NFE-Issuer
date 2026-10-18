package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * JAXB representation of the NF-e root element. This is an XML-layer class;
 * the domain model stays free of JAXB annotations.
 * <p>
 * The {@code ds:Signature} element (required by the official schema, after
 * the optional infNFeSupl) is added exclusively by the signing
 * implementation ({@code XmlSigner}) via DOM — this class deliberately
 * declares no signature slot, so the generated unsigned XML never carries
 * an unused xmldsig namespace declaration (official business rule 587).
 */
@XmlRootElement(name = "NFe")
@XmlAccessorType(XmlAccessType.FIELD)
public class Nfe {

    @XmlElement(name = "infNFe")
    private InfNfe infNfe;

    public InfNfe getInfNfe() {
        return infNfe;
    }

    public void setInfNfe(InfNfe infNfe) {
        this.infNfe = infNfe;
    }
}
