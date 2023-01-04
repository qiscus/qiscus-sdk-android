package com.qiscus.sdk.chat.core.service;

import static org.junit.Assert.*;

import android.content.Intent;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.remote.QiscusResendCommentHelper;
import com.qiscus.sdk.chat.core.event.QiscusUserEvent;
import com.qiscus.sdk.chat.core.service.QiscusSyncService;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusSyncServiceTest extends InstrumentationBaseTest {
    QiscusSyncService qiscusSyncService = null;
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

        qiscusSyncService = new QiscusSyncService();

        application.getApplicationContext()
                .startService(new Intent(application.getApplicationContext(), QiscusSyncService.class));
    }

    @Test
    public void onBind() {
        qiscusSyncService.onBind(new Intent());
    }

    @Test
    public void onUserEvent() {
        qiscusSyncService.onUserEvent(QiscusUserEvent.LOGIN);
        qiscusSyncService.onUserEvent(QiscusUserEvent.LOGOUT);
    }

    @Test
    public void onDestroy() {
        try {
            qiscusSyncService.onDestroy();
        }catch (NullPointerException e){

        }

    }

    @Test
    public void sync() {
        qiscusSyncService = new QiscusSyncService();
        qiscusSyncService.syncComments();
    }

    @Test
    public void syncEvent() {
        qiscusSyncService = new QiscusSyncService();
        qiscusSyncService.syncEvents();
    }

    @Test
    public void stopSync() {
        qiscusSyncService = new QiscusSyncService();
        qiscusSyncService.stopSync();
    }
}