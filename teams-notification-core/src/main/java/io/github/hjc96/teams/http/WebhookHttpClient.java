package io.github.hjc96.teams.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hjc96.teams.card.AdaptiveCardBuilder;
import io.github.hjc96.teams.exception.TeamsNotificationException;
import io.github.hjc96.teams.message.TeamsMessage;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OkHttp를 사용해 MS Teams Incoming Webhook으로 HTTP POST 요청을 보내는 클라이언트.
 *
 * <p>Spring 의존성 없이 순수 Java로 동작합니다.
 * 메시지 타입에 따라 텍스트 JSON 또는 Adaptive Card JSON을 생성해 전송합니다.
 *
 * <p>OkHttp를 선택한 이유:
 * <ul>
 *   <li>Spring 의존성 없이 core 모듈에서 독립적으로 사용 가능</li>
 *   <li>RestTemplate은 Spring 의존성 필요 + deprecated 방향</li>
 *   <li>WebClient는 Spring WebFlux 의존성 필요 + Reactive 강제</li>
 * </ul>
 */
public class WebhookHttpClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AdaptiveCardBuilder adaptiveCardBuilder;
    private final String webhookUrl;

    /**
     * @param webhookUrl Teams Incoming Webhook URL
     * @param timeout    HTTP 요청 타임아웃 (connect / read / write 동일하게 적용)
     */
    public WebhookHttpClient(String webhookUrl, Duration timeout) {
        this.webhookUrl = Objects.requireNonNull(webhookUrl, "webhookUrl must not be null");
        Objects.requireNonNull(timeout, "timeout must not be null");
        this.objectMapper = new ObjectMapper();
        this.adaptiveCardBuilder = new AdaptiveCardBuilder(objectMapper);
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .build();
    }

    /**
     * Teams Webhook으로 메시지를 전송합니다.
     * {@link TeamsMessage#isAdaptiveCard()}가 true면 Adaptive Card JSON,
     * false면 단순 텍스트 JSON으로 직렬화합니다.
     *
     * @param message 전송할 메시지
     * @throws TeamsNotificationException HTTP 요청 실패 또는 비성공 응답 수신 시
     */
    public void post(TeamsMessage message) {
        try {
            String json = message.isAdaptiveCard()
                    ? adaptiveCardBuilder.build(message)
                    : buildTextJson(message);

            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(webhookUrl)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new TeamsNotificationException(
                            "Webhook 전송 실패. HTTP 상태코드: " + response.code()
                    );
                }
            }
        } catch (IOException e) {
            throw new TeamsNotificationException("Webhook 전송 중 오류 발생", e);
        }
    }

    private String buildTextJson(TeamsMessage message) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", "**" + message.getTitle() + "**\n\n" + message.getBody());
        return objectMapper.writeValueAsString(payload);
    }
}