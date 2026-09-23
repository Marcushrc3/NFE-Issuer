package com.example.nfe.application.sefaz;

/**
 * NF-e authorization result extracted from the SEFAZ batch response
 * ({@code protNFe/infProt}).
 * <p>
 * Pure data with simple Java types only — no JAXB/DOM/SOAP types. Every
 * component except {@code status} is nullable: they exist only when the
 * official response carried them. The record is {@code null} on the
 * transmission result whenever SEFAZ did not emit an authorization or
 * denial protocol for the document.
 *
 * @param status      authorized or rejected, per the official protocol status
 * @param code        official protocol status code (infProt/cStat)
 * @param message     official reason (infProt/xMotivo)
 * @param protocol    authorization/denial protocol number (nProt)
 * @param accessKey   NF-e access key echoed by SEFAZ (chNFe)
 * @param digestValue digest of the authorized NF-e (digVal)
 */
public record SefazAuthorizationResult(
        SefazAuthorizationStatus status,
        Integer code,
        String message,
        String protocol,
        String accessKey,
        String digestValue) {
}
