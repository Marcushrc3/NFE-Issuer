package com.example.nfe.infrastructure.signature.config;

import com.example.nfe.infrastructure.signature.SelfSignedTestCertificate;
import com.example.nfe.infrastructure.signature.XmlSigner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the Spring wiring of the signing configuration: no bean when
 * disabled, a real {@link XmlSigner} when enabled with a valid TEST-ONLY
 * keystore, and controlled, sanitized startup failures for invalid
 * configuration.
 */
class NfeSigningConfigurationTest {

    private static final String KEYSTORE_PATH =
            Path.of(SelfSignedTestCertificate.KEYSTORE_FILE).toAbsolutePath().toString();

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withUserConfiguration(NfeSigningConfiguration.class);

    @Test
    void shouldNotRegisterXmlSignerWhenSigningIsDisabled() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(XmlSigner.class);
        });
    }

    @Test
    void shouldRegisterXmlSignerWhenSigningIsEnabledWithValidKeystore() {
        runner.withPropertyValues(
                        "nfe.signing.enabled=true",
                        "nfe.signing.keystore-path=" + KEYSTORE_PATH,
                        "nfe.signing.keystore-password=" + SelfSignedTestCertificate.KEYSTORE_PASSWORD,
                        "nfe.signing.alias=" + SelfSignedTestCertificate.KEY_ALIAS)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(XmlSigner.class);
                });
    }

    @Test
    void shouldFailWithControlledErrorWhenKeystorePathIsMissing() {
        runner.withPropertyValues("nfe.signing.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(rootCause(context.getStartupFailure()))
                            .isInstanceOf(SigningConfigurationException.class)
                            .hasMessageContaining("keystore-path");
                });
    }

    @Test
    void shouldFailWithControlledErrorWhenPasswordIsWrongWithoutExposingIt() {
        String wrongPassword = "definitely-wrong-password-123";
        runner.withPropertyValues(
                        "nfe.signing.enabled=true",
                        "nfe.signing.keystore-path=" + KEYSTORE_PATH,
                        "nfe.signing.keystore-password=" + wrongPassword,
                        "nfe.signing.alias=" + SelfSignedTestCertificate.KEY_ALIAS)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(rootCause(context.getStartupFailure()))
                            .isInstanceOf(SigningConfigurationException.class);
                    assertThat(exceptionChainMessages(context.getStartupFailure()))
                            .doesNotContain(wrongPassword);
                });
    }

    @Test
    void shouldFailWithControlledErrorWhenAliasIsUnknown() {
        runner.withPropertyValues(
                        "nfe.signing.enabled=true",
                        "nfe.signing.keystore-path=" + KEYSTORE_PATH,
                        "nfe.signing.keystore-password=" + SelfSignedTestCertificate.KEYSTORE_PASSWORD,
                        "nfe.signing.alias=unknown-alias")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(rootCause(context.getStartupFailure()))
                            .isInstanceOf(SigningConfigurationException.class)
                            .hasMessageContaining("unknown-alias");
                });
    }

    private static Throwable rootCause(Throwable failure) {
        Throwable cause = failure;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private static String exceptionChainMessages(Throwable failure) {
        StringBuilder messages = new StringBuilder();
        Throwable current = failure;
        while (current != null) {
            messages.append(current.getMessage()).append(' ');
            current = current.getCause();
        }
        return messages.toString();
    }
}
