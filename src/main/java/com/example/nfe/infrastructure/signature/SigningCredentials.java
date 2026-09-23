package com.example.nfe.infrastructure.signature;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Signing credentials abstraction: the private key and the matching X509
 * certificate used to sign NF-e documents.
 * <p>
 * Credentials are supplied by configuration/tests — never embedded in the
 * application, never part of the domain, never logged. The record is
 * infrastructure-level by design.
 */
public record SigningCredentials(
        PrivateKey privateKey,
        X509Certificate certificate) {

    public SigningCredentials {
        if (privateKey == null || certificate == null) {
            throw new IllegalArgumentException(
                    "signing credentials require a private key and an X509 certificate");
        }
    }
}
