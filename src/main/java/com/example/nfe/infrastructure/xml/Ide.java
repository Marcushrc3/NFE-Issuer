package com.example.nfe.infrastructure.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * JAXB representation of the NF-e {@code ide} group. Field order follows the
 * official PL_010f schema: cUF, cNF, natOp, mod, serie, nNF, dhEmi, tpNF,
 * idDest, cMunFG, tpImp, tpEmis, cDV, tpAmb, finNFe, indFinal, indPres,
 * procEmi, verProc. Optional schema fields not yet available in the domain
 * are deliberately absent rather than defaulted.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Ide {

    @XmlElement(name = "cUF")
    private String stateCode;

    @XmlElement(name = "cNF")
    private String randomCode;

    @XmlElement(name = "natOp")
    private String operationDescription;

    @XmlElement(name = "mod")
    private String model;

    @XmlElement(name = "serie")
    private String series;

    @XmlElement(name = "nNF")
    private String number;

    @XmlElement(name = "dhEmi")
    private String emissionDate;

    @XmlElement(name = "tpNF")
    private String operationDirection;

    @XmlElement(name = "idDest")
    private String destinationType;

    @XmlElement(name = "cMunFG")
    private String fiscalEstablishmentCity;

    @XmlElement(name = "tpImp")
    private String printFormat;

    @XmlElement(name = "tpEmis")
    private String emissionType;

    @XmlElement(name = "cDV")
    private String checkDigit;

    @XmlElement(name = "tpAmb")
    private String environment;

    @XmlElement(name = "finNFe")
    private String purpose;

    @XmlElement(name = "indFinal")
    private String finalConsumer;

    @XmlElement(name = "indPres")
    private String presenceIndicator;

    @XmlElement(name = "procEmi")
    private String processType;

    @XmlElement(name = "verProc")
    private String processVersion;

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getRandomCode() {
        return randomCode;
    }

    public void setRandomCode(String randomCode) {
        this.randomCode = randomCode;
    }

    public String getOperationDescription() {
        return operationDescription;
    }

    public void setOperationDescription(String operationDescription) {
        this.operationDescription = operationDescription;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmissionDate() {
        return emissionDate;
    }

    public void setEmissionDate(String emissionDate) {
        this.emissionDate = emissionDate;
    }

    public String getOperationDirection() {
        return operationDirection;
    }

    public void setOperationDirection(String operationDirection) {
        this.operationDirection = operationDirection;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getFiscalEstablishmentCity() {
        return fiscalEstablishmentCity;
    }

    public void setFiscalEstablishmentCity(String fiscalEstablishmentCity) {
        this.fiscalEstablishmentCity = fiscalEstablishmentCity;
    }

    public String getPrintFormat() {
        return printFormat;
    }

    public void setPrintFormat(String printFormat) {
        this.printFormat = printFormat;
    }

    public String getEmissionType() {
        return emissionType;
    }

    public void setEmissionType(String emissionType) {
        this.emissionType = emissionType;
    }

    public String getCheckDigit() {
        return checkDigit;
    }

    public void setCheckDigit(String checkDigit) {
        this.checkDigit = checkDigit;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getFinalConsumer() {
        return finalConsumer;
    }

    public void setFinalConsumer(String finalConsumer) {
        this.finalConsumer = finalConsumer;
    }

    public String getPresenceIndicator() {
        return presenceIndicator;
    }

    public void setPresenceIndicator(String presenceIndicator) {
        this.presenceIndicator = presenceIndicator;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }
}
