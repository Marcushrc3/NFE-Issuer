package com.example.nfe.infrastructure.signature.config;

/**
 * Controlled configuration failure for the signing infrastructure.
 * <p>
 * Messages are deliberately sanitized: they never contain the keystore
 * password, the private key or keystore contents. Paths and alias names are
 * configuration values, not secrets.
 */
public class SigningConfigurationException extends RuntimeException {

    public SigningConfigurationException(String message) {
        super(message);
    }
}
