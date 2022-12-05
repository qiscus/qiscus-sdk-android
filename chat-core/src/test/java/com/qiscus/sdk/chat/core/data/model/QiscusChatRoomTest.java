package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class QiscusChatRoomTest {

    QiscusChatRoom qiscusChatRoom;

    @Before
    public void setUp() throws Exception {
        qiscusChatRoom = new QiscusChatRoom();
    }

    @Test
    public void getId() {
        qiscusChatRoom.getId();
    }

    @Test
    public void setId() {
        qiscusChatRoom.setId(1);
    }

    @Test
    public void getDistinctId() {
        qiscusChatRoom.getDistinctId();
    }

    @Test
    public void setDistinctId() {
        qiscusChatRoom.setDistinctId("123");
    }

    @Test
    public void getUniqueId() {
        qiscusChatRoom.getUniqueId();
    }

    @Test
    public void setUniqueId() {
        qiscusChatRoom.setUniqueId("1223vcx");
    }

    @Test
    public void getName() {
        qiscusChatRoom.getName();
    }

    @Test
    public void setName() {
        qiscusChatRoom.setName("name chat room");
    }

    @Test
    public void getOptions() {
        qiscusChatRoom.getOptions();
    }

    @Test
    public void setOptions() {
        try {
            qiscusChatRoom.setOptions(new JSONObject("{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isGroup() {
        qiscusChatRoom.isGroup();
    }

    @Test
    public void setGroup() {
        qiscusChatRoom.setGroup(true);
    }

    @Test
    public void isChannel() {
        qiscusChatRoom.isChannel();
    }

    @Test
    public void setChannel() {
        qiscusChatRoom.setChannel(false);
    }

    @Test
    public void getAvatarUrl() {
        qiscusChatRoom.getAvatarUrl();
    }

    @Test
    public void setAvatarUrl() {
        qiscusChatRoom.setAvatarUrl("https://");
    }

    @Test
    public void getMember() {
        qiscusChatRoom.getMember();
    }

    @Test
    public void setMember() {
        qiscusChatRoom.setMember(null);
    }

    @Test
    public void getUnreadCount() {
        qiscusChatRoom.getUnreadCount();
    }

    @Test
    public void setUnreadCount() {
        qiscusChatRoom.setUnreadCount(1);
    }

    @Test
    public void getLastComment() {
        qiscusChatRoom.getLastComment();
    }

    @Test
    public void setLastComment() {
        QiscusComment qiscusComment;
        qiscusComment = new QiscusComment();
        qiscusComment.setRoomId(1);
        qiscusChatRoom.setLastComment(qiscusComment);
    }

    @Test
    public void getMemberCount() {
        qiscusChatRoom.getMemberCount();
    }

    @Test
    public void setMemberCount() {
        qiscusChatRoom.setMemberCount(1);
    }

    @Test
    public void testHashCode() {
        setId();
        setDistinctId();
        setUniqueId();
        setName();
        setOptions();
        setGroup();
        setChannel();
        setAvatarUrl();
        setMember();
        setUnreadCount();
        setLastComment();
        setMemberCount();

        qiscusChatRoom.hashCode();
    }

    @Test
    public void testEquals() {
    }

    @Test
    public void describeContents() {
        setId();
        setDistinctId();
        setUniqueId();
        setName();
        setOptions();
        setGroup();
        setChannel();
        setAvatarUrl();
        setMember();
        setUnreadCount();
        setLastComment();
        setMemberCount();

        qiscusChatRoom.describeContents();
    }

    @Test
    public void writeToParcel() {
    }

    @Test
    public void testToString() {
        setId();
        setDistinctId();
        setUniqueId();
        setName();
        setOptions();
        setGroup();
        setChannel();
        setAvatarUrl();
        setMember();
        setUnreadCount();
        setLastComment();
        setMemberCount();

        qiscusChatRoom.toString();
    }

    @Test
    public void testData() {
        QiscusChatRoom.RoomType roomType = QiscusChatRoom.RoomType.CHANNEL;
        String type = "all";
        if (roomType != null) {
            if (roomType == QiscusChatRoom.RoomType.SINGLE) {
                type = "single";
            } else if (roomType == QiscusChatRoom.RoomType.GROUP) {
                type = "group";
            } else if (roomType == QiscusChatRoom.RoomType.CHANNEL)  {
                type = "public_channel";
            }
        }
    }
}