package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusReplyCommentDraftTest extends InstrumentationBaseTest {
    Integer roomId = 10185397;
    QiscusReplyCommentDraft qiscusReplyCommentDraft = null;
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

        QiscusLocation location = new QiscusLocation();
        location.setName("name");
        location.setAddress("address");
        location.setLatitude(12345);
        location.setLongitude(67890);

        QiscusComment qiscusComment2 = new QiscusComment();
        qiscusComment2.generateLocationMessage(roomId,location).getLocation();
        qiscusComment2.setSender("arief94");
        qiscusComment2.setMessage("location");
        qiscusComment2.setSenderEmail("arief94@gmail.com");
        qiscusComment2.setRawType("location");

        qiscusReplyCommentDraft = new QiscusReplyCommentDraft("rep",qiscusComment2);

    }

    @Test
    public void getRepliedPayload() {
        qiscusReplyCommentDraft.getRepliedPayload();
    }

    @Test
    public void getRepliedComment() {
        qiscusReplyCommentDraft.getRepliedComment();
    }

    @Test
    public void testToString() {
        qiscusReplyCommentDraft.toString();
    }

    @Test
    public void qiscusReplyCommentDraft() {
        new QiscusReplyCommentDraft("rep", "{}");
    }


}