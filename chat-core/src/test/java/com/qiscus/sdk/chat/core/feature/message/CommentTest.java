package com.qiscus.sdk.chat.core.feature.message;

import com.qiscus.sdk.chat.core.data.ActualDataTest;
import com.qiscus.sdk.chat.core.data.ExpectedDataTest;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusApiParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommentTest {

    @Test
    public void getTestComment() {
        QiscusComment qiscusComment = QiscusApiParser.parseQiscusComment(ActualDataTest.jsonCommentForTest(), 10185397L);

        QiscusComment expectedComment = ExpectedDataTest.qiscusCommentForTest();

        assertEquals(expectedComment, qiscusComment);
    }

}
