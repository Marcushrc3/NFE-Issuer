package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code imposto} group.
 * <p>
 * This increment models ICMS, IPI, II, PIS, COFINS, IS and the IBSCBS core
 * (in official schema order).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Imposto {

    @XmlElement(name = "ICMS")
    private Icms icms;

    @XmlElement(name = "IPI")
    private Ipi ipi;

    @XmlElement(name = "II")
    private Ii ii;

    @XmlElement(name = "PIS")
    private Pis pis;

    @XmlElement(name = "COFINS")
    private Cofins cofins;

    @XmlElement(name = "IS")
    private Is is;

    @XmlElement(name = "IBSCBS")
    private IbsCbs ibsCbs;

    public Icms getIcms() {
        return icms;
    }

    public void setIcms(Icms icms) {
        this.icms = icms;
    }

    public Ipi getIpi() {
        return ipi;
    }

    public void setIpi(Ipi ipi) {
        this.ipi = ipi;
    }

    public Ii getIi() {
        return ii;
    }

    public void setIi(Ii ii) {
        this.ii = ii;
    }

    public Pis getPis() {
        return pis;
    }

    public void setPis(Pis pis) {
        this.pis = pis;
    }

    public Cofins getCofins() {
        return cofins;
    }

    public void setCofins(Cofins cofins) {
        this.cofins = cofins;
    }

    public Is getIs() {
        return is;
    }

    public void setIs(Is is) {
        this.is = is;
    }

    public IbsCbs getIbsCbs() {
        return ibsCbs;
    }

    public void setIbsCbs(IbsCbs ibsCbs) {
        this.ibsCbs = ibsCbs;
    }
}
