package com.example.nfe.api.dto;

import com.example.nfe.domain.Address;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * API representation of the NF-e issuer.
 * <p>
 * {@code stateRegistration} maps to {@code emit/IE} (Inscrição Estadual do
 * emitente). It is optional at the API boundary and is validated against
 * the official {@code TIe} lexical pattern ({@code [0-9]{2,14}|ISENTO}) —
 * nothing is invented and no fiscal obligation rule is applied: whether a
 * given regime requires an IE is a business rule that this API does not
 * model, so requests without it are accepted and the value simply stays
 * absent in the XML (the bundled PL_010f XSD treats {@code emit/IE} as
 * optional).
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
        @Pattern(regexp = "[0-9]{2,14}|ISENTO",
                message = "stateRegistration must be 2 to 14 digits or 'ISENTO' (official TIe pattern)")
        String stateRegistration,
        String tradeName,
        String taxRegime,
        @Valid Address address) {

    /**
     * Convenience constructor preserving the pre-IE call surface: callers
     * that do not supply a state registration keep working unchanged (the
     * IE is simply absent, exactly as before this field existed).
     */
    public IssuerDto(String name, String document, String tradeName, String taxRegime) {
        this(name, document, null, tradeName, taxRegime, null);
    }

    /**
     * Convenience constructor preserving the pre-IE call surface with an
     * address.
     */
    public IssuerDto(String name, String document, String tradeName, String taxRegime, Address address) {
        this(name, document, null, tradeName, taxRegime, address);
    }
}
