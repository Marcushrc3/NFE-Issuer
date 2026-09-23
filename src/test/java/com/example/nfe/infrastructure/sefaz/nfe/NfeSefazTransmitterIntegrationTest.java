package com.example.nfe.infrastructure.sefaz.nfe;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * OPTIONAL manual integration test against the real SEFAZ-SP homologation
 * environment. Disabled by default — it requires a valid ICP-Brasil A1
 * certificate and network access, and it performs a REAL SEFAZ call.
 * <p>
 * Manual execution steps (never part of {@code mvn test}):
 * <ol>
 *   <li>Produce a signed NF-e (EMIT flow with signing credentials) whose
 *       {@code tpAmb} is 2 and {@code cUF} is 35.</li>
 *   <li>Set {@code nfe.sefaz.enabled=true} and keep
 *       {@code nfe.sefaz.environment=HOMOLOGATION} with the default
 *       endpoint {@code https://homologacao.nfe.fazenda.sp.gov.br/ws/nfeautorizacao4.asmx}.</li>
 *   <li>Invoke {@link NfeSefazTransmitter#transmit(String)} with the signed XML.</li>
 * </ol>
 */
@Disabled("Requires a real ICP-Brasil certificate and network access to SEFAZ-SP homologation. "
        + "Perform real calls manually — never from the regular test suite.")
class NfeSefazTransmitterIntegrationTest {

    @Test
    void transmitRealSignedNfeToHomologation() {
        // Deliberately empty: manual instructions live in the class javadoc.
    }
}
