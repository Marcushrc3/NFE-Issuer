package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the recipient address ({@code enderDest}), in the
 * exact PL_010f {@code TEndereco} sequence for the fields the domain
 * provides: xLgr, nro, xBairro, cMun, xMun, UF, CEP. Fields without a
 * domain counterpart (xCpl, cPais, xPais, fone) are omitted rather than
 * defaulted.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EnderDest {

    @XmlElement(name = "xLgr")
    private String street;

    @XmlElement(name = "nro")
    private String number;

    @XmlElement(name = "xBairro")
    private String neighborhood;

    @XmlElement(name = "cMun")
    private String municipalityCode;

    @XmlElement(name = "xMun")
    private String city;

    @XmlElement(name = "UF")
    private String state;

    @XmlElement(name = "CEP")
    private String zipCode;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode = municipalityCode;
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

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
