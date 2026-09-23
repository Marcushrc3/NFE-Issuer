package com.example.nfe.infrastructure.signature.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for NF-e XML signing (prefix {@code nfe.signing}).
 * <p>
 * Signing is opt-in. When disabled, XML_ONLY works normally and EMIT fails
 * with the controlled {@code SigningNotConfiguredException}. When enabled,
 * the PKCS#12 keystore is loaded at startup from the configured path.
 * <p>
 * Secrets (the keystore password) are intended to be supplied through
 * environment variables at runtime — never committed, never logged, never
 * exposed through the REST API.
 */
@ConfigurationProperties(prefix = "nfe.signing")
public record NfeSigningProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("") String keystorePath,
        @DefaultValue("") String keystorePassword,
        @DefaultValue("") String alias) {
}
