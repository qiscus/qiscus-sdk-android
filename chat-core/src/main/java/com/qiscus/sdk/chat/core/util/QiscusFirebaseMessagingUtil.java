package com.qiscus.sdk.chat.core.util;

import com.google.firebase.messaging.RemoteMessage;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QMessage;

import org.json.JSONObject;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Aug, Tue 14 2018 15.33
 **/
public class QiscusFirebaseMessagingUtil {

    private QiscusCore qiscusCore;

    public QiscusFirebaseMessagingUtil(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;
    }

    /**
     * Handle remote message from FCM to display push notification
     *
     * @param remoteMessage The message from firebase
     * @return true if the message is for Qiscus SDK, false if the message is not for Qiscus SDK
     */
    public boolean handleMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().containsKey("qiscus_sdk")) {
            if (qiscusCore.hasSetupUser()) {
                if (!qiscusCore.getPusherApi().isConnected()) {
                    qiscusCore.getPusherApi().restartConnection();
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

    private void handlePostCommentEvent(RemoteMessage remoteMessage) {
        QMessage qMessage = qiscusCore.getPusherApi().jsonToComment(remoteMessage.getData().get("payload"));
        if (qMessage == null) {
            return;
        }

        qiscusCore.getPusherApi().handleReceivedComment(qMessage);
    }

    private void handleDeleteCommentsEvent(RemoteMessage remoteMessage) {
        try {
            qiscusCore.getPusherApi().handleNotification(new JSONObject(remoteMessage.getData().get("payload")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClearComments(RemoteMessage remoteMessage) {
        try {
            qiscusCore.getPusherApi().handleNotification(new JSONObject(remoteMessage.getData().get("payload")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
