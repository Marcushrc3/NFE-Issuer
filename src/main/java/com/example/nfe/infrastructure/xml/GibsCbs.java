package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the NF-e {@code gIBSCBS} group, mapped from the
 * domain {@link com.example.nfe.domain.IbsCbsTaxation}.
 * <p>
 * Verified against the official PL_010f schema ({@code TCIBS_NFe} type):
 * {@code gIBSCBS} = {@code vBC}, then {@code gIBSUF}/{@code gIBSMun}/
 * {@code vIBS}, {@code gCBS}, {@code gTribRegular} and
 * {@code gTribCompraGov}.
 * <p>
 * This increment models {@code vBC}, {@code gIBSUF}, {@code gIBSMun},
 * {@code vIBS}, {@code gCBS} and {@code gTribRegular}.
 * {@code gALCZFMCBS} and {@code gTribCompraGov} are not modelled by the
 * domain and remain absent.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GibsCbs {

    @XmlElement(name = "vBC")
    private BigDecimal taxBase;

    @XmlElement(name = "gIBSUF")
    private IbsUf ibsUf;

    @XmlElement(name = "gIBSMun")
    private IbsMun ibsMun;

    @XmlElement(name = "vIBS")
    private BigDecimal ibsAmount;

    @XmlElement(name = "gCBS")
    private Cbs cbs;

    @XmlElement(name = "gTribRegular")
    private TribRegular tribRegular;

    public BigDecimal getTaxBase() {
        return taxBase;
    }

    public void setTaxBase(BigDecimal taxBase) {
        this.taxBase = taxBase;
    }

    public IbsUf getIbsUf() {
        return ibsUf;
    }

    public void setIbsUf(IbsUf ibsUf) {
        this.ibsUf = ibsUf;
    }

    public IbsMun getIbsMun() {
        return ibsMun;
    }

    public void setIbsMun(IbsMun ibsMun) {
        this.ibsMun = ibsMun;
    }

    public BigDecimal getIbsAmount() {
        return ibsAmount;
    }

    public void setIbsAmount(BigDecimal ibsAmount) {
        this.ibsAmount = ibsAmount;
    }

    public Cbs getCbs() {
        return cbs;
    }

    public void setCbs(Cbs cbs) {
        this.cbs = cbs;
    }

    public TribRegular getTribRegular() {
        return tribRegular;
    }

    public void setTribRegular(TribRegular tribRegular) {
        this.tribRegular = tribRegular;
    }
}
