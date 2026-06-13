package io.github.hjc96.teams.client;

import io.github.hjc96.teams.http.WebhookHttpClient;
import io.github.hjc96.teams.message.TeamsMessage;
import io.github.resilience4j.retry.Retry;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class TeamsNotificationClientImpl implements TeamsNotificationClient {

    private final WebhookHttpClient defaultClient;
    private final Map<String, WebhookHttpClient> channelClients;

    /**
     * Retry가 null이면 재시도 없이 단순 전송.
     * AutoConfiguration에서 설정값에 따라 Retry 주입 여부 결정.
     */
    private final Retry retry;

    public TeamsNotificationClientImpl(WebhookHttpClient defaultClient,
                                       Map<String, WebhookHttpClient> channelClients,
                                       Retry retry) {
        this.defaultClient = Objects.requireNonNull(defaultClient);
        this.channelClients = channelClients != null ? channelClients : Map.of();
        this.retry = retry;
    }

    @Override
    public void send(TeamsMessage message) {
        execute(() -> defaultClient.post(message));
    }

    @Override
    public CompletableFuture<Void> sendAsync(TeamsMessage message) {
        return CompletableFuture.runAsync(() -> execute(() -> defaultClient.post(message)));
    }

    @Override
    public void sendTo(String channelName, TeamsMessage message) {
        WebhookHttpClient client = channelClients.get(channelName);
        if (client == null) {
            throw new IllegalArgumentException("채널을 찾을 수 없습니다: " + channelName);
        }
        execute(() -> client.post(message));
    }

    /**
     * Retry 적용 실행 헬퍼.
     * retry가 null이면 그냥 실행, 있으면 Retry로 감싸서 실행.
     */
    private void execute(Runnable action) {
        if (retry == null) {
            action.run();
        } else {
            Retry.decorateRunnable(retry, action).run();
        }
    }
}