package com.example.nfe.infrastructure.signature;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * TEST-ONLY credential loader: reads the throwaway self-signed certificate
 * and private key from the committed test keystore
 * ({@code src/test/resources/testcert/nfe-test.p12}, generated once with
 * keytool). This fixture is explicitly NOT production configuration and
 * never used outside tests. The password is a test-fixture constant, not a
 * secret.
 */
public final class SelfSignedTestCertificate {

    /** Path of the committed test-only keystore, relative to the project root. */
    public static final String KEYSTORE_FILE = "src/test/resources/testcert/nfe-test.p12";
    public static final String KEYSTORE_PASSWORD = "changeit";
    public static final String KEY_ALIAS = "nfe-test";

    private static final String KEYSTORE_RESOURCE = "/testcert/nfe-test.p12";

    private SelfSignedTestCertificate() {
    }

    public static SigningCredentials load() throws Exception {
        try (InputStream in = SelfSignedTestCertificate.class.getResourceAsStream(KEYSTORE_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("test keystore not found: " + KEYSTORE_RESOURCE);
            }
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(in, KEYSTORE_PASSWORD.toCharArray());
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, KEYSTORE_PASSWORD.toCharArray());
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(KEY_ALIAS);
            return new SigningCredentials(privateKey, certificate);
        }
    }
}
