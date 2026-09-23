package com.example.nfe.api.dto;

import com.example.nfe.application.sefaz.SefazAuthorizationStatus;

/**
 * API representation of the NF-e authorization result inside a SEFAZ
 * transmission response.
 * <p>
 * API-safe simple types only — no SOAP/DOM/JAXB/HTTP or infrastructure
 * types. Every component except {@code status} is nullable and mirrors the
 * official SEFAZ protocol fields: {@code code} = infProt/cStat,
 * {@code message} = infProt/xMotivo, {@code protocol} = nProt,
 * {@code accessKey} = chNFe, {@code digestValue} = digVal.
 */
public record SefazAuthorizationResponseDto(
        SefazAuthorizationStatus status,
        Integer code,
        String message,
        String protocol,
        String accessKey,
        String digestValue) {
}
