package com.example.nfe.api.dto;

import com.example.nfe.domain.Address;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * API representation of the NF-e issuer.
 * <p>
 * {@code tradeName} ({@code xFant}) and {@code taxRegime} ({@code CRT}) are
 * optional and carry no business validation or defaults.
 * <p>
 * {@code address} maps to {@code emit/enderEmit}, which is mandatory in the
 * official PL_010f schema. It is optional at the API boundary on purpose:
 * when omitted or null, the mapped domain address remains null, the XML
 * mapper emits no address and XSD validation rejects the incomplete NF-e —
 * the API never invents address data. The shared {@link Address} domain
 * value object enforces only the structural rule the schema requires
 * (7-digit {@code municipalityCode}); no business defaults or derivations
 * exist.
 */
public record IssuerDto(
        @NotBlank(message = "name must not be blank") String name,
        @NotBlank(message = "document must not be blank") String document,
        String tradeName,
        String taxRegime,
        @Valid Address address) {

    /**
     * Convenience constructor preserving the pre-address call surface:
     * callers that do not supply an address keep working unchanged (the
     * address is simply absent, exactly as before this contract existed).
     */
    public IssuerDto(String name, String document, String tradeName, String taxRegime) {
        this(name, document, tradeName, taxRegime, null);
    }
}
