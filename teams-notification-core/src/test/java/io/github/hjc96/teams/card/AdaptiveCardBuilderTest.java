package io.github.hjc96.teams.card;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hjc96.teams.message.MessageType;
import io.github.hjc96.teams.message.TeamsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class AdaptiveCardBuilderTest {

    private AdaptiveCardBuilder builder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        builder = new AdaptiveCardBuilder(objectMapper);
    }

    @Test
    void 제목과_타입으로_AdaptiveCard_JSON을_생성한다() throws Exception {
        // given
        TeamsMessage message = TeamsMessage.adaptiveCard()
                .title("배포 완료")
                .type(MessageType.SUCCESS)
                .build();

        // when
        String json = builder.build(message);
        JsonNode root = objectMapper.readTree(json);

        // then
        then(root.get("type").asText()).isEqualTo("message");
        then(root.get("attachments").get(0)
                .get("contentType").asText())
                .isEqualTo("application/vnd.microsoft.card.adaptive");
    }

    @Test
    void SUCCESS_타입이면_Good_색상을_사용한다() throws Exception {
        // given
        TeamsMessage message = TeamsMessage.adaptiveCard()
                .title("배포 완료")
                .type(MessageType.SUCCESS)
                .build();

        // when
        JsonNode body = objectMapper.readTree(builder.build(message))
                .get("attachments").get(0)
                .get("content")
                .get("body");

        // then
        then(body.get(0).get("color").asText()).isEqualTo("Good");
    }

    @Test
    void Fact와_ActionButton이_포함된_JSON을_생성한다() throws Exception {
        // given
        TeamsMessage message = TeamsMessage.adaptiveCard()
                .title("배포 완료")
                .type(MessageType.SUCCESS)
                .fact("환경", "prod")
                .fact("버전", "v1.2.3")
                .actionButton("로그 보기", "https://grafana.example.com")
                .build();

        // when
        JsonNode content = objectMapper.readTree(builder.build(message))
                .get("attachments").get(0)
                .get("content");

        // then
        JsonNode facts = content.get("body").get(1).get("facts");
        then(facts.get(0).get("title").asText()).isEqualTo("환경");
        then(facts.get(0).get("value").asText()).isEqualTo("prod");

        JsonNode actions = content.get("actions");
        then(actions.get(0).get("title").asText()).isEqualTo("로그 보기");
        then(actions.get(0).get("url").asText()).isEqualTo("https://grafana.example.com");
    }
}