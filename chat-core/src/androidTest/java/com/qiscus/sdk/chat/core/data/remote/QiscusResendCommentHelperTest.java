package com.qiscus.sdk.chat.core.data.remote;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class QiscusResendCommentHelperTest extends InstrumentationBaseTest {
    Integer roomId = 10185397;
    String roomUniqId = "8d412fdd3411f5f261f8f30e0f90ff60";
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

        QiscusResendCommentHelper qiscusResendCommentHelper = new QiscusResendCommentHelper();
    }

    @Test
    public void tryResendPendingComment() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId, (String) "testing");
        qiscusComment.setState(0);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        QiscusResendCommentHelper.tryResendPendingComment();

        QiscusResendCommentHelper.resendComment(qiscusComment);
    }

    @Test
    public void tryResendPendingComment2() {
        QiscusComment qiscusComment = QiscusComment.generateFileAttachmentMessage(roomId,"https://www.simplilearn.com/ice9/free_resources_article_thumb/what_is_image_Processing.jpg","test","oke");
        qiscusComment.setState(0);
        qiscusComment.setRawType("file_attachment");
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        QiscusResendCommentHelper.tryResendPendingComment();
        QiscusResendCommentHelper.resendComment(qiscusComment);
        qiscusComment.getCaption();

    }

    @Test
    public void tryResendPendingComment3() {
        File compressedFile = new File("/storage/emulated/0/Pictures/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.png");

        QiscusComment qiscusComment2 = QiscusComment.generateFileAttachmentMessage(roomId,
                compressedFile.getPath(), "caption2", "name file2");

        qiscusComment2.setState(0);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment2);
        QiscusResendCommentHelper.tryResendPendingComment();

    }

    @Test
    public void tryResendPendingComment4() {
        File compressedFile = new File("/storage/emulated/0/Pictures/Twitter/20221112_083258.jpg");

        QiscusComment qiscusComment2 = QiscusComment.generateFileAttachmentMessage(roomId,
                compressedFile.getPath(), "caption2", "name file2");
        qiscusComment2.setRawType("file_attachment");

        qiscusComment2.setState(0);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment2);
        QiscusResendCommentHelper.tryResendPendingComment();

    }

    @Test
    public void cancelPendingComment() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId, (String) "testing");
        qiscusComment.setState(0);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        QiscusResendCommentHelper.cancelPendingComment(qiscusComment);
    }

    @Test
    public void cancelAll() {
        QiscusResendCommentHelper.cancelAll();
    }

    @Test
    public void mustFailed() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId, (String) "testing");
        qiscusComment.setState(0);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        QiscusResendCommentHelper.mustFailed(new Throwable("error"),qiscusComment);
    }

    @Test
    public void commentFail() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId, (String) "testing");
        qiscusComment.setState(0);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        QiscusResendCommentHelper.commentFail(new Throwable("error"),qiscusComment);
    }
}