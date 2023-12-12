package com.qiscus.sdk.chat.core.mediator;

import com.qiscus.sdk.chat.core.QiscusActivityCallback;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.local.QiscusEventCache;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.data.remote.QiscusClearCommentsHandler;
import com.qiscus.sdk.chat.core.data.remote.QiscusDeleteCommentHandler;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.data.remote.QiscusResendCommentHelper;
import com.qiscus.sdk.chat.core.service.QiscusSyncTimer;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusConst;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusFirebaseMessagingUtil;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

public class QiscusMediator implements IQiscusMediator {

    private QiscusApi api;
    private QiscusLogger logger;
    private QiscusErrorLogger errorLogger;
    private QiscusPusherApi pusherApi;
    private QiscusAndroidUtil androidUtil;
    private QiscusActivityCallback activityCallback;
    private QiscusResendCommentHelper resendCommentHelper;
    private QiscusEventCache eventCache;
    private QiscusSyncTimer syncTimer;
    private QiscusDeleteCommentHandler deleteCommentHandler;
    private QiscusFirebaseMessagingUtil firebaseMessagingUtil;
    private QiscusCacheManager cacheManager;
    private QiscusClearCommentsHandler clearCommentsHandler;

    public QiscusMediator() {
    }

    @Override
    public void initAllClass(QiscusCore qiscusCore) {
        logger = new QiscusLogger(qiscusCore);
        errorLogger = new QiscusErrorLogger(qiscusCore);
        api = new QiscusApi(qiscusCore);
        pusherApi = new QiscusPusherApi(qiscusCore);
        androidUtil = new QiscusAndroidUtil(qiscusCore);
        activityCallback = new QiscusActivityCallback(qiscusCore);
        resendCommentHelper = new QiscusResendCommentHelper(qiscusCore);
        eventCache = new QiscusEventCache(qiscusCore);
        syncTimer = new QiscusSyncTimer(qiscusCore);
        deleteCommentHandler = new QiscusDeleteCommentHandler(qiscusCore);
        firebaseMessagingUtil = new QiscusFirebaseMessagingUtil(qiscusCore);
        cacheManager = new QiscusCacheManager(qiscusCore);
        clearCommentsHandler = new QiscusClearCommentsHandler(qiscusCore);

        QiscusConst.setApps(qiscusCore.getApps());
        QiscusConst.setAppsName(qiscusCore.getApps().getApplicationInfo().
                loadLabel(qiscusCore.getApps().getPackageManager()).toString());
    }

    public QiscusApi getApi() {
        return api;
    }

    public QiscusLogger getLogger() {
        return logger;
    }

    public QiscusErrorLogger getErrorLogger() {
        return errorLogger;
    }

    public QiscusPusherApi getPusherApi() {
        return pusherApi;
    }

    public QiscusAndroidUtil getAndroidUtil() {
        return androidUtil;
    }

    public QiscusActivityCallback getActivityCallback() {
        return activityCallback;
    }

    public QiscusResendCommentHelper getResendCommentHelper() {
        return resendCommentHelper;
    }

    public QiscusEventCache getEventCache() {
        return eventCache;
    }

    public QiscusSyncTimer getSyncTimer() {
        return syncTimer;
    }

    public QiscusDeleteCommentHandler getDeleteCommentHandler() {
        return deleteCommentHandler;
    }

    public QiscusFirebaseMessagingUtil getFirebaseMessagingUtil() {
        return firebaseMessagingUtil;
    }

    public QiscusCacheManager getCacheManager() {
        return cacheManager;
    }

    public QiscusClearCommentsHandler getClearCommentsHandler() {
        return clearCommentsHandler;
    }
}
