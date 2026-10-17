package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code total} group, mapped from the
 * domain {@link com.example.nfe.domain.Totals} and
 * {@link com.example.nfe.domain.TaxTotals}.
 * <p>
 * Modelled children (in official schema order): {@code ICMSTot}
 * (mandatory), {@code ISTot} (optional) and {@code IBSCBSTot} (optional).
 * ISSQNtot and retTrib are not provided by the current domain and remain
 * absent.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Total {

    @XmlElement(name = "ICMSTot")
    private IcmsTot icmsTot;

    @XmlElement(name = "ISTot")
    private Istot istot;

    @XmlElement(name = "IBSCBSTot")
    private IbsCbsTot ibsCbsTot;

    public IcmsTot getIcmsTot() {
        return icmsTot;
    }

    public void setIcmsTot(IcmsTot icmsTot) {
        this.icmsTot = icmsTot;
    }

    public Istot getIstot() {
        return istot;
    }

    public void setIstot(Istot istot) {
        this.istot = istot;
    }

    public IbsCbsTot getIbsCbsTot() {
        return ibsCbsTot;
    }

    public void setIbsCbsTot(IbsCbsTot ibsCbsTot) {
        this.ibsCbsTot = ibsCbsTot;
    }
}
