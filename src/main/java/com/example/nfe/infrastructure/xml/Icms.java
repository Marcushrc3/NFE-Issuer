package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code ICMS} wrapper. The official schema
 * models ICMS as a choice of CST-specific variant groups; this class holds
 * the variants currently implemented (ICMS00, ICMS30, ICMS40). Exactly one
 * variant is set by the mapper, so exactly one child element is generated.
 * <p>
 * Variants not implemented (ICMS02, ICMS10, ICMS15, ICMS20, ICMS51, ICMS53,
 * ICMS60, ICMS61, ICMS70, ICMS90, ICMSPart, ICMSSN101/102/201/202/500/900,
 * ICMSST) are intentionally absent: the mapper fails explicitly instead of
 * silently producing schema-invalid XML for them.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Icms {

    @XmlElement(name = "ICMS00")
    private Icms00 icms00;

    @XmlElement(name = "ICMS30")
    private Icms30 icms30;

    @XmlElement(name = "ICMS40")
    private Icms40 icms40;

    public Icms00 getIcms00() {
        return icms00;
    }

    public void setIcms00(Icms00 icms00) {
        this.icms00 = icms00;
    }

    public Icms30 getIcms30() {
        return icms30;
    }

    public void setIcms30(Icms30 icms30) {
        this.icms30 = icms30;
    }

    public Icms40 getIcms40() {
        return icms40;
    }

    public void setIcms40(Icms40 icms40) {
        this.icms40 = icms40;
    }
}
