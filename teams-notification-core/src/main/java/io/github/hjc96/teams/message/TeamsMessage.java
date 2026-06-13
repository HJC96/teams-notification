package io.github.hjc96.teams.message;

import java.util.Objects;

public class TeamsMessage {

    private final String title;
    private final String body;
    private final MessageType type;
    private final boolean adaptiveCard;

    private TeamsMessage(Builder builder) {
        this.title = Objects.requireNonNull(builder.title, "title must not be null");
        this.body = builder.body;
        this.type = builder.type != null ? builder.type : MessageType.INFO;
        this.adaptiveCard = builder.adaptiveCard;
    }

    public static Builder text() {
        return new Builder(false);
    }

    public static Builder adaptiveCard() {
        return new Builder(true);
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public MessageType getType() {
        return type;
    }

    public boolean isAdaptiveCard() {
        return adaptiveCard;
    }

    public static class Builder {
        private String title;
        private String body;
        private MessageType type;
        private final boolean adaptiveCard;

        private Builder(boolean adaptiveCard) {
            this.adaptiveCard = adaptiveCard;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder type(MessageType type) {
            this.type = type;
            return this;
        }

        public TeamsMessage build() {
            return new TeamsMessage(this);
        }
    }
}