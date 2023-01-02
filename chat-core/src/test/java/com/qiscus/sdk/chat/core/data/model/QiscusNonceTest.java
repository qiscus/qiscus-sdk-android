package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class QiscusNonceTest extends TestCase {

    QiscusNonce qiscusNonce;
    @Before
    public void setUp() throws Exception {
        super.setUp();
        qiscusNonce = new QiscusNonce(new Date(), "data nonce");

        qiscusNonce.setExpiredAt(new Date());
        qiscusNonce.setNonce("nonce");


        qiscusNonce.getNonce();
        qiscusNonce.getExpiredAt();
    }


    @Test
    public void testToString() {
        qiscusNonce.toString();
    }
}