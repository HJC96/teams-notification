package io.github.hjc96.teams.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.Objects;

/**
 * 하나의 {@link OkHttpClient} / {@link ObjectMapper}를 공유하는 {@link WebhookHttpClient}를
 * 생성하는 팩토리.
 *
 * <p>OkHttp는 단일 인스턴스를 공유해 커넥션 풀과 스레드 풀을 재사용하도록 설계되어 있으므로,
 * 채널마다 클라이언트를 새로 만들면 커넥션 풀과 스레드가 낭비됩니다.
 * 이 팩토리 인스턴스 하나로 여러 채널의 클라이언트를 만들면 모두 같은 자원을 공유합니다.
 *
 * <pre>{@code
 * WebhookHttpClientFactory factory = new WebhookHttpClientFactory(Duration.ofSeconds(5));
 * WebhookHttpClient defaultClient = factory.create("https://.../default");
 * WebhookHttpClient monitoring   = factory.create("https://.../monitoring");
 * // defaultClient, monitoring 모두 같은 OkHttpClient를 공유한다.
 * }</pre>
 */
public final class WebhookHttpClientFactory {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * @param timeout HTTP 요청 타임아웃 (connect / read / write 동일하게 적용)
     */
    public WebhookHttpClientFactory(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 공유 OkHttpClient / ObjectMapper를 사용하는 {@link WebhookHttpClient}를 생성합니다.
     *
     * @param webhookUrl Teams Incoming Webhook URL
     */
    public WebhookHttpClient create(String webhookUrl) {
        return new WebhookHttpClient(webhookUrl, httpClient, objectMapper);
    }
}
