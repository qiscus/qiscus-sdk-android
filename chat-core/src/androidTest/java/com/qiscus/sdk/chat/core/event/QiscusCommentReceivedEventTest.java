package com.qiscus.sdk.chat.core.event;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusCommentReceivedEventTest extends InstrumentationBaseTest {
    Integer roomId = 10185397;
    QiscusCommentReceivedEvent qiscusCommentReceivedEvent = null;
    @Before
    public void setUp() throws Exception {
        super.setUp();
        QiscusCore.setup(application, "sdksample");
        QiscusCore.setUser("arief92", "arief92")
                .withUsername("arief92")
                .withAvatarUrl("https://")
                .withExtras(null)
                .save(new QiscusCore.SetUserListener() {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        //on success


                    }
                    @Override
                    public void onError(Throwable throwable) {
                        //on error
                    }});

        QiscusComment qiscusComment = QiscusComment.generateMessage(10185397, "test");

        qiscusCommentReceivedEvent = new QiscusCommentReceivedEvent(qiscusComment);

    }

    @Test
    public void getQiscusComment() {
        qiscusCommentReceivedEvent.getQiscusComment();
    }
}