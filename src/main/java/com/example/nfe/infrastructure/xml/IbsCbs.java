package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code IBSCBS} group (IBS/CBS core),
 * mapped from the domain {@link com.example.nfe.domain.RtcTaxation}.
 * <p>
 * Verified against the official PL_010f schema ({@code TTribNFe} type):
 * {@code CST} and {@code cClassTrib} are required, {@code indDoacao} is
 * optional, and {@code gIBSCBS} is one of a choice of nested groups.
 * <p>
 * This increment maps ONLY the core: {@code CST}, {@code cClassTrib},
 * {@code indDoacao} and {@code gIBSCBS/vBC}. Deferred to future increments:
 * {@code gIBSUF}/{@code gIBSMun}/{@code vIBS}/{@code gCBS},
 * {@code gTribRegular}, {@code gDif}/{@code gDevTrib}/{@code gRed},
 * {@code gALCZFMCBS}, monophase, credit groups, {@code gEstornoCred},
 * {@code gTransfCred}, {@code gAjusteCompet} and {@code gTribCompraGov}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IbsCbs {

    @XmlElement(name = "CST")
    private String cst;

    @XmlElement(name = "cClassTrib")
    private String cClassTrib;

    @XmlElement(name = "indDoacao")
    private String indDoacao;

    @XmlElement(name = "gIBSCBS")
    private GibsCbs gibsCbs;

    public String getCst() {
        return cst;
    }

    public void setCst(String cst) {
        this.cst = cst;
    }

    public String getCClassTrib() {
        return cClassTrib;
    }

    public void setCClassTrib(String cClassTrib) {
        this.cClassTrib = cClassTrib;
    }

    public String getIndDoacao() {
        return indDoacao;
    }

    public void setIndDoacao(String indDoacao) {
        this.indDoacao = indDoacao;
    }

    public GibsCbs getGibsCbs() {
        return gibsCbs;
    }

    public void setGibsCbs(GibsCbs gibsCbs) {
        this.gibsCbs = gibsCbs;
    }
}
