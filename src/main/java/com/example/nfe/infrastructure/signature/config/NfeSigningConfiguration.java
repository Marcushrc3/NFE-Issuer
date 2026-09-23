package com.example.nfe.infrastructure.signature.config;

import com.example.nfe.infrastructure.signature.XmlDsigSigner;
import com.example.nfe.infrastructure.signature.XmlSigner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the real XMLDSig signer only when signing is explicitly enabled
 * ({@code nfe.signing.enabled=true}).
 * <p>
 * With the default configuration no {@link XmlSigner} bean exists: XML_ONLY
 * works without any certificate handling and EMIT fails with the controlled
 * {@code SigningNotConfiguredException}. When enabled, the PKCS#12 keystore
 * is loaded at startup through {@link SigningCredentialsLoader} — an invalid
 * configuration fails fast with a sanitized
 * {@link SigningConfigurationException} instead of silently degrading.
 */
@Configuration
@EnableConfigurationProperties(NfeSigningProperties.class)
public class NfeSigningConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "nfe.signing", name = "enabled", havingValue = "true")
    public XmlSigner xmlSigner(NfeSigningProperties properties) {
        return new XmlDsigSigner(SigningCredentialsLoader.load(properties));
    }
}
