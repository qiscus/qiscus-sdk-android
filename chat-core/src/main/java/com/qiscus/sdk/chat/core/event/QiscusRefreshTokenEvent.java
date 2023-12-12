package com.qiscus.sdk.chat.core.event;

public class QiscusRefreshTokenEvent {

    // default code
    public static final int UNAUTHORIZED = 401;
    public static final int EXPIRED_TOKEN = 403;
    // default message
    public static String TOKEN_EXPIRED_MESSAGE = "Unauthorized. Token is expired";
    public static String UNAUTHORIZED_MESSAGE = "Unauthorized";

    private final int code;
    private final String message;

    public QiscusRefreshTokenEvent(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isTokenExpired() {
        return code == EXPIRED_TOKEN && message.equals(TOKEN_EXPIRED_MESSAGE);
    }

    public boolean isUnauthorized() {
        return (code == UNAUTHORIZED || code == EXPIRED_TOKEN) && message.equals(UNAUTHORIZED_MESSAGE);
    }
}
