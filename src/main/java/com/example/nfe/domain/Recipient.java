package com.example.nfe.domain;

/**
 * Recipient (destinatário) of the NF-e.
 * <p>
 * {@code ieStatus} carries the recipient IE status business meaning
 * ({@link RecipientIeStatus}) and is nullable — no default is assumed when
 * the source does not supply it. {@code stateRegistration} carries
 * {@code dest/IE} (Inscrição Estadual do destinatário) as plain data and
 * is nullable — whether it is required depends on {@code ieStatus} and is
 * a fiscal business rule this domain does not model. {@code address} is a
 * provisional field and does not yet represent the official NF-e model.
 */
public record Recipient(
        String name,
        String document,
        Address address,
        RecipientIeStatus ieStatus,
        String stateRegistration) {

    /**
     * Convenience constructor preserving the pre-IE call surface: callers
     * that do not supply a state registration keep working unchanged.
     */
    public Recipient(String name, String document, Address address, RecipientIeStatus ieStatus) {
        this(name, document, address, ieStatus, null);
    }
}
