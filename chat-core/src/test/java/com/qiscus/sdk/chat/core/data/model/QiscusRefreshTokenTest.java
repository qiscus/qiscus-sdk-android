package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusRefreshTokenTest extends TestCase {

    QiscusRefreshToken qiscusRefreshToken;
    QiscusRefreshToken qiscusRefreshToken2;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        qiscusRefreshToken = new QiscusRefreshToken();
        qiscusRefreshToken.setToken("asdasdijrn2432asda");
        qiscusRefreshToken.setRefreshToken("24rawklmdaks");
        qiscusRefreshToken.setTokenExpiresAt("2022-10-14T09:27:22Z");

        qiscusRefreshToken.getToken();
        qiscusRefreshToken.getRefreshToken();
        qiscusRefreshToken.getTokenExpiresAt();

        qiscusRefreshToken2 = new QiscusRefreshToken();
        qiscusRefreshToken2.setToken(null);
        qiscusRefreshToken2.setRefreshToken(null);
        qiscusRefreshToken2.setTokenExpiresAt(null);

        qiscusRefreshToken2.getToken();
        qiscusRefreshToken2.getRefreshToken();
        qiscusRefreshToken2.getTokenExpiresAt();

        qiscusRefreshToken.equals(qiscusRefreshToken);

    }

    @Test
    public void testToString() {
        qiscusRefreshToken.toString();
    }
}