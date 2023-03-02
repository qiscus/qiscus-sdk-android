package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import android.content.Context;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class QiscusCoreChatConfigTest extends InstrumentationBaseTest {
    QiscusCoreChatConfig qiscusCoreChatConfig = null;
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

        qiscusCoreChatConfig = new QiscusCoreChatConfig();
    }

    @Test
    public void setCommentSendingInterceptor() {
        qiscusCoreChatConfig.setCommentSendingInterceptor(new QiscusCommentSendingInterceptor() {
            @Override
            public QiscusComment sendComment(QiscusComment qiscusComment) {
                return null;
            }
        });
    }

    @Test
    public void getQiscusImageCompressionConfig() {
        qiscusCoreChatConfig.getQiscusImageCompressionConfig();
        qiscusCoreChatConfig.getQiscusImageCompressionConfig().getMaxHeight();
        qiscusCoreChatConfig.getQiscusImageCompressionConfig().setMaxHeight(123);
        qiscusCoreChatConfig.getQiscusImageCompressionConfig().getMaxWidth();
        qiscusCoreChatConfig.getQiscusImageCompressionConfig().setMaxWidth(123);
        qiscusCoreChatConfig.getQiscusImageCompressionConfig().getQuality();
        qiscusCoreChatConfig.getQiscusImageCompressionConfig().setQuality(100);

    }

    @Test
    public void setQiscusImageCompressionConfig() {
        qiscusCoreChatConfig.setQiscusImageCompressionConfig(new QiscusImageCompressionConfig(900.0f,1440.0f,80));
    }

    @Test
    public void setNotificationListener() {
        qiscusCoreChatConfig.setNotificationListener(new NotificationListener() {
            @Override
            public void onHandlePushNotification(Context context, QiscusComment qiscusComment) {

            }
        });
    }

    @Test
    public void getDeleteCommentListener() {
        qiscusCoreChatConfig.getDeleteCommentListener();
    }

    @Test
    public void setDeleteCommentListener() {
        qiscusCoreChatConfig.setDeleteCommentListener(new DeleteCommentListener() {
            @Override
            public void onHandleDeletedCommentNotification(Context context, List<QiscusComment> comments, boolean hardDelete) {

            }
        });
    }

    @Test
    public void setEnableLog() {
        qiscusCoreChatConfig.setEnableLog(true);
    }

    @Test
    public void enableDebugMode() {
        qiscusCoreChatConfig.enableDebugMode(true);
    }
}