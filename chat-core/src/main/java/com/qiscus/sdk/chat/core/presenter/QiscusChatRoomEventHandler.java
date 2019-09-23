package com.qiscus.sdk.chat.core.presenter;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
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

    private QiscusAccount qiscusAccount;
    private StateListener listener;
    private QiscusChatRoom qiscusChatRoom;
    private Runnable listenChatRoomTask;
    private HashMap<String, QiscusRoomMember> memberState;

    public QiscusChatRoomEventHandler(QiscusChatRoom qiscusChatRoom, StateListener listener) {
        this.listener = listener;
        this.qiscusAccount = QiscusCore.getQiscusAccount();
        setChatRoom(qiscusChatRoom);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        this.listenChatRoomTask = this::listenChatRoomEvent;

        QiscusAndroidUtil.runOnUIThread(listenChatRoomTask, TimeUnit.SECONDS.toMillis(1));
    }

    public void setChatRoom(QiscusChatRoom qiscusChatRoom) {
        this.qiscusChatRoom = qiscusChatRoom;
        setMemberState();
    }

    private void setMemberState() {
        if (memberState == null) {
            memberState = new HashMap<>();
        } else {
            memberState.clear();
        }

        if (qiscusChatRoom.getMember().isEmpty()) {
            return;
        }

        for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
            memberState.put(member.getEmail(), member);
        }
    }

    private void listenChatRoomEvent() {
        QiscusPusherApi.getInstance().subscribeChatRoom(qiscusChatRoom);
    }

    public void detach() {
        QiscusAndroidUtil.cancelRunOnUIThread(listenChatRoomTask);
        QiscusPusherApi.getInstance().unsubsribeChatRoom(qiscusChatRoom);
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
                    QiscusCore.getDataStore().updateLastDeliveredComment(qiscusChatRoom.getId(), lastDeliveredCommentId);
                    listener.onChangeLastDelivered(lastDeliveredCommentId);
                    break;
                case READ:
                    long lastReadCommentId = event.getCommentId();
                    QiscusCore.getDataStore().updateLastReadComment(qiscusChatRoom.getId(), lastReadCommentId);
                    listener.onChangeLastRead(lastReadCommentId);
                    break;
            }
        }
    }

    public void onGotComment(QiscusComment qiscusComment) {
        if (!qiscusComment.getSender().equals(qiscusAccount.getEmail())) {
            // handle room event such s invite user, kick user
            if (qiscusComment.getType() == QiscusComment.Type.SYSTEM_EVENT) {
                handleChatRoomChanged(qiscusComment);
            }
        }
    }

    private void handleChatRoomChanged(QiscusComment qiscusComment) {
        try {
            JSONObject payload = QiscusRawDataExtractor.getPayload(qiscusComment);
            QiscusRoomMember member = new QiscusRoomMember();
            switch (payload.optString("type")) {
                case "add_member":
                    member.setEmail(payload.optString("object_email"));
                    member.setUsername(payload.optString("object_username"));
                    handleMemberAdded(member);
                    break;
                case "join_room":
                    member.setEmail(payload.optString("subject_email"));
                    member.setUsername(payload.optString("subject_username"));
                    handleMemberAdded(member);
                    break;
                case "remove_member":
                    member.setEmail(payload.optString("object_email"));
                    member.setUsername(payload.optString("object_username"));
                    handleMemberRemoved(member);
                    break;
                case "left_room":
                    member.setEmail(payload.optString("subject_email"));
                    member.setUsername(payload.optString("subject_username"));
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

    private void handleMemberAdded(QiscusRoomMember member) {
        if (!memberState.containsKey(member.getEmail())) {
            memberState.put(member.getEmail(), member);

            listener.onChatRoomMemberAdded(member);
            QiscusAndroidUtil.runOnBackgroundThread(() ->
                    QiscusCore.getDataStore().addOrUpdateRoomMember(qiscusChatRoom.getId(), member, qiscusChatRoom.getDistinctId()));
        }
    }

    private void handleMemberRemoved(QiscusRoomMember member) {
        if (memberState.remove(member.getEmail()) != null) {

            listener.onChatRoomMemberRemoved(member);
            QiscusAndroidUtil.runOnBackgroundThread(() ->
                    QiscusCore.getDataStore().deleteRoomMember(qiscusChatRoom.getId(), member.getEmail()));
        }
    }

    public interface StateListener {
        void onChatRoomNameChanged(String name);

        void onChatRoomMemberAdded(QiscusRoomMember member);

        void onChatRoomMemberRemoved(QiscusRoomMember member);

        void onUserTypng(String email, boolean typing);

        void onChangeLastDelivered(long lastDeliveredCommentId);

        void onChangeLastRead(long lastReadCommentId);
    }
}
