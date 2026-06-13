package io.github.hjc96.teams.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hjc96.teams.exception.TeamsNotificationException;
import io.github.hjc96.teams.message.TeamsMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MS Teams Adaptive Card JSON을 생성하는 빌더.
 *
 * Teams Webhook의 Adaptive Card 전송 포맷:
 * {
 *   "type": "message",
 *   "attachments": [{
 *     "contentType": "application/vnd.microsoft.card.adaptive",
 *     "content": {
 *       "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
 *       "type": "AdaptiveCard",
 *       "version": "1.4",
 *       "body": [ ...블록들... ],
 *       "actions": [ ...버튼들... ]
 *     }
 *   }]
 * }
 *
 * @see <a href="https://adaptivecards.io/explorer/">Adaptive Cards 스펙</a>
 */
public class AdaptiveCardBuilder {

    private final ObjectMapper objectMapper;

    public AdaptiveCardBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String build(TeamsMessage message) {
        try {
            return objectMapper.writeValueAsString(toPayload(message));
        } catch (IOException e) {
            throw new TeamsNotificationException("Adaptive Card JSON 생성 실패", e);
        }
    }

    private Map<String, Object> toPayload(TeamsMessage message) {
        List<Object> body = new ArrayList<>();

        // TextBlock: 제목 표시
        // color는 hex 코드가 아닌 Adaptive Card 전용 키워드 사용
        // (Good=초록, Attention=빨강, Warning=노랑, Accent=파랑)
        body.add(Map.of(
                "type", "TextBlock",
                "text", message.getTitle(),
                "weight", "Bolder",
                "size", "Medium",
                "color", message.getType().getAdaptiveCardColor()
        ));

        // FactSet: 키-값 목록 블록 (facts가 있을 때만 추가)
        // 렌더링 예시:
        //   환경     prod
        //   버전     v1.2.3
        if (!message.getFacts().isEmpty()) {
            List<Map<String, String>> facts = message.getFacts().stream()
                    .map(f -> Map.of("title", f.key(), "value", f.value()))
                    .toList();
            body.add(Map.of("type", "FactSet", "facts", facts));
        }

        // TextBlock: 본문 (body가 있을 때만 추가)
        // wrap=true: 긴 텍스트 자동 줄바꿈
        if (message.getBody() != null && !message.getBody().isBlank()) {
            body.add(Map.of(
                    "type", "TextBlock",
                    "text", message.getBody(),
                    "wrap", true
            ));
        }

        // Actions: 하단 버튼 목록
        // Action.OpenUrl: 클릭 시 지정 URL로 이동
        List<Map<String, String>> actions = message.getActionButtons().stream()
                .map(btn -> Map.of(
                        "type", "Action.OpenUrl",
                        "title", btn.title(),
                        "url", btn.url()
                ))
                .toList();

        // Adaptive Card 본체 조립
        Map<String, Object> card = new HashMap<>();
        card.put("$schema", "http://adaptivecards.io/schemas/adaptive-card.json");
        card.put("type", "AdaptiveCard");
        card.put("version", "1.4");
        card.put("body", body);
        if (!actions.isEmpty()) {
            card.put("actions", actions);
        }

        // Teams Webhook 포맷으로 감싸기
        // 단순 텍스트와 달리 Adaptive Card는 attachments 배열 안에 들어가야 함
        return Map.of(
                "type", "message",
                "attachments", List.of(Map.of(
                        "contentType", "application/vnd.microsoft.card.adaptive",
                        "content", card
                ))
        );
    }
}