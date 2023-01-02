package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import org.json.JSONObject;
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
    public void equals(){
        setUserId();
        setStatus();
        setTimestamp();
        qUserPresence.equals(qUserPresence);

        qUserPresence.equals(new JSONObject());

        qUserPresence.equals(new QUserPresence());


        QUserPresence userPresence = new QUserPresence();
        userPresence.setUserId("11");
        userPresence.setStatus(false);
        userPresence.setTimestamp(new Date(10000));

        QUserPresence userPresence2 = new QUserPresence();
        userPresence2.setUserId("112");
        userPresence2.setStatus(true);
        userPresence2.setTimestamp(new Date());

        userPresence2.equals(userPresence);
    }

    @Test
    public void equals2(){

        QUserPresence userPresence = new QUserPresence();

        QUserPresence userPresence2 = new QUserPresence();
        userPresence2.setUserId("112");
        userPresence2.setStatus(true);
        userPresence2.setTimestamp(new Date());

        userPresence.equals(userPresence2);
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