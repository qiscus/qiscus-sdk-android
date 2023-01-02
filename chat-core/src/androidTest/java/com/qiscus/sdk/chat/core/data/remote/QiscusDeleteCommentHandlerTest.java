package com.qiscus.sdk.chat.core.data.remote;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class QiscusDeleteCommentHandlerTest extends InstrumentationBaseTest {
    Integer roomId = 10185397;
    String roomUniqId = "8d412fdd3411f5f261f8f30e0f90ff60";
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
        QiscusAccount account = QiscusCore.getQiscusAccount();
        QiscusRoomMember actor = new QiscusRoomMember();
        actor.setEmail(account.getEmail());
        actor.setUsername(account.getUsername());
        actor.setAvatar(account.getAvatar());
        actor.setExtras(account.getExtras());


        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId,"test");
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
        qiscusChatRoom.setId(roomId);
        qiscusChatRoom.setLastComment(qiscusComment);
        QiscusCore.getDataStore().add(qiscusChatRoom);


        List<QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment> deletedComments = new ArrayList<>();
        deletedComments.add(new QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment(roomId,
                qiscusComment.getUniqueId()));


        QiscusDeleteCommentHandler.DeletedCommentsData deletedCommentsData
                = new QiscusDeleteCommentHandler.DeletedCommentsData();
        deletedCommentsData.setActor(actor);
        deletedCommentsData.setHardDelete(true);
        deletedCommentsData.setDeletedComments(deletedComments);
        deletedCommentsData.toString();
        deletedCommentsData.getActor();

        QiscusDeleteCommentHandler.DeletedCommentsData deletedCommentsData2
                = new QiscusDeleteCommentHandler.DeletedCommentsData();
        deletedCommentsData2.setActor(actor);
        deletedCommentsData2.setHardDelete(false);
        deletedCommentsData2.setDeletedComments(deletedComments);

        QiscusDeleteCommentHandler.handle(deletedCommentsData2);

        QiscusDeleteCommentHandler.handle(deletedCommentsData);
    }


    @Test
    public void handle2() {
        QiscusAccount account = QiscusCore.getQiscusAccount();
        QiscusRoomMember actor = new QiscusRoomMember();
        actor.setEmail(account.getEmail());
        actor.setUsername(account.getUsername());
        actor.setAvatar(account.getAvatar());
        actor.setExtras(account.getExtras());


        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId,"test");


        List<QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment> deletedComments = new ArrayList<>();
        deletedComments.add(new QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment(roomId,
                qiscusComment.getUniqueId()));


        QiscusDeleteCommentHandler.DeletedCommentsData deletedCommentsData
                = new QiscusDeleteCommentHandler.DeletedCommentsData();
        deletedCommentsData.setActor(actor);
        deletedCommentsData.setHardDelete(true);
        deletedCommentsData.setDeletedComments(deletedComments);

        QiscusDeleteCommentHandler.DeletedCommentsData deletedCommentsData2
                = new QiscusDeleteCommentHandler.DeletedCommentsData();
        deletedCommentsData2.setActor(actor);
        deletedCommentsData2.setHardDelete(false);
        deletedCommentsData2.setDeletedComments(deletedComments);

        QiscusDeleteCommentHandler.handle(deletedCommentsData2);

        QiscusDeleteCommentHandler.handle(deletedCommentsData);
    }
}