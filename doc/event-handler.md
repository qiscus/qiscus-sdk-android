# Event Handler

**Implement QiscusChatPresenter.View to your Activity Or Fragment**

```java
public class MainActivity extends AppCompatActivity implements QiscusChatPresenter.View {
    private QiscusChatPresenter qiscusChatPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qiscusChatPresenter = new QiscusChatPresenter(this, qiscusChatRoom);
    }

    @Override
    public void initRoomData(QiscusChatRoom qiscusChatRoom, List<QiscusComment> comments) {
        // Do your implementation
    }

    @Override
    public void showComments(List<QiscusComment> qiscusComments) {
        // Do your implementation
    }

    @Override
    public void onLoadMore(List<QiscusComment> qiscusComments) {
        // Do your implementation
    }

    @Override
    public void onSendingComment(QiscusComment qiscusComment) {
        // Do your implementation
    }

    @Override
    public void onSuccessSendComment(QiscusComment qiscusComment) {
        // Do your implementation
    }

    @Override
    public void onFailedSendComment(QiscusComment qiscusComment) {
        // Do your implementation
    }

    @Override
    public void onNewComment(QiscusComment qiscusComment) {
        // Do your implementation
    }

    @Override
    public void onCommentDeleted(QiscusComment qiscusComment) {
        // Do your implementation
    }

    @Override
    public void refreshComment(QiscusComment qiscusComment) {
        // Do your implementation
    }

    @Override
    public void updateLastDeliveredComment(int lastDeliveredCommentId) {
        // Do your implementation
    }

    @Override
    public void updateLastReadComment(int lastReadCommentId) {
        // Do your implementation
    }

    @Override
    public void onFileDownloaded(File file, String mimeType) {
        // Do your implementation
    }

    @Override
    public void onUserTyping(String user, boolean typing) {
        // Do your implementation
    }
}
```

**EventBus, so you can listen event from anywhere, It does not matter whether it's an activity or not.**
For example from your application class

```java
public class SampleApps extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Qiscus.init(this, "APP_ID");

        EventBus.getDefault().register(this);
    }

    /**
     * Subscribe anywhere to listen new message if you just got new message from someone
     */
    @Subscribe
    public void onGetNewQiscusComment(QiscusCommentReceivedEvent event) {
        QiscusComment qiscusComment = event.getQiscusComment();
        // Do your implementation
    }


    /**
     * Call QiscusPusherApi.getInstance().listenRoom(qiscusChatRoom); to get room event from anywhere at your application
     */
    @Subscribe
    public void onGetNewQiscusRoomEvent(QiscusChatRoomEvent event) {
        switch (event.getEvent()) {
            case TYPING:
                // Someone is typing on this room event.getRoomId();
                break;
            case DELIVERED:
                // Someone just received your message event.getCommentId()
                break;
            case READ:
                // Someone just read your message event.getCommentId()
                break;
        }
    }

    /**
     * Call QiscusPusherApi.getInstance().listenUserStatus("user1@gmail.com"); to listen status of user1@gmail.com
     */
    @Subscribe
    public void onUserStatusUpdated(QiscusUserStatusEvent event) {
        // A user just changed his/her status from (online or offline)
        // event.getUser() changed to event.isOnline() at event.getLastActive()
    }
}
```