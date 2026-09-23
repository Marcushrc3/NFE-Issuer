package com.example.nfe.api.dto;

import com.example.nfe.application.sefaz.SefazTransmissionStatus;

/**
 * API representation of the SEFAZ batch/transmission response.
 * <p>
 * API-safe simple types only — no SOAP/DOM/JAXB/HTTP or infrastructure
 * types. {@code code} = retEnviNFe/cStat, {@code message} =
 * retEnviNFe/xMotivo, {@code receipt} = infRec/nRec. The raw SOAP response
 * is deliberately NOT exposed through the REST contract: it is an
 * infrastructure diagnostic artifact and may carry unnecessary content.
 */
public record SefazTransmissionResponseDto(
        SefazTransmissionStatus status,
        Integer code,
        String message,
        String receipt,
        SefazAuthorizationResponseDto authorization) {
}
