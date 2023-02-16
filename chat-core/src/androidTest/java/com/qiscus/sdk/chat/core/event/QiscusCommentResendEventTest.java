package com.qiscus.sdk.chat.core.event;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusCommentResendEventTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    @Override
    public void setupEngine() {
        super.setupEngine();

        getQiscusComment();
    }


    public void getQiscusComment() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(123,"test");


        QiscusCommentResendEvent event ;
        event = new QiscusCommentResendEvent(qiscusComment);
        event.getQiscusComment();
    }
}