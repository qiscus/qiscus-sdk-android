package com.qiscus.sdk.chat.core.presenter;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusRawDataExtractor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jan, Fri 04 2019 14.34
 * <p>
 * ChatRoom Event Handler Class
 **/
public class QiscusChatRoomEventHandler {

    private QiscusCore qiscusCore;
    private QAccount qiscusAccount;
    private StateListener listener;
    private QChatRoom qiscusChatRoom;
    private Runnable listenChatRoomTask;
    private HashMap<String, QParticipant> memberState;

    public QiscusChatRoomEventHandler(QiscusCore qiscusCore, QChatRoom qiscusChatRoom, StateListener listener) {
        this.qiscusCore = qiscusCore;
        this.listener = listener;
        this.qiscusAccount = qiscusCore.getQiscusAccount();
        setChatRoom(qiscusChatRoom);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        this.listenChatRoomTask = this::listenChatRoomEvent;

        QiscusAndroidUtil.runOnUIThread(listenChatRoomTask, TimeUnit.SECONDS.toMillis(1));
    }

    public void setChatRoom(QChatRoom qiscusChatRoom) {
        this.qiscusChatRoom = qiscusChatRoom;
        setMemberState();
    }

    private void setMemberState() {
        if (memberState == null) {
            memberState = new HashMap<>();
        } else {
            memberState.clear();
        }

        if (qiscusChatRoom.getParticipants().isEmpty()) {
            return;
        }

        for (QParticipant participant : qiscusChatRoom.getParticipants()) {
            memberState.put(participant.getId(), participant);
        }
    }

    private void listenChatRoomEvent() {
        qiscusCore.getPusherApi().subscribeChatRoom(qiscusChatRoom);
    }

    public void detach() {
        QiscusAndroidUtil.cancelRunOnUIThread(listenChatRoomTask);
        qiscusCore.getPusherApi().unsubsribeChatRoom(qiscusChatRoom);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onChatRoomEvent(QiscusChatRoomEvent event) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleEvent(event));
    }

    private void handleEvent(QiscusChatRoomEvent event) {
        if (event.getRoomId() == qiscusChatRoom.getId()) {
            switch (event.getEvent()) {
                case TYPING:
                    listener.onUserTypng(event.getUser(), event.isTyping());
                    break;
                case DELIVERED:
                    long lastDeliveredCommentId = event.getCommentId();
                    qiscusCore.getDataStore().updateLastDeliveredComment(qiscusChatRoom.getId(), lastDeliveredCommentId);
                    listener.onChangeLastDelivered(lastDeliveredCommentId);
                    break;
                case READ:
                    long lastReadCommentId = event.getCommentId();
                    qiscusCore.getDataStore().updateLastReadComment(qiscusChatRoom.getId(), lastReadCommentId);
                    listener.onChangeLastRead(lastReadCommentId);
                    break;
            }
        }
    }

    public void onGotComment(QMessage qMessage) {
        if (!qMessage.getSender().equals(qiscusAccount.getId())) {
            // handle room event such s invite user, kick user
            if (qMessage.getType() == QMessage.Type.SYSTEM_EVENT) {
                handleChatRoomChanged(qMessage);
            }

            if (qiscusChatRoom.getId() == qMessage.getChatRoomId()) {
                QChatRoom room = qiscusCore.getDataStore().getChatRoom(qiscusChatRoom.getId());
                //room.setLastMessage(qMessage);
                //qiscusCore.getDataStore().addOrUpdate(room);
                if ( room.getLastMessage().getStatus() == QMessage.STATE_SENT ||
                        room.getLastMessage().getStatus() == QMessage.STATE_DELIVERED ) {
                    qiscusCore.getDataStore().updateLastReadComment(qiscusChatRoom.getId(), qMessage.getId());
                    listener.onChangeLastRead(qMessage.getId());
                }
            }
        }
    }

    private void handleChatRoomChanged(QMessage qMessage) {
        try {
            JSONObject payload = QiscusRawDataExtractor.getPayload(qMessage);
            QParticipant participant = new QParticipant();
            switch (payload.optString("type")) {
                case "add_member":
                    participant.setId(payload.optString("object_email"));
                    participant.setName(payload.optString("object_username"));
                    handleMemberAdded(participant);
                    break;
                case "join_room":
                    participant.setId(payload.optString("subject_email"));
                    participant.setName(payload.optString("subject_username"));
                    handleMemberAdded(participant);
                    break;
                case "remove_member":
                    participant.setId(payload.optString("object_email"));
                    participant.setName(payload.optString("object_username"));
                    handleMemberRemoved(participant);
                    break;
                case "left_room":
                    participant.setId(payload.optString("subject_email"));
                    participant.setName(payload.optString("subject_username"));
                    handleMemberRemoved(participant);
                    break;
                case "change_room_name":
                    listener.onChatRoomNameChanged(payload.optString("room_name"));
                    break;
            }
        } catch (JSONException e) {
            //Do nothing
        }
    }

    private void handleMemberAdded(QParticipant participant) {
        if (!memberState.containsKey(participant.getId())) {
            memberState.put(participant.getId(), participant);

            listener.onChatRoomMemberAdded(participant);
            QiscusAndroidUtil.runOnBackgroundThread(() ->
                    qiscusCore.getDataStore().addOrUpdateRoomMember(qiscusChatRoom.getId(), participant));
        }
    }

    private void handleMemberRemoved(QParticipant participant) {
        if (memberState.remove(participant.getId()) != null) {

            listener.onChatRoomMemberRemoved(participant);
            QiscusAndroidUtil.runOnBackgroundThread(() ->
                    qiscusCore.getDataStore().deleteRoomMember(qiscusChatRoom.getId(), participant.getId()));
        }
    }

    public interface StateListener {
        void onChatRoomNameChanged(String name);

        void onChatRoomMemberAdded(QParticipant participant);

        void onChatRoomMemberRemoved(QParticipant participant);

        void onUserTypng(String email, boolean typing);

        void onChangeLastDelivered(long lastDeliveredCommentId);

        void onChangeLastRead(long lastReadCommentId);
    }
}
