package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class QUserPresenceTest {

    QUserPresence qUserPresence;
    @Before
    public void setUp() throws Exception {
        qUserPresence = new QUserPresence();
    }

    @Test
    public void getUserId() {
        qUserPresence.getUserId();
    }

    @Test
    public void setUserId() {
        qUserPresence.setUserId("1");
    }

    @Test
    public void getStatus() {
        qUserPresence.getStatus();
    }

    @Test
    public void setStatus() {
        qUserPresence.setStatus(true);
    }

    @Test
    public void getTimestamp() {
        qUserPresence.getTimestamp();
    }

    @Test
    public void setTimestamp() {
        qUserPresence.setTimestamp(new Date());
    }

    @Test
    public void testEquals() {
    }

    @Test
    public void testHashCode() {
        setUserId();
        setStatus();
        setTimestamp();

        qUserPresence.hashCode();
    }

    @Test
    public void testHashCode2() {
        qUserPresence.setUserId(null);
        qUserPresence.setStatus(null);
        qUserPresence.setTimestamp(null);

        qUserPresence.hashCode();
    }

    @Test
    public void testToString() {
        setUserId();
        setStatus();
        setTimestamp();

        qUserPresence.toString();

    }
}