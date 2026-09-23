package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code dest} group, in the exact PL_010f
 * sequence for the fields the domain provides: CNPJ (of the CNPJ|CPF|
 * idEstrangeiro choice), xNome, enderDest, indIEDest. IE, ISUF, IM and
 * email are omitted because the domain does not provide them.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Dest {

    @XmlElement(name = "CNPJ")
    private String document;

    @XmlElement(name = "xNome")
    private String name;

    @XmlElement(name = "enderDest")
    private EnderDest address;

    @XmlElement(name = "indIEDest")
    private String ieStatusIndicator;

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnderDest getAddress() {
        return address;
    }

    public void setAddress(EnderDest address) {
        this.address = address;
    }

    public String getIeStatusIndicator() {
        return ieStatusIndicator;
    }

    public void setIeStatusIndicator(String ieStatusIndicator) {
        this.ieStatusIndicator = ieStatusIndicator;
    }
}
