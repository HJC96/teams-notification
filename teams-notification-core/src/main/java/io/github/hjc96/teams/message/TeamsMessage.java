package io.github.hjc96.teams.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TeamsMessage {

    private final String title;
    private final String body;
    private final MessageType type;
    private final boolean adaptiveCard;
    private final List<Fact> facts;
    private final List<ActionButton> actionButtons;

    private TeamsMessage(Builder builder) {
        this.title = Objects.requireNonNull(builder.title, "title must not be null");
        this.body = builder.body;
        this.type = builder.type != null ? builder.type : MessageType.INFO;
        this.adaptiveCard = builder.adaptiveCard;
        this.facts = Collections.unmodifiableList(builder.facts);
        this.actionButtons = Collections.unmodifiableList(builder.actionButtons);
    }

    public static Builder text()         { return new Builder(false); }
    public static Builder adaptiveCard() { return new Builder(true); }

    public String getTitle()                     { return title; }
    public String getBody()                      { return body; }
    public MessageType getType()                 { return type; }
    public boolean isAdaptiveCard()              { return adaptiveCard; }
    public List<Fact> getFacts()                 { return facts; }
    public List<ActionButton> getActionButtons() { return actionButtons; }

    /**
     * Adaptive Card의 FactSet에 표시되는 키-값 쌍.
     *
     * Teams 메시지에서 아래처럼 렌더링됨:
     *   환경     prod
     *   버전     v1.2.3
     *
     * 단순 데이터 홀더라 record 사용 (getter, equals, toString 자동 생성)
     */
    public record Fact(String key, String value) {}

    /**
     * Adaptive Card 하단에 표시되는 버튼.
     * 클릭 시 지정한 url로 이동 (Action.OpenUrl 타입)
     *
     * Teams 메시지에서 아래처럼 렌더링됨:
     *   [로그 보기]  [대시보드]
     */
    public record ActionButton(String title, String url) {}

    public static class Builder {
        private String title;
        private String body;
        private MessageType type;
        private final boolean adaptiveCard;
        private final List<Fact> facts = new ArrayList<>();
        private final List<ActionButton> actionButtons = new ArrayList<>();

        private Builder(boolean adaptiveCard) {
            this.adaptiveCard = adaptiveCard;
        }

        public Builder title(String title)     { this.title = title; return this; }
        public Builder body(String body)       { this.body = body; return this; }
        public Builder type(MessageType type)  { this.type = type; return this; }

        /**
         * Adaptive Card FactSet에 키-값 항목 추가.
         * 여러 번 호출하면 순서대로 쌓임.
         *
         * 예: .fact("환경", "prod").fact("버전", "v1.2.3")
         */
        public Builder fact(String key, String value) {
            this.facts.add(new Fact(key, value));
            return this;
        }

        /**
         * Adaptive Card 하단 버튼 추가 (Action.OpenUrl).
         * 여러 번 호출하면 버튼이 나란히 표시됨.
         *
         * 예: .actionButton("로그 보기", "https://grafana.example.com")
         */
        public Builder actionButton(String title, String url) {
            this.actionButtons.add(new ActionButton(title, url));
            return this;
        }

        public TeamsMessage build() { return new TeamsMessage(this); }
    }
}