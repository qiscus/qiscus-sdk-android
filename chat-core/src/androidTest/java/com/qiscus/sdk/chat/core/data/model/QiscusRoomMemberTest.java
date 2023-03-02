package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import com.qiscus.manggil.mention.Mentionable;
import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusRoomMemberTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
    }

    @Test
    public void roomMemberTest(){

        QiscusRoomMember roomMember = new QiscusRoomMember();
        roomMember.setEmail("email");
        roomMember.setUsername("username");


        QiscusRoomMember roomMember2 = new QiscusRoomMember();
        roomMember2.setEmail("email");
        roomMember2.setUsername("username");

        QiscusRoomMember roomMember3 = new QiscusRoomMember();
        roomMember3.setEmail("email2");
        roomMember3.setUsername("username2");

        QiscusRoomMember roomMember4 = new QiscusRoomMember();
        roomMember4.setEmail("Email2");
        roomMember4.setUsername("Username2");

        roomMember.hashCode();
        roomMember.equals(roomMember2);
        roomMember.equals(roomMember3);
        roomMember.equals(roomMember4);

        roomMember.describeContents();

        roomMember.getTextForDisplayMode(Mentionable.MentionDisplayMode.FULL);
        roomMember.getTextForEncodeMode();
        roomMember.getSuggestibleId();
        roomMember.getSuggestiblePrimaryText();
        roomMember.getDeleteStyle();

    }
}