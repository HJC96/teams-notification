package io.github.hjc96.teams.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * application.yml 바인딩 설정.
 *
 * 사용 예시:
 * teams:
 *   notification:
 *     enabled: true
 *     channels:
 *       default:
 *         webhook-url: https://...
 *       monitoring:
 *         webhook-url: https://...
 *     retry:
 *       max-attempts: 3
 *       wait-duration: 1000ms
 *       back-off: FIXED
 *     timeout: 5s
 */
@ConfigurationProperties(prefix = "teams.notification")
public class TeamsNotificationProperties {

    /** 라이브러리 활성화 여부 (기본값: true) */
    private boolean enabled = true;

    /** 채널 이름 → 채널 설정 맵. "default" 채널은 반드시 존재해야 함 */
    private Map<String, ChannelProperties> channels = new HashMap<>();

    private RetryProperties retry = new RetryProperties();

    /** HTTP 요청 타임아웃 (기본값: 5초) */
    private Duration timeout = Duration.ofSeconds(5);

    public boolean isEnabled()                          { return enabled; }
    public void setEnabled(boolean enabled)             { this.enabled = enabled; }
    public Map<String, ChannelProperties> getChannels() { return channels; }
    public void setChannels(Map<String, ChannelProperties> channels) { this.channels = channels; }
    public RetryProperties getRetry()                   { return retry; }
    public void setRetry(RetryProperties retry)         { this.retry = retry; }
    public Duration getTimeout()                        { return timeout; }
    public void setTimeout(Duration timeout)            { this.timeout = timeout; }

    /** 채널별 설정 */
    public static class ChannelProperties {
        /** Teams Incoming Webhook URL */
        private String webhookUrl;

        public String getWebhookUrl()               { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    }

    /** Retry 설정 */
    public static class RetryProperties {

        /** 최대 재시도 횟수 (기본값: 3) */
        private int maxAttempts = 3;

        /** 재시도 간격 (기본값: 1초) */
        private Duration waitDuration = Duration.ofMillis(1000);

        /**
         * 재시도 간격 방식
         * FIXED: 매번 동일한 간격 (1s, 1s, 1s)
         * EXPONENTIAL: 간격이 점점 늘어남 (1s, 2s, 4s)
         */
        private BackOffType backOff = BackOffType.FIXED;

        public int getMaxAttempts()                  { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts)  { this.maxAttempts = maxAttempts; }
        public Duration getWaitDuration()            { return waitDuration; }
        public void setWaitDuration(Duration waitDuration) { this.waitDuration = waitDuration; }
        public BackOffType getBackOff()              { return backOff; }
        public void setBackOff(BackOffType backOff)  { this.backOff = backOff; }
    }

    public enum BackOffType {
        FIXED, EXPONENTIAL
    }
}