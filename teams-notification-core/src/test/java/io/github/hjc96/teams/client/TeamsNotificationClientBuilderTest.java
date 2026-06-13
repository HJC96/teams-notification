package io.github.hjc96.teams.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.hjc96.teams.message.MessageType;
import io.github.hjc96.teams.message.TeamsMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamsNotificationClientBuilderTest {

    private WireMockServer wireMock;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(0);
        wireMock.start();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void builder로_생성한_클라이언트가_기본_webhook으로_메시지를_전송한다() {
        wireMock.stubFor(post(urlEqualTo("/webhook"))
                .willReturn(aResponse().withStatus(200)));

        TeamsNotificationClient client = TeamsNotificationClient.builder()
                .webhookUrl(wireMock.baseUrl() + "/webhook")
                .timeout(Duration.ofSeconds(5))
                .retryMaxAttempts(1)
                .build();

        client.send(TeamsMessage.text()
                .title("테스트")
                .body("테스트 메시지입니다.")
                .type(MessageType.INFO)
                .build());

        wireMock.verify(postRequestedFor(urlEqualTo("/webhook")));
    }

    @Test
    void webhookUrl이_없으면_빌드할_수_없다() {
        assertThatThrownBy(() -> TeamsNotificationClient.builder().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("webhookUrl");
    }
}
