package com.example.nfe.infrastructure.sefaz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuration properties for the SEFAZ authorization adapter
 * (prefix {@code nfe.sefaz}).
 * <p>
 * Defaults target the official SEFAZ-SP (UF 35) homologation WebService
 * {@code NFeAutorizacao4}. No credentials, certificates or private keys are
 * configured here — the A1 certificate is consumed exclusively by the XML
 * signing step. Transmission stays disabled until {@code enabled} is set.
 *
 * @param uf             UF whose authorization WebService is used (SP only for now)
 * @param environment    homologation or production
 * @param endpoint       the official authorization WebService URL
 * @param connectTimeout TCP connect timeout
 * @param readTimeout    response read timeout
 */
@ConfigurationProperties(prefix = "nfe.sefaz")
public record NfeSefazProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("SP") String uf,
        @DefaultValue("HOMOLOGATION") NfeSefazEnvironment environment,
        @DefaultValue("https://homologacao.nfe.fazenda.sp.gov.br/ws/nfeautorizacao4.asmx")
        String endpoint,
        @DefaultValue("10s") Duration connectTimeout,
        @DefaultValue("60s") Duration readTimeout) {
}
