package com.qiscus.sdk.chat.core.presenter;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
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

    private QAccount qAccount;
    private StateListener listener;
    private QChatRoom qChatRoom;
    private Runnable listenChatRoomTask;
    private HashMap<String, QParticipant> memberState;

    public QiscusChatRoomEventHandler(QChatRoom qChatRoom, StateListener listener) {
        this.listener = listener;
        this.qAccount = QiscusCore.getQiscusAccount();
        setChatRoom(qChatRoom);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        this.listenChatRoomTask = this::listenChatRoomEvent;

        QiscusAndroidUtil.runOnUIThread(listenChatRoomTask, TimeUnit.SECONDS.toMillis(1));
    }

    public void setChatRoom(QChatRoom qChatRoom) {
        this.qChatRoom = qChatRoom;
        setMemberState();
    }

    private void setMemberState() {
        if (memberState == null) {
            memberState = new HashMap<>();
        } else {
            memberState.clear();
        }

        if (qChatRoom.getParticipants().isEmpty()) {
            return;
        }

        for (QParticipant member : qChatRoom.getParticipants()) {
            memberState.put(member.getId(), member);
        }
    }

    private void listenChatRoomEvent() {
        QiscusPusherApi.getInstance().subscribeChatRoom(qChatRoom);
    }

    public void detach() {
        QiscusAndroidUtil.cancelRunOnUIThread(listenChatRoomTask);
        QiscusPusherApi.getInstance().unsubsribeChatRoom(qChatRoom);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onChatRoomEvent(QiscusChatRoomEvent event) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleEvent(event));
    }

    private void handleEvent(QiscusChatRoomEvent event) {
        if (event.getRoomId() == qChatRoom.getId()) {
            switch (event.getEvent()) {
                case TYPING:
                    listener.onUserTypng(event.getUser(), event.isTyping());
                    break;
                case DELIVERED:
                    long lastDeliveredCommentId = event.getCommentId();
                    QiscusCore.getDataStore().updateLastDeliveredComment(qChatRoom.getId(), lastDeliveredCommentId);
                    listener.onChangeLastDelivered(lastDeliveredCommentId);
                    break;
                case READ:
                    long lastReadCommentId = event.getCommentId();
                    QiscusCore.getDataStore().updateLastReadComment(qChatRoom.getId(), lastReadCommentId);
                    listener.onChangeLastRead(lastReadCommentId);
                    break;
            }
        }
    }

    public void onGotComment(QiscusComment qiscusComment) {
        if (!qiscusComment.getSender().equals(qAccount.getId())) {
            // handle room event such s invite user, kick user
            if (qiscusComment.getType() == QiscusComment.Type.SYSTEM_EVENT) {
                handleChatRoomChanged(qiscusComment);
            }
        }
    }

    private void handleChatRoomChanged(QiscusComment qiscusComment) {
        try {
            JSONObject payload = QiscusRawDataExtractor.getPayload(qiscusComment);
            QParticipant member = new QParticipant();
            switch (payload.optString("type")) {
                case "add_member":
                    member.setId(payload.optString("object_email"));
                    member.setName(payload.optString("object_username"));
                    handleMemberAdded(member);
                    break;
                case "join_room":
                    member.setId(payload.optString("subject_email"));
                    member.setName(payload.optString("subject_username"));
                    handleMemberAdded(member);
                    break;
                case "remove_member":
                    member.setId(payload.optString("object_email"));
                    member.setName(payload.optString("object_username"));
                    handleMemberRemoved(member);
                    break;
                case "left_room":
                    member.setId(payload.optString("subject_email"));
                    member.setName(payload.optString("subject_username"));
                    handleMemberRemoved(member);
                    break;
                case "change_room_name":
                    listener.onChatRoomNameChanged(payload.optString("room_name"));
                    break;
            }
        } catch (JSONException e) {
            //Do nothing
        }
    }

    private void handleMemberAdded(QParticipant member) {
        if (!memberState.containsKey(member.getId())) {
            memberState.put(member.getId(), member);

            listener.onChatRoomMemberAdded(member);
            QiscusAndroidUtil.runOnBackgroundThread(() ->
                    QiscusCore.getDataStore().addOrUpdateRoomMember(qChatRoom.getId(), member, qChatRoom.getDistinctId()));
        }
    }

    private void handleMemberRemoved(QParticipant member) {
        if (memberState.remove(member.getId()) != null) {

            listener.onChatRoomMemberRemoved(member);
            QiscusAndroidUtil.runOnBackgroundThread(() ->
                    QiscusCore.getDataStore().deleteRoomMember(qChatRoom.getId(), member.getId()));
        }
    }

    public interface StateListener {
        void onChatRoomNameChanged(String name);

        void onChatRoomMemberAdded(QParticipant member);

        void onChatRoomMemberRemoved(QParticipant member);

        void onUserTypng(String email, boolean typing);

        void onChangeLastDelivered(long lastDeliveredCommentId);

        void onChangeLastRead(long lastReadCommentId);
    }
}
