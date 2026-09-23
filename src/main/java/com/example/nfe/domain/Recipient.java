package com.example.nfe.domain;

/**
 * Recipient (destinatário) of the NF-e.
 * <p>
 * {@code ieStatus} carries the recipient IE status business meaning
 * ({@link RecipientIeStatus}) and is nullable — no default is assumed when
 * the source does not supply it. {@code address} is a provisional field and
 * does not yet represent the official NF-e model.
 */
public record Recipient(
        String name,
        String document,
        Address address,
        RecipientIeStatus ieStatus) {
}
