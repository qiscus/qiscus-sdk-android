# Android SDK API Reference

## Init

### Using App ID

```
/**
 * Initialize Qiscus
 *
 * application (Application)
 * appId (string)
 */
 
Qiscus.init(application, appId);
```

### Using custom server

```
/**
 * Initialize Qiscus with custom server
 *
 * application (Application)
 * appId (string)
 * serverBaseUrl (string)
 * mqttBrokerUrl (string)
 */
 
 Qiscus.init(application, appId, serverBaseUrl, mqttBrokerUrl);
```

Please call that function in your Application subclass, example.

```
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Qiscus.init(this, "your_app_id_here");
    }
}
```

## Authentication

### Using `UserID` and `UserKey`

```
/**
 * Basic Auth
 *
 * userId  (string|email|unique)
 * userKey (string)
 * username (string) 
 * avatarURL (string, optional)
 */

Qiscus.setUser(userId , userKey)
      .withUsername(userName)
      .withAvatarUrl(avatarUrl)
      .save(new Qiscus.SetUserListener() {
          @Override
          public void onSuccess(QiscusAccount qiscusAccount) {
              //on success followup
          }
          @Override
          public void onError(Throwable throwable) {
              //do anything if error occurs
      });
```

### Using JWT

Client side need call this function.

```
/**
 * JWT Auth
 *
 * request Nonce to Qiscus Chat SDK Server first
 */
 
 QiscusApi.getInstance().requestNonce();
```

You need to get Identity Token from you custom JWT API by using the nonce. Login or register with Identity Token. 

```
/**
 * After getting the JWT from your server,
 * you must to set JWT to Qiscus
 */
 
 Qiscus.setUser('yourjwttokenfromyourserverhere', new Qiscus.SetUserListener() {
        @Override
        public void onSuccess(QiscusAccount qiscusAccount) {
            //do anything if success
        }

        @Override
        public void onError(Throwable throwable) {
            //do anything if error occurs
        }
    });
```

## User

### Update user profile and profile image

```
/**
 * Update User Profile
 *
 * userName (string)
 * avatarUrl (string)
 */

Qiscus.updateUser(userName, avatarUrl, new Qiscus.SetUserListener() {
        @Override
        public void onSuccess(QiscusAccount qiscusAccount) {
           //do anything after it successfully updated
        }

        @Override
        public void onError(Throwable throwable) {   
           //do anything if error occurs                 
        }
    });
```

### Login Status

```
Qiscus.hasSetupUser(); // return true or false
```

### Logout

```
Qiscus.clearUser();
```

### Block User

```
QiscusApi.getInstance().blockUser(userEmail);
```

### Unblock User

```
QiscusApi.getInstance().unblockUser(userEMail);
```

## Message

Create message objects with various type of payloads.

### Text Message Object

```
/**
  * Text Message
  *
  * content (string) 
  * roomId (integer)
  * topicId (integer)
  */
 QiscusComment.generateMessage(content, roomId, topicId);
```

### File Attachment Object

```
/**
  * File Attachment Message
  *
  * fileUrl (string) 
  * caption (string)
  * roomId (integer)
  * topicId (integer)
  */
 QiscusComment.generateFileAttachmentMessage(fileUrl, caption, roomId, topicId)
```

### Contact Message Object

```
/**
   * create QiscusContact object for Contact Message
   *
   * name (string) : Jarjit Singh
   * value (string) : +628681234567
   * type (string) : phone
   */
  new QiscusContact(name, value, type);
  
  /**
   * Contact Message
   *
   * qiscusContact (QiscusContact)
   * roomId (integer)
   * topicId (integer)
   */
 QiscusComment.generateContactMessage(qiscusContact, roomId, topicId);
```

### Location Message Object

```
/**
   * create QiscusLocation object for Location Message
   * 
   * name (string)
   * address (string)
   * latitude (double)
   * longitude (double)
   */
  QiscusLocation qiscusLocation = new QiscusLocation();
  qiscusLocation.setName(name);
  qiscusLocation.setAddress(address);
  qiscusLocation.setLatitude(latitude);
  qiscusLocation.setLongitude(longitude);
  
   
  /**
   * Location Message
   *
   * qiscusLocation (QiscusLocation)
   * roomId (integer)
   * topicId (integer)
   */
  QiscusComment.generateLocationMessage(qiscusLocation, roomId, topicId);
```

### Reply Message Object

```
/**
   * Reply Message
   * 
   * content (string)
   * roomId (integer)
   * topicId (integer)
   * qiscusComment (QiscusComment) repliedComment object
   */
  QiscusComment.generateReplyMessage(content, roomId, topicId, qiscusComment);
```

### Post Back Message Object

```
/**
   * Post Back Message
   *
   * content (string)
   * payload (string|json) 
   * roomId (integer)
   * topicId (integer)
   */
  QiscusComment.generatePostBackMessage(content, payload, roomId, topicId);
```

### Custom Message Object

```
/**
   * Custom Message
   *
   * text (string)
   * type (string)
   * content (JSONObject)
   * roomId (integer)
   * topicId (integer)
   */   
  QiscusComment.generateCustomMessage(text, type, content, roomId, topicId);
```

### Send Message

After create QiscusComment object, you can send message using this function.

```
public void postComment(QiscusComment qiscusComment) {
    QiscusApi.getInstance().postComment(qiscusComment)
            .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment)) //update comment to local database
            .doOnNext(this::updateStateOnQiscus)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(commentSend -> {
                // success
            }, throwable -> {
                // error
            });
}
    
private void updateStateOnQiscus(QiscusComment comment) {
    comment.setState(QiscusComment.STATE_ON_QISCUS);
    QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(comment.getId(), comment.getUniqueId());
    if (savedQiscusComment != null && savedQiscusComment.getState() > comment.getState()) {
        comment.setState(savedQiscusComment.getState());
    }

    //update comment to local database
    Qiscus.getDataStore().addOrUpdate(comment);
}
```

### Load Messages

Load messages from server.

```
/**
 * Get comments
 *
 * roomId  (integer)
 * topicId (integer)
 * lastCommentId (integer) set it to 0 if want to load from latest comments
 * 
 * will return 20 comments
 */
 
QiscusApi.getInstance().getComments(roomId, topicId, lastCommentId);
```

Load messages from local database.

```
/**
 * Get comments
 *
 * topicId (integer)
 * count (integer) maximum comments need to return
 */
 
 Qiscus.getDataStore().getComments(topicId, count);
```

### Load More

Load more message from server.

```
/**
 * Get comments
 *
 * roomId  (integer)
 * topicId (integer)
 * lastCommentId (integer) set it to 0 if want to load from latest comments
 * 
 * will return 20 comments
 */
 
QiscusApi.getInstance().getComments(roomId, topicId, lastCommentId);
```

Load more message from local database.

```
/**
 * Get comments
 *
 * qiscusComment (QiscusComment)
 * topicId (integer)
 * count (integer) maximum comments need to return
 */
 
Qiscus.getDataStore().getOlderCommentsThan(qiscusComment, topicId, count);
```

### Download Media

```
/**
 * Download file
 *
 * topicId (integer) needed for identification saving the file
 * fileUrl (string)
 * fileName (string) save file as with this name
 */
QiscusApi.getInstance().downloadFile(topicId, fileUrl, fileName, percentage -> {
      // here you can get the progress percentage      
});
```

Snippet code above only download the file without saving information to local database. To save data to local database look at the code below.

```
public void downloadFile(QiscusComment qiscusComment) {
    QiscusApi.*getInstance*()
        .downloadFile(qiscusComment.getTopicId(), qiscusComment.getAttachmentUri().toString(), qiscusComment.getAttachmentName(), percentage -> {
            // here you can get the progress percentage 
        })
        .doOnNext(file -> {
            // here we update the local path of file
            Qiscus.*getDataStore*()
                    .addOrUpdateLocalPath(qiscusComment.getTopicId(), qiscusComment.getId(), file.getAbsolutePath());
        })
        .subscribeOn(Schedulers.*io*())
        .observeOn(AndroidSchedulers.*mainThread*())
        .subscribe(file -> {
            //success            
        }, throwable -> {
            //error            
        });
}
```

So if we need to get the local file of a comment, you can call this function.

```
/**
 * Get local file of comment
 *
 * commentId (integer)
 */
Qiscus.*getDataStore*().getLocalPath(commentId);
```

### Search Message

In all chat rooms.

```
/**
 * Search comments from backend
 *
 * query (string)
 * lastCommentId (integer)
 */
QiscusApi.getInstance().searchComments(query, lastCommentId);
```

```
/**
 * Search comments from local data
 *
 * query (string)
 * limit (integer)
 * offset (integer)
 */
 Qiscus.getDataStore().searchComments(query, limit, offset);
```

In specific chat room.

```
/**
 * Search comments
 *
 * query (string)
 * roomId (integer)
 * lastCommentId (integer)
 */
QiscusApi.getInstance().searchComments(query, roomId, lastCommentId);
```

```
/**
 * Search comments
 *
 * query (string)
 * roomId (integer)
 * limit (integer)
 * offset (integer)
 */
Qiscus.getDataStore().searchComments(query, roomId, limit, offset);
```

### Delete Message

```
/**
 * Delete message
 *
 * commentUniqueIds (String) 
 * isDeleteForEveryone (boolean) 
 * isHardDelete (boolean) 
 */
 
 QiscusApi.getInstance().deleteComments(commentUniqueIds, isDeleteForEveryone, isHardDelete)
```

### Clear Messages

```
/**
 * Clear messages
 *
 * roomIds (list<long>) | roomUniqueIds (list<long>)
 */
 
 QiscusApi.getInstance().clearCommentsByRoomIds(roomIds)
 
 // or you can using this
 
 QiscusApi.getInstance().clearCommentsByRoomUniqueIds(roomUniqueIds)
```

## Room

### Create 1-on-1 Chat Room

```
/**
 * Create Chat Room 1-on-1
 *
 * userId (string|email|unique) email or something unique
 * distinctId (string|nullable) deprecated
 * options (JSONObject|nullable) you can define JSON to save something in chatroom
 */
 
QiscusApi.getInstance().getChatRoom(userId, distinctId, options);
```

### Create Group Room

```
/**
 * Create Chat Room Group
 *
 * name (string) group name
 * ids (List<String>) list of userIds e.g. list of email
 * avatarUrl (string) url of group avatar
 * options (JSONObject|nullable) you can define JSON to save something in chatroom
 */
 
QiscusApi.getInstance().createGroupChatRoom(*name, ids, avatarUrl, options*);
```

After success creating chatroom, you must save chatroom to local data like this.

```
Qiscus.getDataStore().addOrUpdate(*chatRoom*);
```

### Get Chat Room By ID

Get chat room from server.

```
/**
 * Get chat room by id
 *
 * roomId (integer)
 */
QiscusApi.getInstance().getChatRoom(*roomId*);
```

Get chat room from local database.

```
/**
 * Get chat room by id
 *
 * roomId (integer|string)
 */
Qiscus.getDataStore().getChatRoom(*roomId*);
```

### Get Chat Room By Channel

Get chat room from server.

```
/**
 * Get chat room by channel
 *
 * uniqueId (string) the channel
 * name (string)
 * avatarUrl (string)
 * options (JSONObject)
 */
QiscusApi.getInstance().getGroupChatRoom(uniqueId, name, avatarUrl, options);
```

Get chat room from local database.

```
/**
 * Get chat room by channel
 *
 * uniqueId (string) the channel
 */
Qiscus.getDataStore().getChatRoomWithUniqueId(uniqueId);
```

### Get Chat Room Opponent By User ID

Get chat room opponent from server.

```
/**
 * Get chat room by opponent id
 *
 * withEmail (string) the opponent id
 * distinctId (string) if you need a difference room
 * options (JSONObject)
 */
 
QiscusApi.getInstance().getChatRoom(withEmail, distinctId, options);
```

Get chat room opponent from local database.

```
/**
 * Get chat room by opponent id
 *
 * email (string) the opponent id
 * distinctId (string) if you need a difference room
 */

Qiscus.getDataStore().getChatRoom(email);

//or

Qiscus.getDataStore().getChatRoom(email, distinctId);
```

### Get Room List

Get room list from server.

```
/**
 * Get room list
 *
 * page (integer) start from 1
 * limit (integer) the maximum size of list room
 * showMembers (boolean) if set to false, then the variable member in QiscusChatRoom will be null
 */
 
 QiscusApi.getInstance().getChatRooms(page, limit, showMembers);
```

Get room list from local database.

```
/**
 * Get room list
 *
 * limit (integer) the maximum size of list room
 * offset (integer)
 */
 
Qiscus.getDataStore().getChatRooms(limit, offset);

//or only using limit

Qiscus.getDataStore().getChatRooms(limit);

/**
 * or you want return observable
 */
 
Qiscus.getDataStore().getObservableChatRooms(limit, offset);

Qiscus.getDataStore().getObservableChatRooms(limit);
```

### Update Room

Update room in server.

```
/**
 * Update room
 *
 * roomId (integer)
 * name (string)
 * avatarUrl (string)
 * options (JSONObject)
 */
 
QiscusApi.getInstance().updateChatRoom(roomId, name, avatarUrl, options);
```

Update room in local database.

```
/**
 * Update room
 *
 * qiscusChatRoom (QiscusChatRoom)
 */
 
Qiscus.*getDataStore*().addOrUpdate(qiscusChatRoom);
```

### Get List of Participant in a Room

Get participant from local database.

```
/**
 * Search comments
 *
 * roomId (integer)
 */
 
Qiscus.*getDataStore*().getRoomMembers(*roomId*);
```

Get participant from QiscusChatRoom object.

```
qiscusChatRoom.getMember();
```

### Get Total Unread Count

```
/**
 * Get unread count
 */
 
QiscusApi.getInstance().getTotalUnreadCount();
```

### Add Participant in a Room

 ```
 /**
 * Add participant in a room
 * 
 * roomId (long)
 * ids (List<String>) list of userIds e.g. list of email (
 */
QiscusApi.getInstance().addRoomMember(roomId, ids)
```

### Remove Participant in a Room
```
/**
 * Remove participant in a room
 * 
 * roomId (long)
 * ids (List<String>) list of userIds e.g. list of email (
 */
QiscusApi.getInstance().removeRoomMember(roomId, ids)
```


## Statuses

### Publish Start Or Stop Typing

```
/**
 * roomId (integer)
 * topicId (integer)
 * typing (boolean)
 *
 * typing :
 *      true  | for start typing
 *      false | for stop typing
 */
 
QiscusPusherApi.getInstance().setUserTyping(roomId, topicId, typing);
```

### Update Message Status

```
/**
 * roomId (integer)
 * topicId (integer)
 * commentId (integer)
 * commentUniqueId (string|nullable)
 */
 
QiscusPusherApi.getInstance().setUserRead(roomId, topicId, commentId, commentUniqueId);
```

### View Who Has Received And Read Message

To get information who has read a message, we can get all room members of that chat room. In QiscusRoomMember class there is variable called lastReadCommentId. From this variable we can compare it to the id of the comment, if the lastReadCommentId from QiscusRoomMember class is larger than id of comment or lastReadCommentId equal with comment id, thats mean this QiscusRoomMember has read that comment. This is sample code to get who has read a comment.

```
// First get all members from this chat room
List<QiscusRoomMember> members = Qiscus.*getDataStore*().getRoomMembers(comment.getRoomId());

// List will save the member who has read the message
List<QiscusRoomMember> memberHasRead = new ArrayList<>();

// Get current user
QiscusAccount account = Qiscus.getQiscusAccount();
for (QiscusRoomMember member : members) {
    if (!member.getEmail().equals(account.getEmail())) { // not current user
        // if last read comment id is more than or equal with comment id, it means user has read
        if (member.getLastReadCommentId() >= comment.getId()) {
            memberHasRead.add(member);
        }
    }
}
```

## Event Handler

Qiscus SDK is using EventBus for broadcasting event to entire application. You can learn more about EventBus on this website http://greenrobot.org/eventbus/. What you need to do is registering the object which will receive event from EventBus. You can call it like this.

```
EventBus.getDefault().register(this);
```

And don't forget to unregister the receiver after you don't need to listen event anymore by calling this method.

```
EventBus.getDefault().unregister(this);
```

This is example how to register an activity for receiving event from EventBus.

```
public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_my);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.*getDefault*().register(this); // register to EventBus
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.*getDefault*().unregister(this); // unregister from EventBus
    }
}
```

After you register the receiver, now you can subscribe to specific event you need to know. What you need to do is you must create a method with org.greenrobot.eventbus.Subscribe annotation, the method name is up to you, but the parameter in your method must be equal with the event class which you want to listen.

### New Messages

Class for this event is com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent so now we can create a method that listen with this class type.

```
@Subscribe
public void onReceiveComment(QiscusCommentReceivedEvent event) {
    event.getQiscusComment(); // to get the comment
}
```

The method name is up to you, you also can make the method like below. And this is valid too.

```
@Subscribe
public void onGotQiscusMessage(QiscusCommentReceivedEvent event) {
    event.getQiscusComment(); // to get the comment
}
```

Please note QiscusCommentReceivedEvent not guarantee the event only published once per unique comment. So there are will be the same QiscusCommentReceivedEvent published, so you need to handle it.

### Typing

Different from listening receive comment, listening to user typing doesn't automatically started. So you must trigger Qiscus mqtt first to listen an event on specific room id. We make it like that for performance reason, listening event to so many room at the same time is not good, so we highly recommend to you just listen room event only to active room page. To listening room event you can call method from QiscusPusherApi class like this.

```
QiscusPusherApi.getInstance().listenRoom(qiscusChatRoom);
```

Don't forget to unlisten it after you don't need to listen event anymore by calling this method.

```
QiscusPusherApi.getInstance().unListenRoom(qiscusChatRoom);
```

After you call listen room method from QiscusPusherApi now you can subscribe to room event class which is com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent same like at listen receive comment, the method name is up to you too.

```
@Subscribe
public void onReceiveRoomEvent(QiscusChatRoomEvent roomEvent) {
    if (roomEvent.getEvent() == QiscusChatRoomEvent.Event.*TYPING*) {
        roomEvent.getRoomId(); // this is the room id        
        roomEvent.getUser(); // this is the qiscus user id        
        roomEvent.isTyping(); // true if the user is typing    
    }
}
```

### Message Status Change

```
@Subscribe
public void onReceiveRoomEvent(QiscusChatRoomEvent roomEvent) {
    switch (roomEvent.getEvent()) {
            case TYPING:
                roomEvent.getRoomId(); // this is the room id
                roomEvent.getUser(); // this is the qiscus user id
                roomEvent.isTyping(); // true if the user is typing
                break;
            case DELIVERED:
                roomEvent.getRoomId(); // this is the room id
                roomEvent.getUser(); // this is the qiscus user id
                roomEvent.getCommentId(); // the comment id was delivered
                break;
            case READ:
                roomEvent.getRoomId(); // this is the room id
                roomEvent.getUser(); // this is the qiscus user id
                roomEvent.getCommentId(); // the comment id was read
                break;
        }
}
```

### Participants Online Status

Same as listen room event, to get user online status you need to listen it using QiscusPusherApi first.

```
QiscusPusherApi.getInstance().listenUserStatus("qiscus_user_id");
```

Then create method that subscribe to event with com.qiscus.sdk.chat.core.event.QiscusUserStatusEvent class.

```
@Subscribe
public void onUserStatusChanged(QiscusUserStatusEvent event) {
    event.getUser(); // this is the qiscus user id    
    event.isOnline(); // true if user is online    
    event.getLastActive(); // Date of last active user
}
```

Don't forget to unlisten it after you don't need to listen event anymore by calling this method.

```
QiscusPusherApi.getInstance().unListenUserStatus("qiscus_user_id");
```

## Notification

By default push notification already enabled, without to do nothing anymore. But you can still change the configuration to meet your need.

### Enable or Disable Notification

```
Qiscus.getChatConfig().setEnablePushNotification(true); //default is true
```

By default too, notifications will appear either when opening a chat page or not. You can change it to just show notification only when user is not open that chat room page.

```
Qiscus.getChatConfig().setOnlyEnablePushNotificationOutsideChatRoom(true); // default is false
```

### FCM Support

Default notification is by mqtt event, we strongly recommend you to use Firebase Cloud Messaging (FCM) too to improve the reliability. First step is to enable FCM configuration.

```
Qiscus.getChatConfig().setEnableFcmPushNotification(true); // default is false
```

After that on your FirebaseInstanceIdService subclass please notify Qiscus the fcm token.

```
public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        
        // Register token to qiscus        
        Qiscus.setFcmToken(refreshedToken);
        
        // Below is your own apps specific code        
        // e.g register the token to your backend        
        sendTokenToMyBackend(refreshedToken);
    }
}
```

Then whenever FCM receive a message pass it to Qiscus, so Qiscus can handle that message.

```
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (QiscusFirebaseService.handleMessageReceived(remoteMessage)) { // For qiscus            
            return;
        }

        // Your FCM PN here    
    }
}
```

### Custom Notification

If you need a custom notification you can create a NotificationBuilderInterceptor

```
class MyNotificationBuilder implements QiscusNotificationBuilderInterceptor {
    @Override
    public boolean intercept(NotificationCompat.Builder notificationBuilder, QiscusComment qiscusComment) {
        //Here you can create your own notification        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationChannelId);        
        builder.setContentText(qiscusComment.getMessage());
        NotificationManagerCompat.from(context).notify(id, builder.build());

        return false; // return false to canceling qiscus showing notification    
    }
}
```

Then register the interceptor to Qiscus

```
Qiscus.getChatConfig().setNotificationBuilderInterceptor(new MyNotificationBuilder());
```


