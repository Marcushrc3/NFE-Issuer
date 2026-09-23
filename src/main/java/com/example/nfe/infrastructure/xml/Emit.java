package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code emit} group. Field order follows
 * the official PL_010f schema: CNPJ|CPF (choice), xNome, xFant?, enderEmit,
 * IE?, IEST?, IM?/CNAE?, CRT, ISUFEmit?.
 * <p>
 * The issuer document is mapped to {@code CNPJ} until the domain
 * distinguishes CNPJ/CPF. Fields without a domain counterpart are omitted
 * rather than defaulted: IEST, IM, CNAE and ISUFEmit are not represented
 * and must never be fabricated.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Emit {

    @XmlElement(name = "CNPJ")
    private String document;

    @XmlElement(name = "xNome")
    private String name;

    @XmlElement(name = "xFant")
    private String tradeName;

    @XmlElement(name = "enderEmit")
    private EnderEmit address;

    @XmlElement(name = "IE")
    private String stateRegistration;

    @XmlElement(name = "CRT")
    private String taxRegime;

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

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public EnderEmit getAddress() {
        return address;
    }

    public void setAddress(EnderEmit address) {
        this.address = address;
    }

    public String getStateRegistration() {
        return stateRegistration;
    }

    public void setStateRegistration(String stateRegistration) {
        this.stateRegistration = stateRegistration;
    }

    public String getTaxRegime() {
        return taxRegime;
    }

    public void setTaxRegime(String taxRegime) {
        this.taxRegime = taxRegime;
    }
}
