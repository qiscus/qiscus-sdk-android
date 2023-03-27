package com.qiscus.sdk.chat.core.data.remote;

import static org.junit.Assert.*;

import android.content.res.AssetManager;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    @Test
    public void resendFileTest() {
        QiscusResendCommentHelper helper = new QiscusResendCommentHelper();

        AssetManager am = context.getAssets();
        try {
            InputStream inputStream = am.open("sample.pdf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String fileName = "name file";
        File f = getFileFromAsset(fileName);

        QiscusComment qiscusComment = QiscusComment.generateFileAttachmentMessage(
                roomId, f.getAbsolutePath(), "caption", fileName + ".pdf"
        );

        try {
            Method resendFile = extractMethode(helper, "resendFile", QiscusComment.class);
            resendFile.invoke(helper, qiscusComment);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }

    private File getFileFromAsset(String fileName) {
        try{
            AssetManager am = context.getAssets();
            InputStream inputStream = am.open("sample.pdf");

            File f = new File(context.getCacheDir()+"/"+ fileName +".pdf");

            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }catch (IOException e) {
            //Logging exception
        }

        return null;
    }
}