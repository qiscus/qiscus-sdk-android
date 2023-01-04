package com.qiscus.sdk.chat.core.event;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import org.junit.Test;

public class QiscusCommentUpdateEventTest {

    @Test
    public void getQiscusCommentTest() {
        QiscusCommentUpdateEvent event = new QiscusCommentUpdateEvent(new QiscusComment());

        assertNotNull(
                event.getQiscusComment()
        );
    }
}