package io.github.hjc96.teams.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.hjc96.teams.exception.TeamsNotificationException;
import io.github.hjc96.teams.message.MessageType;
import io.github.hjc96.teams.message.TeamsMessage;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class WebhookHttpClientTest {

    private static WireMockServer wireMock;
    private WebhookHttpClient client;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(8089);
        wireMock.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        client = new WebhookHttpClientFactory(Duration.ofSeconds(5))
                .create("http://localhost:8089/webhook");
    }

    @Test
    void 유효한_메시지_전송시_Webhook에_POST_요청을_보낸다() {
        stubFor(post(urlEqualTo("/webhook"))
                .willReturn(aResponse().withStatus(200)));

        TeamsMessage message = TeamsMessage.text()
                .title("테스트")
                .body("테스트 메시지입니다.")
                .type(MessageType.INFO)
                .build();

        assertThatCode(() -> client.post(message))
                .doesNotThrowAnyException();

        verify(postRequestedFor(urlEqualTo("/webhook")));
    }

    @Test
    void 서버_에러_발생시_TeamsNotificationException을_던진다() {
        stubFor(post(urlEqualTo("/webhook"))
                .willReturn(aResponse().withStatus(500)));

        TeamsMessage message = TeamsMessage.text()
                .title("테스트")
                .body("실패 메시지입니다.")
                .type(MessageType.FAIL)
                .build();

        assertThatThrownBy(() -> client.post(message))
                .isInstanceOf(TeamsNotificationException.class)
                .hasMessageContaining("500");
    }

    @Test
    void 실패_응답시_예외_메시지에_응답_본문을_포함한다() {
        stubFor(post(urlEqualTo("/webhook"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Invalid webhook payload")));

        TeamsMessage message = TeamsMessage.text()
                .title("테스트")
                .body("본문 검증")
                .type(MessageType.FAIL)
                .build();

        assertThatThrownBy(() -> client.post(message))
                .isInstanceOf(TeamsNotificationException.class)
                .hasMessageContaining("400")
                .hasMessageContaining("Invalid webhook payload");
    }
}