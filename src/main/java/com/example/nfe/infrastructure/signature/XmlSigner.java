package com.example.nfe.infrastructure.signature;

/**
 * Abstraction for XML digital signing of generated NF-e documents.
 * <p>
 * Implementations are responsible for the full XMLDSig pipeline
 * (canonicalization, digest, signature creation, KeyInfo/certificate) —
 * none of which is embedded in the application itself. No implementation
 * is provided yet: the {@code ds:Signature} JAXB structure is the
 * structural foundation built in this increment, and cryptographic
 * signing plus certificate management are follow-up work.
 * <p>
 * The expected flow is: domain → XML mapping → XML generation → XSD
 * validation → access key / Id finalized → {@link #sign} → signed XML.
 */
public interface XmlSigner {

    /**
     * Signs the given NF-e XML document and returns the signed document.
     *
     * @param xml the generated (and XSD-validated) NF-e XML
     * @return the signed XML
     */
    String sign(String xml);
}
