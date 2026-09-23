package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IssuerTest {

    @Test
    void shouldPreserveIssuerFields() {
        Address address = new Address("Main St", "100", "Sao Paulo", "SP", "01310-100", "Centro", "3550308");
        Issuer issuer = new Issuer("Acme Ltd", "12345678000199", "123456789", address,
                "Acme Comercio Ltda", "3");

        assertEquals("Acme Ltd", issuer.name());
        assertEquals("12345678000199", issuer.document());
        assertEquals("123456789", issuer.stateRegistration());
        assertEquals(address, issuer.address());
        assertEquals("Acme Comercio Ltda", issuer.tradeName());
        assertEquals("3", issuer.taxRegime());
    }

    @Test
    void shouldAllowNullOptionalIssuerFields() {
        Issuer issuer = new Issuer("Acme Ltd", "12345678000199", null, null, null, null);

        assertNull(issuer.stateRegistration());
        assertNull(issuer.address());
        assertNull(issuer.tradeName());
        assertNull(issuer.taxRegime());
    }
}
