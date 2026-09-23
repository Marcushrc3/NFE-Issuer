package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code total} group. Only the mandatory
 * {@code ICMSTot} child is modelled; ISSQNtot, retTrib, ISTot and
 * IBSCBSTot are not provided by the current domain and remain absent.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Total {

    @XmlElement(name = "ICMSTot")
    private IcmsTot icmsTot;

    public IcmsTot getIcmsTot() {
        return icmsTot;
    }

    public void setIcmsTot(IcmsTot icmsTot) {
        this.icmsTot = icmsTot;
    }
}
