package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code transporta} group (carrier), in the
 * exact PL_010f sequence for the fields the domain provides: CNPJ (of the
 * CNPJ|CPF choice — the domain does not distinguish document types), xNome,
 * xEnder, xMun, UF. IE is not provided by the domain and remains absent.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Transporta {

    @XmlElement(name = "CNPJ")
    private String document;

    @XmlElement(name = "xNome")
    private String name;

    @XmlElement(name = "xEnder")
    private String street;

    @XmlElement(name = "xMun")
    private String city;

    @XmlElement(name = "UF")
    private String state;

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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
