package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

/**
 * JAXB representation of the official {@code ICMS30} variant (CST 30 —
 * isenta ou não tributada com cobrança do ICMS por substituição
 * tributária), in the exact PL_010f sequence: orig, CST, modBCST, pMVAST,
 * pRedBCST, vBCST, pICMSST, vICMSST, vBCFCPST, pFCPST, vFCPST, vICMSDeson,
 * motDesICMS. pMVAST and pRedBCST are optional; all other elements are
 * required by the schema.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Icms30 {

    @XmlElement(name = "orig")
    private String origin;

    @XmlElement(name = "CST")
    private String cst;

    @XmlElement(name = "modBCST")
    private String modBcSt;

    @XmlElement(name = "pMVAST")
    private BigDecimal stMarginRate;

    @XmlElement(name = "pRedBCST")
    private BigDecimal stReductionRate;

    @XmlElement(name = "vBCST")
    private BigDecimal stBase;

    @XmlElement(name = "pICMSST")
    private BigDecimal stRate;

    @XmlElement(name = "vICMSST")
    private BigDecimal stAmount;

    @XmlElement(name = "vBCFCPST")
    private BigDecimal fcpStBase;

    @XmlElement(name = "pFCPST")
    private BigDecimal fcpStRate;

    @XmlElement(name = "vFCPST")
    private BigDecimal fcpStAmount;

    @XmlElement(name = "vICMSDeson")
    private BigDecimal desonerationAmount;

    @XmlElement(name = "motDesICMS")
    private String desonerationReason;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCst() {
        return cst;
    }

    public void setCst(String cst) {
        this.cst = cst;
    }

    public String getModBcSt() {
        return modBcSt;
    }

    public void setModBcSt(String modBcSt) {
        this.modBcSt = modBcSt;
    }

    public BigDecimal getStMarginRate() {
        return stMarginRate;
    }

    public void setStMarginRate(BigDecimal stMarginRate) {
        this.stMarginRate = stMarginRate;
    }

    public BigDecimal getStReductionRate() {
        return stReductionRate;
    }

    public void setStReductionRate(BigDecimal stReductionRate) {
        this.stReductionRate = stReductionRate;
    }

    public BigDecimal getStBase() {
        return stBase;
    }

    public void setStBase(BigDecimal stBase) {
        this.stBase = stBase;
    }

    public BigDecimal getStRate() {
        return stRate;
    }

    public void setStRate(BigDecimal stRate) {
        this.stRate = stRate;
    }

    public BigDecimal getStAmount() {
        return stAmount;
    }

    public void setStAmount(BigDecimal stAmount) {
        this.stAmount = stAmount;
    }

    public BigDecimal getFcpStBase() {
        return fcpStBase;
    }

    public void setFcpStBase(BigDecimal fcpStBase) {
        this.fcpStBase = fcpStBase;
    }

    public BigDecimal getFcpStRate() {
        return fcpStRate;
    }

    public void setFcpStRate(BigDecimal fcpStRate) {
        this.fcpStRate = fcpStRate;
    }

    public BigDecimal getFcpStAmount() {
        return fcpStAmount;
    }

    public void setFcpStAmount(BigDecimal fcpStAmount) {
        this.fcpStAmount = fcpStAmount;
    }

    public BigDecimal getDesonerationAmount() {
        return desonerationAmount;
    }

    public void setDesonerationAmount(BigDecimal desonerationAmount) {
        this.desonerationAmount = desonerationAmount;
    }

    public String getDesonerationReason() {
        return desonerationReason;
    }

    public void setDesonerationReason(String desonerationReason) {
        this.desonerationReason = desonerationReason;
    }
}
