package io.github.hjc96.teams.autoconfigure;

import io.github.hjc96.teams.client.TeamsNotificationClient;
import io.github.hjc96.teams.client.TeamsNotificationClientImpl;
import io.github.hjc96.teams.http.WebhookHttpClient;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * TeamsNotificationClient 자동 설정.
 *
 * 아래 조건에서 동작:
 * - teams.notification.enabled=true (기본값)
 * - TeamsNotificationClient 빈이 없을 때
 *
 * 사용자가 직접 @Bean으로 TeamsNotificationClient를 등록하면
 * @ConditionalOnMissingBean에 의해 이 AutoConfiguration은 동작하지 않음.
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "teams.notification",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true   // enabled 설정 없어도 기본 동작
)
@EnableConfigurationProperties(TeamsNotificationProperties.class)
public class TeamsNotificationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TeamsNotificationClient teamsNotificationClient(TeamsNotificationProperties properties) {
        // 채널별 WebhookHttpClient 생성
        Map<String, WebhookHttpClient> channelClients = new HashMap<>();
        WebhookHttpClient defaultClient = null;

        for (Map.Entry<String, TeamsNotificationProperties.ChannelProperties> entry
                : properties.getChannels().entrySet()) {

            String channelName = entry.getKey();
            String webhookUrl = entry.getValue().getWebhookUrl();

            WebhookHttpClient client = new WebhookHttpClient(webhookUrl, properties.getTimeout());

            if ("default".equals(channelName)) {
                defaultClient = client;
            } else {
                channelClients.put(channelName, client);
            }
        }

        if (defaultClient == null) {
            throw new IllegalStateException(
                    "teams.notification.channels.default.webhook-url 설정이 필요합니다."
            );
        }

        // Retry 설정
        Retry retry = buildRetry(properties.getRetry());

        return new TeamsNotificationClientImpl(defaultClient, channelClients, retry);
    }

    /**
     * RetryProperties를 Resilience4j RetryConfig으로 변환.
     * EXPONENTIAL: 재시도마다 대기 시간이 2배씩 증가.
     */
    private Retry buildRetry(TeamsNotificationProperties.RetryProperties retryProps) {
        RetryConfig retryConfig;

        if (retryProps.getBackOff() == TeamsNotificationProperties.BackOffType.EXPONENTIAL) {
            retryConfig = RetryConfig.custom()
                    .maxAttempts(retryProps.getMaxAttempts())
                    .intervalFunction(attempt ->
                            retryProps.getWaitDuration().toMillis() * (long) Math.pow(2, attempt - 1))
                    .build();
        } else {
            retryConfig = RetryConfig.custom()
                    .maxAttempts(retryProps.getMaxAttempts())
                    .waitDuration(retryProps.getWaitDuration())
                    .build();
        }

        return Retry.of("teams-notification", retryConfig);
    }
}