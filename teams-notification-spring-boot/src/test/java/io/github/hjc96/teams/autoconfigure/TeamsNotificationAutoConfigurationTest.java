package io.github.hjc96.teams.autoconfigure;

import io.github.hjc96.teams.client.TeamsNotificationClient;
import io.github.hjc96.teams.client.TeamsNotificationClientImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = TeamsNotificationAutoConfigurationTest.TestApplication.class,
        properties = {
                "teams.notification.channels.default.webhook-url=https://example.com/default",
                "teams.notification.channels.monitoring.webhook-url=https://example.com/monitoring",
                "teams.notification.retry.max-attempts=2",
                "teams.notification.retry.wait-duration=10ms",
                "teams.notification.retry.back-off=exponential",
                "teams.notification.timeout=1s"
        }
)
class TeamsNotificationAutoConfigurationTest {

    @Autowired
    private TeamsNotificationClient teamsNotificationClient;

    @Autowired
    private TeamsNotificationProperties properties;

    @Test
    void createsTeamsNotificationClientFromConfigurationProperties() {
        assertThat(teamsNotificationClient).isInstanceOf(TeamsNotificationClientImpl.class);

        assertThat(properties.getChannels()).containsKeys("default", "monitoring");
        assertThat(properties.getChannels().get("default").getWebhookUrl())
                .isEqualTo("https://example.com/default");
        assertThat(properties.getChannels().get("monitoring").getWebhookUrl())
                .isEqualTo("https://example.com/monitoring");
        assertThat(properties.getRetry().getMaxAttempts()).isEqualTo(2);
        assertThat(properties.getRetry().getWaitDuration()).isEqualTo(Duration.ofMillis(10));
        assertThat(properties.getRetry().getBackOff())
                .isEqualTo(TeamsNotificationProperties.BackOffType.EXPONENTIAL);
        assertThat(properties.getTimeout()).isEqualTo(Duration.ofSeconds(1));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
