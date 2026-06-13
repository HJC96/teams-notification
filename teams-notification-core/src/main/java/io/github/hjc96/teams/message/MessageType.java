package io.github.hjc96.teams.message;

public enum MessageType {
    SUCCESS("#28a745"),
    FAIL("#dc3545"),
    WARNING("#ffc107"),
    INFO("#17a2b8");

    private final String colorCode;

    MessageType(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorCode() {
        return colorCode;
    }
}