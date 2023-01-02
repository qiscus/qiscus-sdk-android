package com.qiscus.sdk.chat.core.data.remote;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class QiscusClearCommentsHandlerTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        QiscusCore.setup(application, "sdksample");

        QiscusCore.setUser("arief92", "arief92")
                .withUsername("arief92")
                .withAvatarUrl("https://")
                .withExtras(null)
                .save(new QiscusCore.SetUserListener() {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        //on success


                    }
                    @Override
                    public void onError(Throwable throwable) {
                        //on error
                    }});
    }

    @Test
    public void handle() {

        QiscusClearCommentsHandler.ClearCommentsData clearCommentsData
                = new QiscusClearCommentsHandler.ClearCommentsData();

        QiscusRoomMember actor = new QiscusRoomMember();
        actor.setEmail("arief92");
        actor.setUsername("arief92");

        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(Long.valueOf(12356));

        clearCommentsData.setTimestamp(1243454534);
        clearCommentsData.setActor(actor);
        clearCommentsData.setRoomIds(ids);
        clearCommentsData.getRoomIds();
        clearCommentsData.getTimestamp();


        QiscusClearCommentsHandler.handle(clearCommentsData);
    }

    @Test
    public void handle3(){
        QiscusClearCommentsHandler.ClearCommentsData clearCommentsData2
                = new QiscusClearCommentsHandler.ClearCommentsData();

        QiscusRoomMember actor2 = new QiscusRoomMember();
        actor2.setEmail("arief94");
        actor2.setUsername("arief94");


        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(Long.valueOf(12356));


        clearCommentsData2.setTimestamp(1243454534);
        clearCommentsData2.setActor(actor2);
        clearCommentsData2.setRoomIds(ids);

        QiscusClearCommentsHandler.handle(clearCommentsData2);
    }
}