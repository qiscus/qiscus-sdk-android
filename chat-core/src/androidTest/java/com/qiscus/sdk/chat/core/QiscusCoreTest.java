package com.qiscus.sdk.chat.core;

import static org.junit.Assert.*;

import android.widget.Toast;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.qiscus.sdk.chat.core.data.local.QiscusDataStore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRefreshToken;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import rx.Observable;

@RunWith(AndroidJUnit4ClassRunner.class)
public class QiscusCoreTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void init() {
        QiscusCore.init(application, "sdksample");
    }

    @Test
    public void setup() {
        QiscusCore.setup(application, "sdksample");
    }

    @Test
    public void initWithCustomServer() {
        QiscusCore.initWithCustomServer(application, "sdksample",
                "https://api3.qiscus.com", "ssl://realtime-bali.qiscus.com:1885", "https://realtime-bali.qiscus.com");
    }

    @Test
    public void setupWithCustomServer() {
        QiscusCore.setupWithCustomServer(application, "sdksample",
                "https://api3.qiscus.com", "ssl://realtime-bali.qiscus.com:1885", "https://realtime-bali.qiscus.com");
    }
    @Test
    public void setupWithCustomServer2() {
        QiscusCore.setupWithCustomServer(application, "sdksample",
                "https://api3.qiscus.com", "ssl://realtime-bali.qiscus.com:1885", null);
    }

    @Test
    public void testInitWithCustomServer() {
        QiscusCore.initWithCustomServer(application,"sdksample", "https://api3.qiscus.com", "ssl://realtime-bali.qiscus.com:1885", true, "https://realtime-bali.qiscus.com");
    }

    @Test
    public void isBuiltIn() {
        QiscusCore.isBuiltIn(false);
    }

    @Test
    public void checkAppIdSetup() {
        try {
            QiscusCore.checkAppIdSetup();
        } catch (RuntimeException e){

        }
    }

    @Test
    public void hasSetupUser() {
        QiscusCore.hasSetupUser();
    }

    @Test
    public void hasSetupUser2() {
        setup();
        QiscusCore.hasSetupUser();
    }

    @Test
    public void hasSetupAppID() {
        setup();
        QiscusCore.hasSetupAppID();
    }

    @Test
    public void hasSetupAppID2() {
        QiscusCore.hasSetupAppID();
    }

    @Test
    public void getQiscusAccount() {
        setup();

        try {
            QiscusApi.getInstance().loginOrRegister("arief92",
                    "arief92", "arief92", "https://", new JSONObject("{}"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            QiscusCore.getQiscusAccount();

        } catch (RuntimeException e){

        }
    }


    @Test
    public void getQiscusAccount2() {
        try {
            QiscusCore.getQiscusAccount();
        } catch (RuntimeException e){

        }
    }

    @Test
    public void openRealtimeConnection() {
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

        QiscusCore.setEnableDisableRealtime(true);
        QiscusCore.openRealtimeConnection();

    }


    @Test
    public void closeRealtimeConnection() {
        QiscusCore.setup(application, "sdksample");
        QiscusCore.setUser("arief92", "arief92")
                .withUsername("arief92")
                .withAvatarUrl("https://")
                .withExtras(null)
                .save(new QiscusCore.SetUserListener() {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        //on success
                        QiscusCore.updateUserAsObservable("testing", "https://", new JSONObject());
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        //on error
                    }});

        QiscusCore.closeRealtimeConnection();
    }

    @Test
    public void otherTest() {
        setup();
        QiscusCore.startSyncService();
        QiscusCore.stopSyncService();
        QiscusCore.isEnableMqttLB();
        QiscusCore.getEnableEventReport();
        QiscusCore.getEnableRealtime();
        QiscusCore.setEnableDisableRealtime(true);
        QiscusCore.setIsExactAlarmDisable(true);
        QiscusCore.getIsExactAlarmDisable();
        QiscusCore.getEnableSync();
        QiscusCore.getEnableSyncEvent();
        QiscusCore.isSyncServiceDisabledManually();
        QiscusCore.isAutoRefreshToken();

        try {
            QiscusApi.getInstance().loginOrRegister("arief92",
                    "arief92", "arief92", "https://", new JSONObject("{}"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        QiscusCore.openRealtimeConnection();
        QiscusCore.closeRealtimeConnection();

        QiscusCore.getMqttBrokerUrl();
        QiscusCore.getBaseURLLB();
        QiscusCore.willGetNewNodeMqttBrokerUrl();
        QiscusCore.getLocalDataManager();
        QiscusCore.getStatusRealtimeEnableDisable();

        QiscusCore.setHeartBeat(5000);
        QiscusCore.getAutomaticHeartBeat();
        QiscusCore.setSyncInterval(5000);
        QiscusCore.getDataStore().getChatRoom(123);
        QiscusCore.setUserWithIdentityToken("24534wrfsdfasdasd");
        QiscusCore.getAppsName();
        QiscusAndroidUtil.runOnUIThread(() -> {

        });

        QiscusCore.isOnForeground();
        QiscusCore.setAppInForeground();
        QiscusCore.getChatConfig().setEnableFcmPushNotification(true);
        QiscusCore.registerDeviceToken("34213asdasdas");
        QiscusCore.removeDeviceToken("214324234");
        QiscusCore.setFcmToken("12423424");
        QiscusCore.getFcmToken();
        QiscusCore.refreshToken(null);
        QiscusCore.getTaskExecutor();
        QiscusCore.setCustomHeader(new JSONObject());


        QiscusCore.refreshToken(new QiscusCore.SetRefreshTokenListener() {
            @Override
            public void onSuccess(QiscusRefreshToken refreshToken) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        QiscusCore.hasSetupUser();
        QiscusCore.getRefreshToken();
        QiscusCore.saveRefreshToken(new QiscusRefreshToken());
        QiscusCore.setUserAsObservable("23423");
        QiscusCore.setUser("2453434", new QiscusCore.SetUserListener() {
            @Override
            public void onSuccess(QiscusAccount qiscusAccount) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        QiscusCore.setUserWithIdentityToken("2324234", new QiscusCore.SetUserListener() {
            @Override
            public void onSuccess(QiscusAccount qiscusAccount) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });


        QiscusCore.clearUser();
    }

    @Test
    public void removeDeviceToken() {
        QiscusCore.removeDeviceToken("24234234");
    }

    @Test
    public void getToken() {
        QiscusCore.init(application, "sdksample");
        QiscusCore.getToken();
    }

    @Test
    public void getRefreshToken() {
        QiscusCore.init(application, "sdksample");
        QiscusCore.setUser("2453434", new QiscusCore.SetUserListener() {
            @Override
            public void onSuccess(QiscusAccount qiscusAccount) {
                QiscusCore.getRefreshToken();
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

    }

    @Test
    public void dataStore() {
        QiscusCore.setDataStore(new QiscusDataStore() {
            @Override
            public void clear() {

            }

            @Override
            public void add(QiscusChatRoom qiscusChatRoom) {

            }

            @Override
            public boolean isContains(QiscusChatRoom qiscusChatRoom) {
                return false;
            }

            @Override
            public void update(QiscusChatRoom qiscusChatRoom) {

            }

            @Override
            public void addOrUpdate(QiscusChatRoom qiscusChatRoom) {

            }

            @Override
            public QiscusChatRoom getChatRoom(long roomId) {
                return null;
            }

            @Override
            public QiscusChatRoom getChatRoom(String email) {
                return null;
            }

            @Override
            public QiscusChatRoom getChatRoom(String email, String distinctId) {
                return null;
            }

            @Override
            public QiscusChatRoom getChatRoomWithUniqueId(String uniqueId) {
                return null;
            }

            @Override
            public List<QiscusChatRoom> getChatRooms(int limit) {
                return null;
            }

            @Override
            public List<QiscusChatRoom> getChatRooms(int limit, int offset) {
                return null;
            }

            @Override
            public Observable<List<QiscusChatRoom>> getObservableChatRooms(int limit) {
                return null;
            }

            @Override
            public Observable<List<QiscusChatRoom>> getObservableChatRooms(int limit, int offset) {
                return null;
            }

            @Override
            public List<QiscusChatRoom> getChatRooms(List<Long> roomIds, List<String> uniqueIds) {
                return null;
            }

            @Override
            public void deleteChatRoom(long roomId) {

            }

            @Override
            public void addRoomMember(long roomId, QiscusRoomMember qiscusRoomMember, String distinctId) {

            }

            @Override
            public boolean isContainsRoomMember(long roomId, String email) {
                return false;
            }

            @Override
            public void updateRoomMember(long roomId, QiscusRoomMember qiscusRoomMember, String distinctId) {

            }

            @Override
            public void addOrUpdateRoomMember(long roomId, QiscusRoomMember qiscusRoomMember, String distinctId) {

            }

            @Override
            public List<QiscusRoomMember> getRoomMembers(long roomId) {
                return null;
            }

            @Override
            public void deleteRoomMember(long roomId, String email) {

            }

            @Override
            public void deleteRoomMembers(long roomId) {

            }

            @Override
            public void add(QiscusComment qiscusComment) {

            }

            @Override
            public boolean isContains(QiscusComment qiscusComment) {
                return false;
            }

            @Override
            public void update(QiscusComment qiscusComment) {

            }

            @Override
            public void addOrUpdate(QiscusComment qiscusComment) {

            }

            @Override
            public void delete(QiscusComment qiscusComment) {

            }

            @Override
            public boolean deleteCommentsByRoomId(long roomId) {
                return false;
            }

            @Override
            public boolean deleteCommentsByRoomId(long roomId, long timestampOffset) {
                return false;
            }

            @Override
            public void updateLastDeliveredComment(long roomId, long commentId) {

            }

            @Override
            public void updateLastReadComment(long roomId, long commentId) {

            }

            @Override
            public QiscusComment getComment(String uniqueId) {
                return null;
            }

            @Override
            public QiscusComment getCommentByBeforeId(long beforeId) {
                return null;
            }

            @Override
            public List<QiscusComment> getComments(long roomId) {
                return null;
            }

            @Override
            public List<QiscusComment> getComments(long roomId, int limit) {
                return null;
            }

            @Override
            public List<QiscusComment> getComments(long roomId, long timestampOffset) {
                return null;
            }

            @Override
            public Observable<List<QiscusComment>> getObservableComments(long roomId) {
                return null;
            }

            @Override
            public Observable<List<QiscusComment>> getObservableComments(long roomId, int limit) {
                return null;
            }

            @Override
            public List<QiscusComment> getOlderCommentsThan(QiscusComment qiscusComment, long roomId, int limit) {
                return null;
            }

            @Override
            public Observable<List<QiscusComment>> getObservableOlderCommentsThan(QiscusComment qiscusComment, long roomId, int limit) {
                return null;
            }

            @Override
            public List<QiscusComment> getCommentsAfter(QiscusComment qiscusComment, long roomId) {
                return null;
            }

            @Override
            public Observable<List<QiscusComment>> getObservableCommentsAfter(QiscusComment qiscusComment, long roomId) {
                return null;
            }

            @Override
            public QiscusComment getLatestComment() {
                return null;
            }

            @Override
            public QiscusComment getLatestComment(long roomId) {
                return null;
            }

            @Override
            public QiscusComment getLatestDeliveredComment(long roomId) {
                return null;
            }

            @Override
            public QiscusComment getLatestReadComment(long roomId) {
                return null;
            }

            @Override
            public List<QiscusComment> getPendingComments() {
                return null;
            }

            @Override
            public Observable<List<QiscusComment>> getObservablePendingComments() {
                return null;
            }

            @Override
            public List<QiscusComment> searchComments(String query, long roomId, int limit, int offset) {
                return null;
            }

            @Override
            public List<QiscusComment> searchComments(String query, int limit, int offset) {
                return null;
            }

            @Override
            public void saveLocalPath(long roomId, long commentId, String localPath) {

            }

            @Override
            public boolean isContainsFileOfComment(long commentId) {
                return false;
            }

            @Override
            public void updateLocalPath(long roomId, long commentId, String localPath) {

            }

            @Override
            public void addOrUpdateLocalPath(long roomId, long commentId, String localPath) {

            }

            @Override
            public File getLocalPath(long commentId) {
                return null;
            }

            @Override
            public void deleteLocalPath(long commentId) {

            }

            @Override
            public void add(QiscusRoomMember qiscusRoomMember) {

            }

            @Override
            public boolean isContains(QiscusRoomMember qiscusRoomMember) {
                return false;
            }

            @Override
            public void update(QiscusRoomMember qiscusRoomMember) {

            }

            @Override
            public void addOrUpdate(QiscusRoomMember qiscusRoomMember) {

            }

            @Override
            public QiscusRoomMember getMember(String email) {
                return null;
            }
        });
    }

}