package com.qiscus.sdk.util;

import com.google.firebase.messaging.RemoteMessage;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;

import org.json.JSONObject;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Aug, Tue 14 2018 15.33
 **/
public final class QiscusFirebaseMessagingUtil {

    private QiscusFirebaseMessagingUtil() {
    }

    /**
     * Handle remote message from FCM to display push notification
     *
     * @param remoteMessage The message from firebase
     * @return true if the message is for Qiscus SDK, false if the message is not for Qiscus SDK
     */
    public static boolean handleMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().containsKey("qiscus_sdk")) {
            if (QiscusCore.hasSetupUser()) {
                if (!QiscusPusherApi.getInstance().isConnected()) {
                    QiscusPusherApi.getInstance().restartConnection();
                }
                if (remoteMessage.getData().containsKey("payload")) {
                    if (remoteMessage.getData().get("qiscus_sdk").equals("post_comment")) {
                        handlePostCommentEvent(remoteMessage);
                    } else if (remoteMessage.getData().get("qiscus_sdk").equals("delete_message")) {
                        handleDeleteCommentsEvent(remoteMessage);
                    } else if (remoteMessage.getData().get("qiscus_sdk").equals("clear_room")) {
                        handleClearComments(remoteMessage);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private static void handlePostCommentEvent(RemoteMessage remoteMessage) {
        QiscusComment qiscusComment = QiscusPusherApi.jsonToComment(remoteMessage.getData().get("payload"));
        if (qiscusComment == null) {
            return;
        }
        QiscusPusherApi.handleReceivedComment(qiscusComment);
    }

    private static void handleDeleteCommentsEvent(RemoteMessage remoteMessage) {
        try {
            QiscusPusherApi.handleNotification(new JSONObject(remoteMessage.getData().get("payload")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClearComments(RemoteMessage remoteMessage) {
        try {
            QiscusPusherApi.handleNotification(new JSONObject(remoteMessage.getData().get("payload")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
