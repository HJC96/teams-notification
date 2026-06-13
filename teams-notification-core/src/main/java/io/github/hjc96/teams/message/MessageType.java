package io.github.hjc96.teams.message;

public enum MessageType {
    SUCCESS("#28a745", "Good"),
    FAIL("#dc3545", "Attention"),
    WARNING("#ffc107", "Warning"),
    INFO("#17a2b8", "Accent");

    private final String colorCode;
    private final String adaptiveCardColor;

    MessageType(String colorCode, String adaptiveCardColor) {
        this.colorCode = colorCode;
        this.adaptiveCardColor = adaptiveCardColor;
    }

    public String getColorCode() { return colorCode; }
    public String getAdaptiveCardColor() { return adaptiveCardColor; }
}