package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.List;

/**
 * JAXB representation of the NF-e {@code infNFe} group, in XSD order:
 * ide, emit, dest, det+, total, transp, pag. Attributes {@code Id}
 * (NFe + 44-digit access key) and {@code versao} are required by the
 * official schema; they are set by the mapper from the access key
 * generator and the XML-layer version constant — never invented in the
 * domain.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class InfNfe {

    @XmlAttribute(name = "Id")
    private String id;

    @XmlAttribute(name = "versao")
    private String versao;

    @XmlElement(name = "ide")
    private Ide ide;

    @XmlElement(name = "emit")
    private Emit emit;

    @XmlElement(name = "dest")
    private Dest dest;

    @XmlElement(name = "det")
    private List<Det> dets;

    @XmlElement(name = "total")
    private Total total;

    @XmlElement(name = "transp")
    private Transp transp;

    @XmlElement(name = "pag")
    private Pag pag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public Ide getIde() {
        return ide;
    }

    public void setIde(Ide ide) {
        this.ide = ide;
    }

    public Emit getEmit() {
        return emit;
    }

    public void setEmit(Emit emit) {
        this.emit = emit;
    }

    public Dest getDest() {
        return dest;
    }

    public void setDest(Dest dest) {
        this.dest = dest;
    }

    public List<Det> getDets() {
        return dets;
    }

    public void setDets(List<Det> dets) {
        this.dets = dets;
    }

    public Total getTotal() {
        return total;
    }

    public void setTotal(Total total) {
        this.total = total;
    }

    public Transp getTransp() {
        return transp;
    }

    public void setTransp(Transp transp) {
        this.transp = transp;
    }

    public Pag getPag() {
        return pag;
    }

    public void setPag(Pag pag) {
        this.pag = pag;
    }
}
