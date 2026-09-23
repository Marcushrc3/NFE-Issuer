package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TransportTest {

    private static final Address DELIVERY_ADDRESS =
            new Address("Port St", "1", "Santos", "SP", "11013-100", "Centro", "3548500");

    @Test
    void shouldAllowNullFreightModeAndOptionalFields() {
        Transport transport = new Transport(null, null, null, null);

        assertNull(transport.freightMode());
        assertNull(transport.carrierDocument());
        assertNull(transport.carrierName());
        assertNull(transport.deliveryAddress());
    }

    @Test
    void shouldPreserveFreightModeExactlyAsSupplied() {
        Transport transport = new Transport("0", null, null, null);

        assertEquals("0", transport.freightMode());
    }

    @Test
    void shouldKeepExistingFieldsUnchanged() {
        Transport transport = new Transport("1", "12345678000199", "Acme Carrier", DELIVERY_ADDRESS);

        assertEquals("1", transport.freightMode());
        assertEquals("12345678000199", transport.carrierDocument());
        assertEquals("Acme Carrier", transport.carrierName());
        assertSame(DELIVERY_ADDRESS, transport.deliveryAddress());
    }
}
