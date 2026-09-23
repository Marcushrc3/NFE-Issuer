package com.example.nfe.infrastructure.sefaz.config;

import com.example.nfe.application.sefaz.SefazTransmitter;
import com.example.nfe.infrastructure.sefaz.nfe.NfeSefazTransmitter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the SEFAZ authorization adapter only when transmission is
 * explicitly enabled ({@code nfe.sefaz.enabled=true}).
 * <p>
 * With the default configuration the application exposes no
 * {@link SefazTransmitter} bean, so {@code EMIT} fails with the controlled
 * {@code SefazNotConfiguredException} instead of attempting any network call.
 */
@Configuration
@EnableConfigurationProperties(NfeSefazProperties.class)
public class NfeSefazConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "nfe.sefaz", name = "enabled", havingValue = "true")
    public SefazTransmitter sefazTransmitter(NfeSefazProperties properties) {
        return new NfeSefazTransmitter(properties);
    }
}
