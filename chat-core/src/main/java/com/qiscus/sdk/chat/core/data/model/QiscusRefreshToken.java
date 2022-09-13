package com.qiscus.sdk.chat.core.data.model;

public class QiscusRefreshToken {

    private String token = null;
    private String refreshToken = null;


    public void setToken(String token) {
        this.token = token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token != null ? token : "";
    }

    public String getRefreshToken() {
        return refreshToken != null ? refreshToken : "";
    }

    @Override
    public String toString() {
        return "QiscusRefreshToken{" +
                "token='" + token + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
