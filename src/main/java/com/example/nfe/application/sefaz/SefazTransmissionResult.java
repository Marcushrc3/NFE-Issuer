package com.example.nfe.application.sefaz;

/**
 * Structured result of a SEFAZ transmission attempt, keeping the two
 * official response levels explicitly separated:
 * <ul>
 *   <li><b>batch/transmission level</b> — the {@code retEnviNFe} answer:
 *       status code (cStat), reason (xMotivo) and receipt (infRec/nRec);</li>
 *   <li><b>authorization level</b> — the {@code protNFe/infProt} answer for
 *       the document, present only when SEFAZ emitted an authorization or
 *       denial protocol.</li>
 * </ul>
 * Pure data — no XML parsing or SEFAZ-specific processing lives here, and
 * no JAXB/DOM/SOAP types are exposed. {@code transmissionCode},
 * {@code transmissionMessage}, {@code receipt},
 * {@code authorizationResult} and {@code rawResponse} are nullable: a
 * rejected or failed transmission carries no receipt, and a response
 * without {@code infProt} carries no authorization result.
 */
public record SefazTransmissionResult(
        SefazTransmissionStatus transmissionStatus,
        Integer transmissionCode,
        String transmissionMessage,
        String receipt,
        SefazAuthorizationResult authorizationResult,
        String rawResponse) {
}
