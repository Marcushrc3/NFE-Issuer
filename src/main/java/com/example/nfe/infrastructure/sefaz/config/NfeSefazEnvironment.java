package com.example.nfe.infrastructure.sefaz.config;

/**
 * SEFAZ operational environment for the configured authorization endpoint.
 * <p>
 * Maps to the official NF-e {@code tpAmb} concept: {@code HOMOLOGATION}
 * corresponds to tpAmb 2 and {@code PRODUCTION} to tpAmb 1. The value is
 * configuration metadata only — {@code tpAmb} itself travels inside the
 * signed NF-e and is never rewritten by this layer.
 */
public enum NfeSefazEnvironment {

    HOMOLOGATION,

    PRODUCTION
}
