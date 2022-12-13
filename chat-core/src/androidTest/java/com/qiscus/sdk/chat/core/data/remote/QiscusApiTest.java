package com.qiscus.sdk.chat.core.data.remote;

import androidx.core.util.Pair;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.event.QiscusSyncEvent;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
import com.qiscus.sdk.chat.core.util.QiscusLogger;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QiscusApiTest extends InstrumentationBaseTest {

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
                        QiscusCore.updateUser("testing", "https://", new JSONObject());

                    }
                    @Override
                    public void onError(Throwable throwable) {
                        //on error
                    }});

        QiscusApi.getInstance().reInitiateInstance();


    }

    @Test
    public void requestNonce() {
        QiscusApi.getInstance().requestNonce();
        QiscusApi.getInstance().getJWTNonce();
    }


    @Test
    public void loginOrRegister() {
        QiscusApi.getInstance().loginOrRegister("testing21", "testing21","testing21","loginOrRegister");
        QiscusApi.getInstance().loginOrRegister("testing21", "testing21","testing21","loginOrRegister", null);
        QiscusApi.getInstance().setUser("testing21", "testing21","testing21","loginOrRegister", null);
        QiscusApi.getInstance().setUser("testing21", "testing21","testing21","loginOrRegister", new JSONObject());
    }


    @Test
    public void updateProfile() {
        QiscusApi.getInstance().updateProfile("testing20", "https://", null);
    }

    @Test
    public void updateProfile2() {
        QiscusApi.getInstance().updateProfile("testing23", "https://");
    }

    @Test
    public void updateProfile3() {
        QiscusApi.getInstance().updateProfile("testing21", "https://",  new JSONObject());
    }

    @Test
    public void updateProfile5() {
        QiscusApi.getInstance().updateUser("testing24", "https://");
    }

    @Test
    public void updateProfile6() {
        QiscusApi.getInstance().updateUser("testing21", "https://",  new JSONObject());
    }

    @Test
    public void getUserData() {
        QiscusApi.getInstance().getUserData();
    }

    @Test
    public void singleChat() {
        QiscusApi.getInstance()
                .chatUser("arief96", new JSONObject())
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));

        QiscusApi.getInstance()
                .chatUser("arief93",null)
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));

        QiscusApi.getInstance()
                .getChatRoom("arief94", new JSONObject())
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));

        QiscusApi.getInstance()
                .getChatRoom("arief95",null)
                .doOnNext(qiscusChatRoom -> {
                    QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
                    QiscusApi.getInstance().getChatRoom(qiscusChatRoom.getId());
                    QiscusApi.getInstance().getChatRoomInfo(qiscusChatRoom.getId());
                });


    }

    @Test
    public void groupChat() {
        ArrayList<String> emails = new ArrayList<String>();

        emails.add("arief93");
        emails.add("arief92");

        QiscusApi.getInstance()
                .createGroupChat("test group", new ArrayList<>(emails), "https://", new JSONObject())
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));

        QiscusApi.getInstance()
                .createGroupChat("test group", new ArrayList<>(emails), "https://", null)
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));


        QiscusApi.getInstance()
                .createGroupChatRoom("test group", new ArrayList<>(emails), "https://", new JSONObject())
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));

        QiscusApi.getInstance()
                .createGroupChatRoom("test group", new ArrayList<>(emails), "https://", null)
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));



        QiscusApi.getInstance()
                .createGroupChatRoom("test group", new ArrayList<>(emails), "https://", new JSONObject())
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));

        QiscusApi.getInstance()
                .createGroupChatRoom("test group", new ArrayList<>(emails), "https://", null)
                .doOnNext(qiscusChatRoom -> {
                    QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
                });

    }

    @Test
    public void channelChat() {
        QiscusApi.getInstance()
                .createChannel("123", "channel test", "https://", new JSONObject())
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));

        QiscusApi.getInstance()
                .createChannel("1234", "channel test2", "https://", null)
                .doOnNext(qiscusChatRoom -> {
                    QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
                });

        QiscusApi.getInstance()
                .getGroupChatRoom("123", "channel test", "https://", new JSONObject())
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom));

        QiscusApi.getInstance()
                .getGroupChatRoom("1234", "channel test2", "https://", null)
                .doOnNext(qiscusChatRoom -> {
                    QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
                });


        QiscusApi.getInstance().getChannel("123");
    }

    @Test
    public void getChatRoom() {
        QiscusApi.getInstance()
                .chatUser("arief93", new JSONObject())
                .doOnNext(qiscusChatRoom -> {
                    QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
                } );
        QiscusApi.getInstance().getChatRoom(roomId);
    }

    @Test
    public void getChatRoomInfo() {
        QiscusApi.getInstance()
                .chatUser("arief93", new JSONObject())
                .doOnNext(qiscusChatRoom -> {
                    QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);

                } );
        QiscusApi.getInstance().getChatRoomInfo(roomId);
    }

    @Test
    public void getChatRoomComments() {
        QiscusApi.getInstance().getChatRoomComments(roomId)
                .doOnError(throwable -> {
                    QiscusErrorLogger.print(throwable);
                    QiscusAndroidUtil.runOnUIThread(() -> {

                    });
                })
                .doOnNext(roomData -> {

                    Collections.sort(roomData.second, (lhs, rhs) -> rhs.getTime().compareTo(lhs.getTime()));

                    QiscusCore.getDataStore().addOrUpdate(roomData.first);
                })
                .doOnNext(roomData -> {
                    for (QiscusComment qiscusComment : roomData.second) {
                        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
                    }
                })
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable -> null);
    }

    @Test
    public void getChatRoomWithMessages() {
        QiscusApi.getInstance().getChatRoomWithMessages(roomId)
                .doOnError(throwable -> {
                    QiscusErrorLogger.print(throwable);
                    QiscusAndroidUtil.runOnUIThread(() -> {

                    });
                })
                .doOnNext(roomData -> {

                    Collections.sort(roomData.second, (lhs, rhs) -> rhs.getTime().compareTo(lhs.getTime()));

                    QiscusCore.getDataStore().addOrUpdate(roomData.first);
                })
                .doOnNext(roomData -> {
                    for (QiscusComment qiscusComment : roomData.second) {
                        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
                    }
                })
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable -> null);
    }


    @Test
    public void getChagetAllChatRoomstRooms() {
        QiscusApi.getInstance().getAllChatRooms(true, true, true, 1, 50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                }, throwable -> {
                    //on error
                });
    }

    @Test
    public void getChagetAllChatRoomstRooms2() {
        QiscusApi.getInstance().getAllChatRooms(true, true, true, QiscusChatRoom.RoomType.SINGLE,1, 50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                }, throwable -> {
                    //on error
                });

        QiscusApi.getInstance().getAllChatRooms(true, true, true, QiscusChatRoom.RoomType.GROUP,1, 50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                }, throwable -> {
                    //on error
                });

        QiscusApi.getInstance().getAllChatRooms(false, false, false, QiscusChatRoom.RoomType.CHANNEL,1, 50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                }, throwable -> {
                    //on error
                });

        QiscusApi.getInstance().getAllChatRooms(true, true, true, null,1, 50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                }, throwable -> {
                    //on error
                });
    }

    @Test
    public void getChatRooms() {
        QiscusApi.getInstance().getChatRooms(1,50,true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                }, throwable -> {
                    //on error
                });
    }

    @Test
    public void getChatRoomsListOfRoomIDs() {
        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(Long.valueOf(roomId));
        QiscusApi.getInstance().getChatRooms(ids,null,true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                }, throwable -> {
                    //on error
                });

        QiscusApi.getInstance().getChatRooms(ids,true, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                }, throwable -> {
                    //on error
                });
    }

    @Test
    public void getChatRoomsWithUniqueIds() {
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(roomUniqId);
        QiscusApi.getInstance().getChatRoomsWithUniqueIds(ids,true, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                }, throwable -> {
                    //on error
                });
    }

    @Test
    public void getComments() {
        QiscusApi.getInstance().getComments(roomId,0);
        QiscusApi.getInstance().getComments(roomId,1161463672);
    }
    @Test
    public void getCommentsAfter() {
        QiscusApi.getInstance().getCommentsAfter(roomId,0);
        QiscusApi.getInstance().getCommentsAfter(roomId,1161463672);

    }

    @Test
    public void getPreviousMessagesById() {
        QiscusApi.getInstance().getPreviousMessagesById(roomId,100,0);
        QiscusApi.getInstance().getPreviousMessagesById(roomId,100, 1161463672);
        QiscusApi.getInstance().getPreviousMessagesById(roomId,100);
        QiscusApi.getInstance().getPreviousMessagesById(roomId,100);
    }


    @Test
    public void getNextMessagesById() {
        QiscusApi.getInstance().getNextMessagesById(roomId,100,0);
        QiscusApi.getInstance().getNextMessagesById(roomId,100, 1161463672);
        QiscusApi.getInstance().getNextMessagesById(roomId,100);
        QiscusApi.getInstance().getNextMessagesById(roomId,100);

    }

    @Test
    public void updateMessage() {
        QiscusApi.getInstance().getChatRooms(1,50,true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                    QiscusApi.getInstance().updateMessage(chatRooms.get(1).getLastComment());
                }, throwable -> {
                    //on error
                });
    }

    @Test
    public void postComment() {
        QiscusApi.getInstance().postComment(QiscusComment.generateMessage(roomId,"test"));
    }

    @Test
    public void sendMessage(){
        QiscusApi.getInstance().sendMessage(QiscusComment.generateMessage(roomId,"test"));
    }

    @Test
    public void sync(){
        QiscusApi.getInstance().sync()
                .doOnSubscribe(() -> {
                })
                .doOnCompleted(() -> {
                })
                .subscribeOn(Schedulers.io())
                .subscribe(QiscusPusherApi::handleReceivedComment, throwable -> {
                });
    }

    @Test
    public void sync2(){
        QiscusApi.getInstance().getChatRooms(1,50,true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                    QiscusApi.getInstance().sync(chatRooms.get(1).getLastComment().getId());

                }, throwable -> {
                    //on error
                });

    }

    @Test
    public void synchronize(){
        QiscusApi.getInstance().getChatRooms(1,50,true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRooms -> {
                    //on success
                    QiscusApi.getInstance().synchronize(chatRooms.get(1).getLastComment().getId());

                }, throwable -> {
                    //on error
                });

    }

    @Test
    public void syncEvent(){
        QiscusApi.getInstance().synchronizeEvent(0);
    }

    @Test
    public void sendFile(){
        File compressedFile = new File("/storage/emulated/0/Pictures/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.png");

        QiscusComment qiscusComment = QiscusComment.generateFileAttachmentMessage(roomId,
                compressedFile.getPath(), "caption", "name file");
        qiscusComment.setDownloading(true);

        File finalCompressedFile = compressedFile;

        QiscusApi.getInstance().sendFileMessage(
                        qiscusComment, finalCompressedFile, percentage -> {
                            qiscusComment.setProgress((int) percentage);
                        }).doOnSubscribe(() -> QiscusCore.getDataStore().addOrUpdate(qiscusComment))
                .doOnError(throwable -> {

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {
                    if (commentSend.getRoomId() == roomId) {
                        commentSend.setDownloading(false);
                        QiscusCore.getDataStore()
                                .addOrUpdateLocalPath(commentSend.getRoomId(),
                                        commentSend.getId(), finalCompressedFile.getAbsolutePath());
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    if (qiscusComment.getRoomId() == roomId) {

                    }
                });
    }

    @Test
    public void uploadFIle(){
        File compressedFile = new File("/storage/emulated/0/Pictures/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.png");

        QiscusComment qiscusComment = QiscusComment.generateFileAttachmentMessage(roomId,
                compressedFile.getPath(), "caption2", "name file2");
        qiscusComment.setDownloading(true);

        File finalCompressedFile = compressedFile;

        QiscusApi.getInstance().uploadFile(
                        finalCompressedFile, percentage -> {
                            qiscusComment.setProgress((int) percentage);
                        }).doOnSubscribe(() -> QiscusCore.getDataStore().addOrUpdate(qiscusComment))
                .doOnError(throwable -> {

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {

                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    if (qiscusComment.getRoomId() == roomId) {

                    }
                });
    }

    @Test
    public void upload(){
        File compressedFile = new File("/storage/emulated/0/Pictures/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.png");

        QiscusComment qiscusComment = QiscusComment.generateFileAttachmentMessage(roomId,
                compressedFile.getPath(), "caption2", "name file2");
        qiscusComment.setDownloading(true);

        File finalCompressedFile = compressedFile;

        QiscusApi.getInstance().upload(
                        finalCompressedFile, percentage -> {
                            qiscusComment.setProgress((int) percentage);
                        }).doOnSubscribe(() -> QiscusCore.getDataStore().addOrUpdate(qiscusComment))
                .doOnError(throwable -> {

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {

                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    if (qiscusComment.getRoomId() == roomId) {

                    }
                });
    }

    @Test
    public void downloadFile() {
        QiscusApi.getInstance()
                .downloadFile("https://www.shutterstock.com/image-vector/vector-illustration-sample-red-grunge-600w-2065712915.jpg", "name",
                        percentage -> {

                        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(file1 -> {
                    QiscusFileUtil.notifySystem(file1);
                })
                .subscribe(file1 -> {
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                });
    }

    @Test
    public void updateCommentStatus(){
        QiscusApi.getInstance().loginOrRegister("testing21", "testing21","testing21","loginOrRegister");
        QiscusApi.getInstance().updateCommentStatus(roomId,0,0);
    }

//    @Test
//    public void clearCommentsByRoomIds(){
//        ArrayList<Long> ids = new ArrayList<Long>();
//        ids.add(Long.valueOf(roomId));
//        QiscusApi.getInstance().clearCommentsByRoomIds(ids);
//    }
//
//    @Test
//    public void clearCommentsByRoomUniqueIds(){
//        ArrayList<String> ids = new ArrayList<String>();
//        ids.add(roomUniqId);
//        QiscusApi.getInstance().clearCommentsByRoomUniqueIds(ids);
//    }
//
//    @Test
//    public void clearMessagesByChatRoomIds(){
//        ArrayList<Long> ids = new ArrayList<Long>();
//        ids.add(Long.valueOf(roomId));
//        QiscusApi.getInstance().clearMessagesByChatRoomIds(ids);
//    }
//
//    @Test
//    public void clearMessagesByChatRoomUniqueIds(){
//        ArrayList<String> ids = new ArrayList<String>();
//        ids.add(roomUniqId);
//        QiscusApi.getInstance().clearMessagesByChatRoomUniqueIds(ids);
//    }

    @Test
    public void deleteComments(){
        QiscusApi.getInstance().setUser("testing21", "testing21","testing21","loginOrRegister", new JSONObject());

        ArrayList<String> uniqId = new ArrayList<String>();
        uniqId.add("javascript-1670826465468");
        QiscusApi.getInstance().deleteComments(uniqId, true);


    }


    @Test
    public void getEvents(){
        QiscusApi.getInstance().getEvents(0L);
    }



}