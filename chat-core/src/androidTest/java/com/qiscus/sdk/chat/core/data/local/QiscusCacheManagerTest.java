package com.qiscus.sdk.chat.core.data.local;

import static org.junit.Assert.*;

import androidx.core.util.Pair;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusCommentDraft;
import com.qiscus.sdk.chat.core.data.model.QiscusPushNotificationMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

public class QiscusCacheManagerTest extends InstrumentationBaseTest {

    private static QiscusCacheManager manager;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    @Override
    public void setupEngine() {
        super.setupEngine();
        manager = QiscusCacheManager.getInstance();
    }

    @Test
    public void imagePathTest() {
        String path = "path";
        manager.cacheLastImagePath(path);
        String result = manager.getLastImagePath();
        assertEquals(path, result);
    }

    @Test
    public void messageNotifItemTest() {
        long roomId = 101L;
        QiscusPushNotificationMessage message = new QiscusPushNotificationMessage(
                creteMessage(100L, "roomName", "msg")
        );
        manager.clearMessageNotifItems(roomId);

        assertFalse(
                manager.updateMessageNotifItem(message, roomId)
        );
        assertFalse(
                manager.removeMessageNotifItem(message, roomId)
        );

        assertTrue(
                manager.addMessageNotifItem(message, roomId)
        );
        assertFalse(
                manager.addMessageNotifItem(message, roomId)
        );

        QiscusPushNotificationMessage newMessage = new QiscusPushNotificationMessage(
                creteMessage(102L, "roomNameA", "msg 123")
        );
        assertTrue(
                manager.addMessageNotifItem(newMessage, roomId)
        );

        QiscusPushNotificationMessage updateMessage = new QiscusPushNotificationMessage(
                creteMessage(103L, "roomNameB", "this msg is different")
        );
        assertFalse(
                manager.updateMessageNotifItem(updateMessage, roomId)
        );

        newMessage.setCommentId(100L);
        newMessage.setRoomName("roomName");
        newMessage.setRoomName("this msg is different");

        assertTrue(
                manager.updateMessageNotifItem(newMessage, roomId)
        );
        assertFalse(
                manager.updateMessageNotifItem(newMessage, roomId)
        );

        QiscusPushNotificationMessage emptyMessage = new QiscusPushNotificationMessage(
                creteMessage(1045L, "", "msg 123")
        );
        assertTrue(
                manager.addMessageNotifItem(emptyMessage, roomId)
        );
        emptyMessage.setMessage("message banget");
        assertTrue(
                manager.updateMessageNotifItem(emptyMessage, roomId)
        );
        emptyMessage.setMessage("message banget ok");
        assertTrue(
                manager.updateMessageNotifItem(emptyMessage, roomId)
        );

        emptyMessage.setCommentId(100L);
        emptyMessage.setMessage("message banget ya");
        assertTrue(
                manager.updateMessageNotifItem(emptyMessage, roomId)
        );

        assertTrue(
                manager.removeMessageNotifItem(message, roomId)
        );

        manager.clearMessageNotifItems(roomId);
    }

    private QiscusComment creteMessage(long id, String roomName, String msg) {
        QiscusComment message = new QiscusComment();
        message.setId(id);
        message.setRoomName(roomName);
        message.setMessage(msg);
        message.setRoomAvatar("avatar");
        return message;
    }

    @Test
    public void setLastChatActivityTest() {
        long roomId = 101L;
        manager.setLastChatActivity(true, roomId);

        Pair<Boolean, Long> pair = manager.getLastChatActivity();
        assertEquals(Boolean.TRUE, pair.first);
        long resultRoomId = pair.second;
        assertEquals(roomId, resultRoomId);
    }

    @Test
    public void setDraftCommentTest() {
        long roomId = 101L;
        QiscusCommentDraft draft = new QiscusCommentDraft("{ \"message\' : \"msg\" }");
        assertNull(
                manager.getDraftComment(roomId)
        );
        manager.setDraftComment(roomId, draft);
        assertNotNull(
                manager.getDraftComment(roomId)
        );

        QiscusCommentDraft newDraft = new QiscusCommentDraft("{ \"repliedPayload\" : \"payload\", "
                + " \"message\" : \"msg\" }");
        manager.setDraftComment(roomId, newDraft);
        assertNotNull(
                manager.getDraftComment(roomId)
        );

        manager.clearDraftComment(roomId);
    }

    @Test
    public void clearDataTest() {
        manager.clearData();
    }
}