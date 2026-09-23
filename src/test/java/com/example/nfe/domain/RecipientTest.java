package com.example.nfe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecipientTest {

    @Test
    void shouldPreserveEachIeStatusValue() {
        assertEquals(RecipientIeStatus.CONTRIBUTOR,
                new Recipient("Beta Corp", "98765432000188", null,
                        RecipientIeStatus.CONTRIBUTOR).ieStatus());
        assertEquals(RecipientIeStatus.EXEMPT,
                new Recipient("Beta Corp", "98765432000188", null,
                        RecipientIeStatus.EXEMPT).ieStatus());
        assertEquals(RecipientIeStatus.NOT_CONTRIBUTOR,
                new Recipient("Beta Corp", "98765432000188", null,
                        RecipientIeStatus.NOT_CONTRIBUTOR).ieStatus());
    }

    @Test
    void shouldAllowNullIeStatus() {
        Recipient recipient = new Recipient("Beta Corp", "98765432000188", null, null);

        assertNull(recipient.ieStatus());
    }
}
