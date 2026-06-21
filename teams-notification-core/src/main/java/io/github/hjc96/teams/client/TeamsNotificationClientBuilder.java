package io.github.hjc96.teams.client;

import io.github.hjc96.teams.http.WebhookHttpClient;
import io.github.hjc96.teams.http.WebhookHttpClientFactory;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TeamsNotificationClientBuilder {

    private String webhookUrl;
    private Duration timeout = Duration.ofSeconds(5);
    private int retryMaxAttempts = 3;
    private Duration retryWaitDuration = Duration.ofSeconds(1);
    private final Map<String, String> channelWebhookUrls = new LinkedHashMap<>();

    TeamsNotificationClientBuilder() {
    }

    public TeamsNotificationClientBuilder webhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        return this;
    }

    public TeamsNotificationClientBuilder timeout(Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout, "timeout must not be null");
        return this;
    }

    public TeamsNotificationClientBuilder retryMaxAttempts(int retryMaxAttempts) {
        if (retryMaxAttempts < 1) {
            throw new IllegalArgumentException("retryMaxAttempts must be greater than or equal to 1");
        }
        this.retryMaxAttempts = retryMaxAttempts;
        return this;
    }

    public TeamsNotificationClientBuilder retryWaitDuration(Duration retryWaitDuration) {
        this.retryWaitDuration = Objects.requireNonNull(retryWaitDuration, "retryWaitDuration must not be null");
        return this;
    }

    public TeamsNotificationClientBuilder channel(String channelName, String webhookUrl) {
        if (channelName == null || channelName.isBlank()) {
            throw new IllegalArgumentException("channelName must not be blank");
        }
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalArgumentException("webhookUrl must not be blank");
        }
        this.channelWebhookUrls.put(channelName, webhookUrl);
        return this;
    }

    public TeamsNotificationClient build() {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalStateException("webhookUrl must be configured");
        }
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalStateException("timeout must be positive");
        }
        if (retryWaitDuration.isZero() || retryWaitDuration.isNegative()) {
            throw new IllegalStateException("retryWaitDuration must be positive");
        }

        // 모든 채널이 하나의 OkHttpClient / ObjectMapper를 공유하도록 팩토리로 생성한다.
        WebhookHttpClientFactory clientFactory = new WebhookHttpClientFactory(timeout);

        WebhookHttpClient defaultClient = clientFactory.create(webhookUrl);
        Map<String, WebhookHttpClient> channelClients = new LinkedHashMap<>();
        channelWebhookUrls.forEach((name, url) ->
                channelClients.put(name, clientFactory.create(url)));

        return new TeamsNotificationClientImpl(defaultClient, channelClients, buildRetry());
    }

    private Retry buildRetry() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(retryMaxAttempts)
                .waitDuration(retryWaitDuration)
                .build();
        return Retry.of("teams-notification", retryConfig);
    }
}
