package io.github.hjc96.teams.client;

import io.github.hjc96.teams.message.TeamsMessage;

import java.util.concurrent.CompletableFuture;

/**
 * MS Teams 채널로 알림을 전송하는 클라이언트 인터페이스.
 *
 * <p>Spring Boot 환경에서는 Auto-Configuration에 의해 자동으로 빈이 등록됩니다.
 *
 * <pre>{@code
 * // 텍스트 메시지 전송
 * teamsClient.send(TeamsMessage.text()
 *     .title("배포 완료")
 *     .body("v1.2.3 배포가 완료되었습니다.")
 *     .type(MessageType.SUCCESS)
 *     .build());
 *
 * // 비동기 전송
 * teamsClient.sendAsync(message)
 *     .thenRun(() -> log.info("전송 완료"))
 *     .exceptionally(e -> { log.error("전송 실패", e); return null; });
 * }</pre>
 */
public interface TeamsNotificationClient {

    /**
     * Spring 없이 사용할 수 있는 클라이언트 빌더를 생성합니다.
     *
     * @return TeamsNotificationClient 빌더
     */
    static TeamsNotificationClientBuilder builder() {
        return new TeamsNotificationClientBuilder();
    }

    /**
     * 기본 채널로 메시지를 동기 전송합니다.
     * 전송이 완료될 때까지 현재 스레드가 블로킹됩니다.
     *
     * @param message 전송할 메시지
     * @throws io.github.hjc96.teams.exception.TeamsNotificationException 전송 실패 시
     */
    void send(TeamsMessage message);

    /**
     * 기본 채널로 메시지를 비동기 전송합니다.
     * 현재 스레드를 블로킹하지 않고 즉시 {@link CompletableFuture}를 반환합니다.
     * 실제 전송은 ForkJoinPool의 별도 스레드에서 실행됩니다.
     *
     * @param message 전송할 메시지
     * @return 전송 완료 시 resolve되는 CompletableFuture
     */
    CompletableFuture<Void> sendAsync(TeamsMessage message);

    /**
     * 지정한 채널로 메시지를 동기 전송합니다.
     * {@code application.yml}의 {@code teams.notification.channels} 하위에
     * 정의된 채널 이름을 사용합니다.
     *
     * @param channelName 채널 이름 (application.yml에 정의된 키)
     * @param message     전송할 메시지
     * @throws IllegalArgumentException 채널이 존재하지 않을 경우
     */
    void sendTo(String channelName, TeamsMessage message);
}
