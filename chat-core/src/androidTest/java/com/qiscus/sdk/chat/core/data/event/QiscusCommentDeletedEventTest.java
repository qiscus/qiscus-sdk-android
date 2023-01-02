package com.qiscus.sdk.chat.core.data.event;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.event.QiscusCommentDeletedEvent;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusCommentDeletedEventTest extends InstrumentationBaseTest {

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
    }

    @Test
    public void check(){
        QiscusComment qiscusComment;
        qiscusComment = new QiscusComment();
        qiscusComment.setRoomId(1);
        qiscusComment.setDeleted(false);
        qiscusComment.setMessage("test");
        QiscusCommentDeletedEvent qiscusCommentDeletedEvent = new QiscusCommentDeletedEvent(qiscusComment);
        qiscusCommentDeletedEvent.getQiscusComment();
        qiscusCommentDeletedEvent.isHardDelete();
        new QiscusCommentDeletedEvent(qiscusComment, true);
    }
}