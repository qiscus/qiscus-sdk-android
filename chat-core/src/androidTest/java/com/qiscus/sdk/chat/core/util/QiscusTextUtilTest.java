package com.qiscus.sdk.chat.core.util;

import androidx.core.content.ContextCompat;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.R;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4ClassRunner.class)
public class QiscusTextUtilTest extends InstrumentationBaseTest {

    Map<String, QiscusRoomMember> roomMembers = new HashMap();
    QiscusRoomMember member = creteMember();
    int color = 0;

    private QiscusRoomMember creteMember() {
        QiscusRoomMember member = new QiscusRoomMember();
        member.setEmail("mail@mail.com");
        member.setExtras(new JSONObject());
        member.setAvatar("avatar");
        member.setUsername("uName");
        return member;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
        color = ContextCompat.getColor(context, R.color.emoji_background);
        roomMembers.clear();
        roomMembers.put(member.getEmail(), member);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }


    @Test
    public void getStringTest() {
        QiscusTextUtil.getString(R.string.qiscus_date_and_time, "ok", 0);
    }

    @Test
    public void isBlankTest() {
        QiscusTextUtil.isBlank(null);
        QiscusTextUtil.isBlank("");
        QiscusTextUtil.isBlank("no");
        QiscusTextUtil.isBlank("no no no");
        QiscusTextUtil.isBlank("okskipajalah");
    }

    @Test
    public void isNotBlankTest() {
        QiscusTextUtil.isNotBlank("");
        QiscusTextUtil.isNotBlank("no");
    }

    @Test
    public void isUrlTest() {
        QiscusTextUtil.isUrl("no");
    }

    @Test
    public void extractUrlTest() {
        QiscusTextUtil.extractUrl("");
        QiscusTextUtil.extractUrl("okokok=https://www.qiscus.com");
        QiscusTextUtil.extractUrl("okokok=www.qiscus.com");
        QiscusTextUtil.extractUrl("@okokok=www.qiscus.com@");
        QiscusTextUtil.extractUrl("@okokok=www.qiscus.com");
        QiscusTextUtil.extractUrl("okokok=www.qiscus.com@");
    }

    @Test
    public void createQiscusSpannableTextNullTest() {
        QiscusTextUtil.createQiscusSpannableText(
                null,
                roomMembers, color, color, color,
                member1 -> {
                    // ignored
                }
        );
    }

    @Test
    public void createQiscusSpannableTextTest() {
        QiscusTextUtil.createQiscusSpannableText(
                "message",
                roomMembers, color, color, color,
                member1 -> {
                    // ignored
                }
        );
    }

    @Test
    public void createQiscusSpannableTextSpecialCaseTest() {
        Map<String, QiscusRoomMember> roomMembers = new HashMap();
        roomMembers.put("email", null);

        int color = ContextCompat.getColor(context, R.color.emoji_background);

        QiscusTextUtil.createQiscusSpannableText(
                "@message",
                roomMembers, color, color, color,
                member1 -> {
                    // ignored
                }
        );
    }

    @Test
    public void createQiscusSpannableTextSpecialCase2Test() {
        QiscusTextUtil.createQiscusSpannableText(
                "[@message] ok siap ndan",
                roomMembers, color, color, color,
                member1 -> {
                    // ignored
                }
        );
    }

    @Test
    public void createQiscusSpannableTextAllTest() {
        QiscusTextUtil.createQiscusSpannableText(
                "[@message]  all",
                roomMembers, color, color, color,
                member1 -> {
                    // ignored
                }
        );
    }

}