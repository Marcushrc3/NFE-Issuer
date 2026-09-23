package com.example.nfe.api.dto;

import com.example.nfe.domain.Address;
import com.example.nfe.domain.RecipientIeStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * API representation of the NF-e recipient.
 * <p>
 * {@code ieStatus} is optional and carries no defaults — when omitted the
 * generated XML remains schema-invalid until the source supplies it.
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
        @Valid Address address) {

    /**
     * Convenience constructor preserving the pre-address call surface:
     * callers that do not supply an address keep working unchanged.
     */
    public RecipientDto(String name, String document, RecipientIeStatus ieStatus) {
        this(name, document, ieStatus, null);
    }
}
