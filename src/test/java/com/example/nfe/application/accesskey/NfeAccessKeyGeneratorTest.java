package com.example.nfe.application.accesskey;

import com.example.nfe.domain.Address;
import com.example.nfe.domain.EmissionStatus;
import com.example.nfe.domain.Issuer;
import com.example.nfe.domain.NfeEmission;
import com.example.nfe.domain.NfeItem;
import com.example.nfe.domain.OperationType;
import com.example.nfe.domain.Recipient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NfeAccessKeyGeneratorTest {

    private static final String FULL_43_DIGITS = "3526091234567800019955003000001000100000001";

    @Test
    void shouldGenerateDeterministicAccessKey() {
        NfeEmission emission = emission("35", "00000001", "1", 3, 1000L, "8");

        assertEquals("35260912345678000199550030000010001000000018",
                NfeAccessKeyGenerator.generate(emission));
    }

    @Test
    void shouldRejectMismatchedExplicitCheckDigit() {
        NfeEmission emission = emission("35", "00000001", "1", 3, 1000L, "0");

        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> NfeAccessKeyGenerator.generate(emission));

        assertEquals("checkDigit does not match the NF-e access key calculation", ex.getMessage());
    }

    @Test
    void shouldComputeOfficialCheckDigit() {
        assertEquals("8", NfeAccessKeyGenerator.checkDigit(FULL_43_DIGITS));
    }

    @Test
    void shouldZeroCheckDigitWhenRemainderWouldExceedNine() {
        // Crafted so the modulo-11 remainder is 0 -> dv = 11 -> 0.
        assertEquals("0", NfeAccessKeyGenerator.checkDigit("0000000000000000000000000000000000000000000"));
    }

    @Test
    void shouldReturnNullWhenComponentsAreMissing() {
        NfeEmission missingCnf = emission("35", null, "1", 3, 1000L);
        NfeEmission missingSeries = emission("35", "00000001", "1", null, 1000L);
        NfeEmission missingDate = emission("35", "00000001", "1", 3, 1000L, null);
        NfeEmission missingState = emission(null, "00000001", "1", 3, 1000L);

        assertNull(NfeAccessKeyGenerator.generate(missingCnf));
        assertNull(NfeAccessKeyGenerator.generate(missingSeries));
        assertNull(NfeAccessKeyGenerator.generate(missingDate));
        assertNull(NfeAccessKeyGenerator.generate(missingState));
    }

    private static NfeEmission emission(String stateCode, String randomCode, String emissionType,
                                        Integer series, Long number) {
        return emission(stateCode, randomCode, emissionType, series, number,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"), "9");
    }

    private static NfeEmission emission(String stateCode, String randomCode, String emissionType,
                                        Integer series, Long number, String checkDigit) {
        return emission(stateCode, randomCode, emissionType, series, number,
                OffsetDateTime.parse("2026-09-21T10:30:00-03:00"), checkDigit);
    }

    private static NfeEmission emission(String stateCode, String randomCode, String emissionType,
                                        Integer series, Long number, OffsetDateTime date, String checkDigit) {
        Issuer issuer = new Issuer("Acme Ltd", "12345678000199", "123456789",
                new Address("Main St", "100", "Sao Paulo", "SP", "01310-100", "Centro", "3550308"),
                null, null);
        Recipient recipient = new Recipient("Beta Corp", "98765432000188", null, null);
        NfeItem item = new NfeItem("P-1", "Widget", "84818090", null, null, null,
                "UN", new BigDecimal("1"), new BigDecimal("10.50"), new BigDecimal("10.50"),
                null, null, null, null, null, null, null,
                "5102", "0", null, null, null, null, null);
        return new NfeEmission(
                "emission-1", "REF-1", OperationType.TRANSFER, EmissionStatus.RECEIVED,
                "55", series, number, date, "Remessa para industrialização", "3550308",
                stateCode, randomCode, "1", "1", "1", emissionType, checkDigit, "2", "1", "1", "1", "0", "1.0",
                issuer, recipient, List.of(item),
                null, null, null, null, null, null);
    }
}
