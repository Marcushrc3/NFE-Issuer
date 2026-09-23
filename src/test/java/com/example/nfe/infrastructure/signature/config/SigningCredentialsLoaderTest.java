package com.example.nfe.infrastructure.signature.config;

import com.example.nfe.infrastructure.signature.SelfSignedTestCertificate;
import com.example.nfe.infrastructure.signature.SigningCredentials;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the PKCS#12 signing-credentials loader. Only the committed
 * TEST-ONLY keystore is used; production secrets are never involved.
 */
class SigningCredentialsLoaderTest {

    @Test
    void shouldLoadCredentialsFromTestKeystore() {
        SigningCredentials credentials = SigningCredentialsLoader.load(properties(
                SelfSignedTestCertificate.KEYSTORE_FILE,
                SelfSignedTestCertificate.KEYSTORE_PASSWORD,
                SelfSignedTestCertificate.KEY_ALIAS));

        assertNotNull(credentials.privateKey());
        assertNotNull(credentials.certificate());
        assertEquals("RSA", credentials.privateKey().getAlgorithm());
    }

    @Test
    void shouldFailWhenKeystorePathIsNotConfigured() {
        SigningConfigurationException exception = assertThrows(SigningConfigurationException.class,
                () -> SigningCredentialsLoader.load(properties("", "pw", "alias")));

        assertTrue(exception.getMessage().contains("keystore-path"));
    }

    @Test
    void shouldFailWhenKeystorePasswordIsNotConfigured() {
        SigningConfigurationException exception = assertThrows(SigningConfigurationException.class,
                () -> SigningCredentialsLoader.load(properties(
                        SelfSignedTestCertificate.KEYSTORE_FILE, "", "alias")));

        assertTrue(exception.getMessage().contains("keystore-password"));
    }

    @Test
    void shouldFailWhenAliasIsNotConfigured() {
        SigningConfigurationException exception = assertThrows(SigningConfigurationException.class,
                () -> SigningCredentialsLoader.load(properties(
                        SelfSignedTestCertificate.KEYSTORE_FILE, "pw", "")));

        assertTrue(exception.getMessage().contains("alias"));
    }

    @Test
    void shouldFailWhenKeystoreFileDoesNotExist() {
        SigningConfigurationException exception = assertThrows(SigningConfigurationException.class,
                () -> SigningCredentialsLoader.load(properties(
                        "src/test/resources/testcert/does-not-exist.p12", "pw", "alias")));

        assertTrue(exception.getMessage().contains("cannot be read"));
        assertTrue(exception.getMessage().contains("does-not-exist.p12"));
    }

    @Test
    void shouldFailOnWrongPasswordWithoutExposingIt() {
        String wrongPassword = "secret-wrong-password-123";

        SigningConfigurationException exception = assertThrows(SigningConfigurationException.class,
                () -> SigningCredentialsLoader.load(properties(
                        SelfSignedTestCertificate.KEYSTORE_FILE,
                        wrongPassword,
                        SelfSignedTestCertificate.KEY_ALIAS)));

        assertTrue(exception.getMessage().contains("could not be opened"));
        assertFalse(exception.getMessage().contains(wrongPassword),
                "the password must never appear in the failure message");
    }

    @Test
    void shouldFailWhenAliasDoesNotExist() {
        SigningConfigurationException exception = assertThrows(SigningConfigurationException.class,
                () -> SigningCredentialsLoader.load(properties(
                        SelfSignedTestCertificate.KEYSTORE_FILE,
                        SelfSignedTestCertificate.KEYSTORE_PASSWORD,
                        "unknown-alias")));

        assertTrue(exception.getMessage().contains("unknown-alias"));
    }

    @Test
    void shouldFailWhenAliasIsNotAPrivateKeyEntry() throws Exception {
        Path certOnlyKeystore = certOnlyKeystore();

        SigningConfigurationException exception = assertThrows(SigningConfigurationException.class,
                () -> SigningCredentialsLoader.load(properties(
                        certOnlyKeystore.toString(), "storepass", "cert-only")));

        assertTrue(exception.getMessage().contains("not a private-key entry"));
    }

    @Test
    void shouldFailWhenCertificateIsExpired() throws Exception {
        X509Certificate expired = mock(X509Certificate.class);
        doThrow(new CertificateExpiredException()).when(expired).checkValidity();

        SigningConfigurationException exception = assertThrows(SigningConfigurationException.class,
                () -> SigningCredentialsLoader.validateCertificate(expired, "alias"));

        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void shouldFailWhenCertificateIsNotYetValid() throws Exception {
        X509Certificate notYetValid = mock(X509Certificate.class);
        doThrow(new CertificateNotYetValidException()).when(notYetValid).checkValidity();

        SigningConfigurationException exception = assertThrows(SigningConfigurationException.class,
                () -> SigningCredentialsLoader.validateCertificate(notYetValid, "alias"));

        assertTrue(exception.getMessage().contains("not yet valid"));
    }

    private static NfeSigningProperties properties(String path, String password, String alias) {
        return new NfeSigningProperties(true, path, password, alias);
    }

    /**
     * Builds a temporary PKCS#12 that contains only a certificate entry
     * (no private key) for the alias "cert-only".
     */
    private static Path certOnlyKeystore() throws Exception {
        KeyStore source = KeyStore.getInstance("PKCS12");
        try (InputStream in = SelfSignedTestCertificate.class.getResourceAsStream("/testcert/nfe-test.p12")) {
            source.load(in, SelfSignedTestCertificate.KEYSTORE_PASSWORD.toCharArray());
        }
        Certificate certificate = source.getCertificate(SelfSignedTestCertificate.KEY_ALIAS);

        KeyStore target = KeyStore.getInstance("PKCS12");
        target.load(null, null);
        target.setCertificateEntry("cert-only", certificate);

        Path file = Files.createTempFile("cert-only", ".p12");
        file.toFile().deleteOnExit();
        try (OutputStream out = Files.newOutputStream(file)) {
            target.store(out, "storepass".toCharArray());
        }
        return file;
    }
}
