package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.List;

/**
 * JAXB representation of the NF-e {@code pag} group, holding one or more
 * {@code detPag} details in XSD order. The group is only generated when
 * the domain provides payment data — never as an empty element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Pag {

    @XmlElement(name = "detPag")
    private List<DetPag> detPags;

    public List<DetPag> getDetPags() {
        return detPags;
    }

    public void setDetPags(List<DetPag> detPags) {
        this.detPags = detPags;
    }
}
