package com.example.nfe.api.dto;

import com.example.nfe.domain.Address;
import com.example.nfe.domain.RecipientIeStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * API representation of the NF-e recipient.
 * <p>
 * {@code ieStatus} is optional and carries no defaults — when omitted the
 * generated XML remains schema-invalid until the source supplies it.
 * <p>
 * {@code stateRegistration} maps to {@code dest/IE} (Inscrição Estadual do
 * destinatário). It is optional at the API boundary and is validated
 * against the official {@code TIeDest} lexical pattern
 * ({@code ISENTO|[0-9]{2,14}}) — nothing is invented: when omitted, the
 * value simply stays absent in the XML. Whether a recipient requires an IE
 * depends on {@code indIEDest} and is a business rule the API does not
 * model (the official XSD treats {@code dest/IE} as optional).
 * <p>
 * {@code address} maps to {@code dest/enderDest}, which is optional in the
 * official PL_010f schema. It is optional at the API boundary on purpose:
 * when omitted or null, the mapped domain address remains null and the XML
 * mapper emits no address — the API never invents address data. Structural
 * rules come from the shared {@link Address} domain value object only.
 */
public record RecipientDto(
        @NotBlank(message = "name must not be blank") String name,
        @NotBlank(message = "document must not be blank") String document,
        RecipientIeStatus ieStatus,
        @Pattern(regexp = "[0-9]{2,14}|ISENTO",
                message = "stateRegistration must be 2 to 14 digits or 'ISENTO' (official TIeDest pattern)")
        String stateRegistration,
        @Valid Address address) {

    /**
     * Convenience constructor preserving the pre-address call surface:
     * callers that do not supply an address keep working unchanged.
     */
    public RecipientDto(String name, String document, RecipientIeStatus ieStatus) {
        this(name, document, ieStatus, null, null);
    }

    /**
     * Convenience constructor preserving the pre-IE call surface: callers
     * that do not supply a state registration keep working unchanged.
     */
    public RecipientDto(String name, String document, RecipientIeStatus ieStatus, Address address) {
        this(name, document, ieStatus, null, address);
    }
}
