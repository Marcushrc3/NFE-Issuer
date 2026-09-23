package com.example.nfe.infrastructure.signature.config;

import com.example.nfe.infrastructure.signature.SigningCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

/**
 * Loads the externally supplied PKCS#12 signing credentials.
 * <p>
 * Failure behavior is controlled and descriptive, but never exposes the
 * keystore password, the private key or keystore contents. Missing
 * configuration, unreadable files, wrong passwords, unknown aliases,
 * non-private-key aliases and invalid/expired certificates all surface as
 * {@link SigningConfigurationException} with a sanitized message.
 * <p>
 * There is no fallback: when configuration is invalid the application fails
 * to start rather than silently using an arbitrary certificate.
 */
public final class SigningCredentialsLoader {

    private SigningCredentialsLoader() {
    }

    /**
     * Reads the configured PKCS#12 keystore and extracts the private key and
     * X.509 certificate of the configured alias.
     *
     * @throws SigningConfigurationException for any configuration or
     *                                      keystore problem (sanitized message)
     */
    public static SigningCredentials load(NfeSigningProperties properties) {
        String keystorePath = required(properties.keystorePath(), "nfe.signing.keystore-path");
        String alias = required(properties.alias(), "nfe.signing.alias");
        String password = required(properties.keystorePassword(), "nfe.signing.keystore-password");

        byte[] keystoreBytes;
        try {
            keystoreBytes = Files.readAllBytes(Path.of(keystorePath));
        } catch (IOException e) {
            throw new SigningConfigurationException(
                    "signing keystore file cannot be read: " + keystorePath);
        }

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(keystoreBytes), password.toCharArray());
        } catch (Exception e) {
            throw new SigningConfigurationException(
                    "signing keystore could not be opened (wrong password or unsupported format): "
                            + keystorePath);
        }

        boolean isKeyEntry;
        try {
            isKeyEntry = keyStore.isKeyEntry(alias);
        } catch (KeyStoreException e) {
            throw new SigningConfigurationException("keystore entry for alias cannot be inspected: " + alias);
        }
        if (!isKeyEntry) {
            throw new SigningConfigurationException("alias is not a private-key entry: " + alias);
        }

        PrivateKey privateKey;
        try {
            Key key = keyStore.getKey(alias, password.toCharArray());
            if (!(key instanceof PrivateKey)) {
                throw new SigningConfigurationException("alias has no private key: " + alias);
            }
            privateKey = (PrivateKey) key;
        } catch (SigningConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new SigningConfigurationException("private key for alias cannot be recovered: " + alias);
        }

        Certificate[] chain;
        try {
            chain = keyStore.getCertificateChain(alias);
        } catch (KeyStoreException e) {
            throw new SigningConfigurationException("certificate chain for alias cannot be read: " + alias);
        }
        if (chain == null || chain.length == 0 || !(chain[0] instanceof X509Certificate)) {
            throw new SigningConfigurationException("alias has no X.509 certificate: " + alias);
        }
        X509Certificate certificate = (X509Certificate) chain[0];
        validateCertificate(certificate, alias);

        return new SigningCredentials(privateKey, certificate);
    }

    /**
     * Validates the certificate validity window (expiry / not-yet-valid).
     * Package-private so the validation logic can be exercised in isolation.
     */
    static void validateCertificate(X509Certificate certificate, String alias) {
        try {
            certificate.checkValidity();
        } catch (CertificateExpiredException e) {
            throw new SigningConfigurationException("certificate is expired: " + alias);
        } catch (CertificateNotYetValidException e) {
            throw new SigningConfigurationException("certificate is not yet valid: " + alias);
        }
    }

    private static String required(String value, String property) {
        if (value == null || value.isBlank()) {
            throw new SigningConfigurationException(
                    "signing is enabled but " + property + " is not configured");
        }
        return value.trim();
    }
}
