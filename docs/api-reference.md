## API Functions

### List of Main Class

* **com.qiscus.sdk.chat.core.QiscusCore**

> *This class is the main class of the QiscusCore library, all other components will be created and executed when the QiscusCore SDK library is initiated. *

> *To initiate the library, it should be done using this class, by calling the **init()** method. Initiation of libraries should only be done once throughout the life cycle of the application. *

> *Therefore do the library initiation process in the Application class exactly when the **onCreate() **method is called. *

> *In this class there are also some helper method that facilitate the process of making chat room, user setup, chat page configuration, and so forth.*

* **com.qiscus.sdk.chat.core.data.remote.QiscusApi**

> *This is the class responsible for interacting with the Qiscus server. *

> *Using this class we can access data stored on Qiscus servers, such as taking a list of user-owned chat rooms, sending messages, retrieving certain chat room data and so on. *

> *All methods that exist in this class will call the server, so that when there is no internet connection can be a failure. Then all methods in this class also do not give side effect to the data residing in local device, for example when calling method **getChatRoom() **then result will be directly returned without save it to database which is in local device, so when we try to access data in local , the chat room has not been saved. There is another class that will be responsible for managing data locally. Also note that all methods in this class will return the **rx.Observable object (RxJava)**, this is to facilitate multithreading handling. For developers who are uncomfortable with  RxJava, we provide helper class that can execute the method by using **com.qiscus.util.QiscusRxExecutor**.*

* **com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi**

> *If QiscusApi class using http request to interact with Qiscus server. Then this QiscusPusherApi class using mqtt to interact with the Qiscus mqtt broker. *

> *This class can be used to get events related to a chat room such as when a user is typing, when the message we send is received by the other person or when he read it, and to know the status of the user is online or not. *

> *The events that occur will be sent using EventBus, you can check existing event class list on package **com.qiscus.sdk.event**.*

* **com.qiscus.sdk.chat.core.data.local.QiscusDataStore**

> *Unlike the two classes above that always require internet connection to be used, QiscusDataStore class does not require internet connection because this class is responsible for managing data in local device. *

> *Please note that data stored on local devices can not be guaranteed to be the most recent and complete data such as data that exist on the server. *

> *However, the use of local data is to improve UX applications,  such as speed up the process of opening a chat page, when the activity chat run we can display a list of messages that are in the local first before then updated with the latest data from the server. Local data is also used for the process of resending the pending message, if we send a message when the internet connection condition is not good, by insert the message data into the local database will make it possible for Qiscus to retry the sending again at a later time when the connection has been start to stabilize again.*

### Setup

* init with *app_id*

```java
/**
 * Initialize Qiscus
 *
 * application (Application)
 * appId (string)
 */
 
QiscusCore.init(*application*, *appId*);
```

* init custom server

```java
/**
 * Initialize Qiscus with custom server
 *
 * application (Application)
 * appId (string)
 * serverBaseUrl (string)
 * mqttBrokerUrl (string)
 */
 
 QiscusCore.initCustomServer(*application, appId, serverBaseUrl, mqttBrokerUrl*);
```

* get *app_id*

```java
/**
 * Get app_id
 */
 
QiscusCore.getAppId()
```

### User

* Auth using *user_id *and *user_key*

```java
/**
 * Basic Auth
 *
 * userId  (string|email|unique)
 * userKey (string)
 * name (string) 
 * avatarURL (string, optional)
 * extras (JSONObject, optional)
 */

QiscusCore.setUser(*userId* , *userKey*)
      .withUsername(*name*)
      .withAvatarUrl(*avatarUrl*)
      .withExtras(*extras*)
      .save(new Qiscus.SetUserListener() {
          @Override
          public void onSuccess(QiscusAccount qAccount) {
              //on success followup
          }
          @Override
          public void onError(Throwable throwable) {
              //do anything if error occurs
      });
```

* Auth using *JWT*

> *Server Authentication is another option, which allow you to authenticate using Json Web Token [(JWT)](https://jwt.io/). Json web token contains your app account details which typically consists of a single string which contains information of two parts, Jose header and JWT claims set.*


The steps to authenticate with JWT goes like this:

1. The Client App request a *nonce* from Qiscus SDK server
2. Qiscus SDK Server will send *nonce* to Client App
3. Client App send user credentials and *nonce* that is obtained from Qiscus SDK Server to Client app Backend
4. The Client App backend will send the *token* to client app
5. The Client App send that *token* to Qiscus Chat SDK
6. Qiscus Chat SDK send Qiscus Account to Client App

![](../screenshot/jwt.png?raw=true)

> *You need to request Nonce from Qiscus Chat SDK Server. Nonce (Number Used Once) is a unique, randomly generated string used to identify a single request. Please be noted that a Nonce will expire in 10 minutes. So you need to implement your code to request JWT from your backend right after you got the returned Nonce. Here is how to authenticate to Qiscus Chat SDK using JWT :*

```java
/**
 * JWT Auth
 *
 * request Nonce to Qiscus Chat SDK Server first
 */
 
 QiscusApi.getInstance().requestNonce();
```

> *You will request Nonce from Qiscus SDK server and a Nonce will be returned. If it is success, you can request JWT from your backend by sending Nonce you got from Qiscus SDK Server. When you got the JWT Token, you can pass that JWT to QiscusCore.setUser()** method to allow Qiscus to authenticate your user and return user account, as shown in the code below :*

```java
/**
 * After getting the JWT from your server,
 * you must to set JWT to Qiscus
 */
 
 QiscusCore.setUser(*'yourjwttokenfromyourserverhere'*, new QiscusCore.SetUserListener() {
        @Override
        public void onSuccess(QiscusAccount qAccount) {
            //do anything if success
        }

        @Override
        public void onError(Throwable throwable) {
            //do anything if error occurs
        }
    });
```

* Updating *user *(profile and avatar)

```java
/**
 * Update User Profile
 *
 * userName (string)
 * avatarUrl (string)
 */

QiscusCore.updateUser(*name*, *avatarUrl*, new QiscusCore.SetUserListener() {
        @Override
        public void onSuccess(QiscusAccount qAccount) {
           //do anything after it successfully updated
        }

        @Override
        public void onError(Throwable throwable) {   
           //do anything if error occurs                 
        }
    });

/**
 * Update User Profile with extras
 *
 * userName (string)
 * avatarUrl (string)
 * extras (JSONObject)
 */
 
QiscusCore.updateUser(*name*, *avatarUrl*, *extras*, new QiscusCore.SetUserListener() {
            @Override
            public void onSuccess(QiscusAccount qAccount) {
                //do anything after it successfully updated
            }

            @Override
            public void onError(Throwable throwable) {
                //do anything if error occurs
            }
        });
```

* Check is *user* logged in

```java
/**
 * Check is User logged in
 */
 
QiscusCore.hasSetupUser(); //boolean
```

* Block *user*

```java
/**
 * Block User
 */
 
QiscusApi.getInstance().blockUser(*userEmail*)
```

* Unblock *user*

```java
/**
 * Unblock User
 */
 
QiscusApi.getInstance().unBlockUser(*userEmail*)
```

* Get list blocked *user*

```java
// get all blocked users without pagination
QiscusApi.getInstance().getBlockedUsers()

/**
 * get all blocked users with pagination
 * page (long)
 * limit (long)
 */
QiscusApi.getInstance().getBlockedUsers(page, limit)
```

* Logout *user*

```java
/**
 * Clear User session
 */
 
QiscusCore.clearUser();
```

### Comment

* Send *comment*

> *Before you send QiscusComment object, you must create the object first using provided methods in QiscusCore with parameters of specified type. Then, you can pass QiscusComment object to QiscusApi.getInstance().postComment() like below :*

```java

public void postComment(QiscusComment qiscusComment) {
    QiscusApi.getInstance().postComment(qiscusComment)
            .doOnSubscribe(() -> QiscusCore.getDataStore().addOrUpdate(qiscusComment)) //update comment to local database
            .doOnNext(this::updateStateOnQiscus)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(commentSent -> {
                // success
            }, throwable -> {
                // error
            });
}
    
private void updateStateOnQiscus(QiscusComment comment) {
    comment.setState(QiscusComment.STATE_ON_QISCUS);
    QiscusComment savedQiscusComment = QiscusCore.getDataStore().getComment(comment.getId(), comment.getUniqueId());
    if (savedQiscusComment != null && savedQiscusComment.getState() > comment.getState()) {
        comment.setState(savedQiscusComment.getState());
    }

    //update comment to local database
    QiscusCore.getDataStore().addOrUpdate(comment);
}
```

    * *TEXT*

```java
/**inter
  * Text Message
  *
  * roomId (long)
  * content (string) 
  */
QiscusComment.generateMessage(roomId, content)
```

    * *FILE*

```java
/**
  * File Attachment Message
  *
  * roomId (long)
  * fileUrl (string)
  * caption (string)
  * name (string)
  */
 QiscusComment.generateFileAttachmentMessage(roomId, fileUrl, caption, name);
```

    * *REPLY*

```java
/**
 * Reply Message
 * 
 * roomId (long)
 * content (string)
 * repliedComment (QiscusComment)
 */
QiscusComment.generateReplyMessage(roomId, content, repliedComment);
```

    * *CONTACT*

```java
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
  * roomId (long)
  * contact (QiscusContact)
  */
QiscusComment.generateContactMessage(roomId, contact);
```

    * *LOCATION*

```java
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
  * roomId (long)
  * location (QiscusLocation)
  */
QiscusComment.generateLocationMessage(roomId, location);
```

    * *POSTBACK*

```java
/**
 * Post Back Message
 * 
 * roomId (long)
 * content (string)
 * payload (string)
 */
QiscusComment.generatePostBackMessage(roomId, content, payload);
```

    * *CUSTOM*

```java
/**
 * Custom Message
 *
 * roomId (long)
 * text (string)
 * type (string) | define your own type
 * content (JSONObject)
 */
QiscusComment.generateCustomMessage(roomId, text, type, content);
```

* Load *comments* (with *limit* and *offset*)
    * From Server

```java
/**
 * Get comments
 *
 * roomId  (long)
 * lastCommentId (long)
 * 
 * will return 20 comments
 */
 
QiscusApi.getInstance().getComments(roomId, lastCommentId)
```

    * From Local

```java
/**
 * Get comments
 *
 * roomId (long)
 * limit (int) maximum comments need to return
 */
 
 QiscusCore.getDataStore().getComments(roomId, limit);
```

* Load *comments *after 
    * From Server

```java
/**
 * Get comments
 *
 * roomId  (long)
 * lastCommentId (long)
 * 
 * will return 20 comments
 */
 
QiscusApi.getInstance().getCommentsAfter(long roomId, long lastCommentId);
```

    * From Local

```java
/**
 * Get comments
 *
 * qiscusComment (QiscusComment)
 * roomId (long)
 * limit (integer) maximum comments need to return
 */
 
QiscusCore.getDataStore().getOlderCommentsThan(qiscusComment, roomId, limit);
```

* Download media (the *path *and % of process)

```java
/**
 * Download file
 *
 * file (string)
 * fileName (string) save file as with this name
 * totalDownloaded (long)
 */
QiscusApi.getInstance().downloadFile(url, fileName, totalDownloaded -> {
      // here you can get the progress total file downloaded      
});
```

> *That code only download the file without saving information to local database. To save data to local database look at the code below:*

```java
public void downloadFile(QiscusComment qiscusComment) {
    QiscusApi.*getInstance*()
        .downloadFile(qiscusComment.getAttachmentUri().toString(), qiscusComment.getAttachmentName(), total -> {
            // here you can get the progress total downloaded 
        })
        .doOnNext(file -> {
            // here we update the local path of file
            QiscusCore.*getDataStore*()
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

> *So if we need to get the local file of a comment, you can call this:*

```java
/**
 * Get local file of comment
 *
 * commentId (long)
 */
QiscusCore.*getDataStore*().getLocalPath(commentId);
```

* Keyword search *comments*
    * In all *chatrooms*
        * From Local

```java
/**
 * Search comments from local data
 *
 * query (string)
 * roomId (long)
 * limit (integer)
 * offset (integer)
 */
 QiscusCore.getDataStore().searchComments(query, roomId, limit, offset);
```

    * In speficified *chatrooms*

        * From Local

```java
/**
 * Search comments
 *
 * query (string)
 * roomId (long)
 * limit (integer)
 * offset (integer)
 */
QiscusCore.getDataStore().searchComments(query, roomId, limit, offset);
```

* Delete *comment*

```java
/**
 * Delete comments
 *
 * commentUniqueIds (String) 
 * isHardDelete (boolean) 
 */ 
QiscusApi.getInstance().deleteComments(commentUniqueIds, isHardDelete);
```

* Clear all *comments*
    * From Server

```java
/**
 * Clear messages
 *
 * roomIds (list<long>) | roomUniqueIds (list<long>)
 */
 
 QiscusApi.getInstance().clearCommentsByRoomIds(roomIds)
 
 // or you can using this
 
 QiscusApi.getInstance().clearCommentsByRoomUniqueIds(roomUniqueIds)
```

    * From Local

```java
/**
  * Local
  */  

QiscusCore.getDataStore().deleteCommentsByRoomId(roomId)
  
//or
  
QiscusCore.getDataStore().deleteCommentsByRoomId(roomId, timestampOffset)
```

* Working with extras

> *You can add **JSON** into **extras **property inside **QiscusComment** object*

```java
// generate TEXT comment
QiscusComment qiscusComment = QiscusComment.generateMessage("This is message", 123, 123);

// set extras like below :
String stringJson = "{\"key\" : \"value\", \"key2\" : \"value2\"}";
qiscusComment.setExtras(new JSONObject(stringJson));

//using Comment Interceptor
QiscusCore.getChatConfig().setCommentSendingInterceptor(qiscusComment -> {
                    qiscusComment.setExtras(new JSONObject(stringJson));
                    return qiscusComment;
                });
```

### Chatroom

> *In Qiscus SDK, we provide 3 type of chatrooms :*

> ***SINGLE, GROUP, CHANNEL***

* Create *chatroom *1-on-1

```java
/**
 * Create Chat Room 1-on-1
 *
 * userId (string|email|unique) email or something unique
 * distinctId (string|nullable) deprecated
 * options (JSONObject|nullable) you can define JSON to save something in chatroom
 */
 
QiscusApi.getInstance().getChatRoom(userId, distinctId, options);
```

* Create *group chatroom *

```java
/**
 * Create Chat Room Group
 *
 * name (string) group name
 * ids (List<String>) list of userIds e.g. list of email
 * avatarUrl (string) url of group avatar
 * options (JSONObject|nullable) you can define JSON to save something in chatroom
 */
 
QiscusApi.getInstance().createGroupChatRoom(name, ids, avatarUrl, options);
```

> *After success creating chatroom, you must save chatroom to local data like this :*

```java
QiscusCore.getDataStore().addOrUpdate(*chatRoom*);
```

* Get *chatroom *by *id*
    * From Server

```java
/**
 * Get chat room by id
 *
 * roomId (long)
 */
QiscusApi.getInstance().getChatRoom(roomId);
```

    * From Local

```java
/**
 * Get chat room by id
 *
 * roomId (long)
 */
QiscusCore.getDataStore().getChatRoom(roomId);
```

* Get *chatroom *by *channel*
    * From Server

```java
/**
 * Get chat room by channel
 *
 * uniqueId (string) the channel
 * name (string)
 * avatarUrl (string)
 * options (JSONObject)
 */
QiscusApi.*getInstance*().getGroupChatRoom(uniqueId, name, avatarUrl, options);
```

    * From Local

```java
/**
 * Get chat room by channel
 *
 * uniqueId (string) the channel
 */
QiscusCore.*getDataStore*().getChatRoomWithUniqueId(uniqueId);
```

* Get *chatroom *opponent by *user_id*
    * From Server

```java
/**
 * Get chat room by opponent id
 *
 * withEmail (string) the opponent id
 * distinctId (string) if you need a difference room
 * options (JSONObject)
 */
 
QiscusApi.*getInstance*().getChatRoom(withEmail, distinctId, options);
```

    * From Local

```java
/**
 * Get chat room by opponent id
 *
 * email (string) the opponent id
 * distinctId (string) if you need a difference room
 */

QiscusCore.*getDataStore*().getChatRoom(email);

//or

QiscusCore.*getDataStore*().getChatRoom(email, distinctId);
```

> *So if you want to save the chatroom from server after create or get chatroom from server, the code will be like this*

```java
QiscusApi.*getInstance*().getChatRoom(withEmail*,** *distinctId, options)
        .doOnNext(chatRoom -> QiscusCore.*getDataStore*().addOrUpdate(chatRoom))
        .subscribeOn(Schedulers.*io*())
        .observeOn(AndroidSchedulers.*mainThread*())
        .subscribe(chatRoom -> {
           //success         
        }, throwable -> {
            //error        
        });
```

* Get *chatroom *info 

> *If you need some chatrooms using **array****** of ids**, you can use this method*

    * From Server

```java
/**
 * Get rooms info
 *
 * roomIds List<Long>
 * uniqueIds (List<String>)
 * showMembers (boolean) if set to false, then the variable member in QiscusChatRoom will be null
 */
 
QiscusApi.getInstance().getChatRooms(roomIds, uniqueIds, showMembers);
```

    * From Local

```java
/**
 * roomIds List<Long>
 * uniqueIds (List<String>)
 */
 
QiscusCore.getDataStore().getChatRooms(roomIds, uniqueIds);
```

* Get *chatroom *list

> *If you need to get **all chatrooms in the user**, you can use this method*

    * From Server

```java
/**
 * Get room list
 *
 * page (integer) start from 1
 * limit (integer) the maximum size of list room
 * showMembers (boolean) if set to false, then the variable member in QiscusChatRoom will be null
 */
 
QiscusApi.getInstance().getChatRooms(page, limit, showMembers);
```

    * From Local

```java
/**
 * Get room list
 *
 * limit (integer) the maximum size of list room
 * offset (integer)
 */
 
QiscusCore.getDataStore().getChatRooms(limit, offset);

//or only using limit

QiscusCore.getDataStore().getChatRooms(limit);

/**
 * or you want return observable
 */
 
QiscusCore.getDataStore().getObservableChatRooms(limit, offset);

QiscusCore.getDataStore().getObservableChatRooms(limit);
```

> *If we need to cached rooms from server to local, so the code will be like this:*

```java
QiscusApi.getInstance().getChatRooms(page, limit, showMembers)
    .doOnNext(chatRooms -> {
        for (QiscusChatRoom chatRoom : chatRooms) {
            QiscusCore.getDataStore().addOrUpdate(chatRoom);
        }
    })
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(chatRooms -> {
        //success
    }, throwable -> {
        //error
    });
```

* Update *chatroom *(include options)
    * From Server

```java
/**
 * Update room
 *
 * roomId (long)
 * name (string)
 * avatarUrl (string)
 * options (JSONObject)
 */
 
QiscusApi.*getInstance*().updateChatRoom(roomId, name, avatarUrl, options);
```

    * From Local

```java
/**
 * Update room
 *
 * qiscusChatRoom (QiscusChatRoom)
 */
 
Qiscus.getDataStore().addOrUpdate(qiscusChatRoom);
```

* Get *chatroom's participants*
    * From Server

```java
/**
 * roomUniqueId (String)
 *
 * default limit is 100
 */
QiscusApi.getInstance().getRoomMembers(roomUniqueId);

/**
 * roomUniqueId String
 * offset intger
 * orderKey String | valid value **name**, **joined_at** or **email**, default to order by name
 * sorting String  | **asc** or **desc**, default to asc
 * metaRoomMembersListener MetaRoomMembersListener
 */
QiscusApi.getInstance().getRoomMembers(roomUniqueId, offset, orderKey, sorting, metaRoomMembersListener);
 
 /**
 * roomUniqueId String
 * offset intger
 * orderKey String | valid value **name**, **joined_at** or **email**, default to order by name
 * sorting String  | **asc** or **desc**, default to asc
 * userName String | if you want to get participants based on **userName** filter
 * metaRoomMembersListener MetaRoomMembersListener
 */
 QiscusApi.getInstance().getRoomMembers(roomUniqueId, offset, orderKey, sorting, userName, metaRoomMembersListener);
  
  // example :
 QiscusApi.getInstance().getRoomMembers("roomuniqueid", 0, null, null, (currentOffset, perPage, total) -> {
                    /**
                     * TODO : you can get currentOffset, perPage, and total here
                     * currentOffset -> current offset the data
                     * perPage -> limit data = 100
                     * total -> total data
                     */
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusRoomMembers -> {
                    // TODO : now you can qiscusRoomMembers here
                }, throwable -> {
                    // TODO :show error here
                });
```

    * From Local

```java
/**
 * roomId (long)
 */
 
Qiscus.getDataStore().getRoomMembers(roomId);

// from QiscusChatroom object
qiscusChatRoom.getMember();
```

* Add *chatroom's participant*

```java
/**
 * roomId (long)
 * emails (List<String>)
 */
QiscusApi.getInstance().addRoomMember(roomId, emails);
```

> *After success add room member, you must to update chatroom object to local data like this*

```java
QiscusApi.getInstance().addRoomMember(roomId, emails);
        .doOnNext(chatRoom -> Qiscus.getDataStore().addOrUpdate(chatRoom))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(chatRoom -> {
           //success
        }, throwable -> {
            //error        
        });
```

* Remove *chatroom's participant*

```java
/**
 * roomId (long)
 * emails (List<String>)
 */
QiscusApi.getInstance().removeRoomMember(roomId, emails)

// after success, you must update room to local data like above in QiscusApi.getInstance().addRoomMember()
```

* Get total unread count

```java
/**
 * Get unread count
 */
 
QiscusApi.getInstance().getTotalUnreadCount();
```

### Statuses

* Publish start and stop *typing*

```java
/**
 * roomId (long)
 * typing (boolean)
 *
 * typing :
 *      true  | for start typing
 *      false | for stop typing
 */
 
QiscusPusherApi.getInstance().setUserTyping(roomId, typing);
```

* Update *comment status*

```java
/**
 * roomId (long)
 * commentId (long)
 */
 
QiscusPusherApi.getInstance().setUserRead(roomId, commentId);
```

* Viewing who has read a *comment*

```java
/**
 * commentId (long)
 */

QiscusApi.getInstance().getCommentInfo(commentId);
```

### Event Handler

> *Qiscus SDK is using EventBus for broadcasting event to entire application. You can learn more about EventBus on this website http://greenrobot.org/eventbus/. What you need to do is registering the object which will receive event from EventBus. You can call it like this:*

```java
EventBus.*getDefault*().register(this);
```

> *And don't forget to unregister the receiver after you don't need to listen event anymore by calling this method:*

```java
EventBus.*getDefault*().unregister(this);
```

> *This is example how to register an activity for receiving event from EventBus:*

```java
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

> *After you register the receiver, now you can subscribe to specific event you need to know. What you need to do is you must create a method with **org.greenrobot.eventbus.Subscribe** annotation, the method name is up to you, but the parameter in your method must be equal with the event class which you want to listen.*

* On receive *comment*

> *Class for this event is **com.qiscus.sdk.event.QiscusCommentReceivedEvent** so now we can create a method that listen with this class type.*

```java
@Subscribe
public void onReceiveComment(QiscusCommentReceivedEvent event) {
    event.getQiscusComment(); // to get the comment
}
```

> *The method name is up to you, you also can make the method like below. And this is valid too.*

```java
@Subscribe
public void onGotQiscusMessage(QiscusCommentReceivedEvent event) {
    event.getQiscusComment(); // to get the comment
}
```

> *So now the activity class will be like this below:*

```java
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
    
    @Subscribe
    public void onReceiveComment(QiscusCommentReceivedEvent event) {
        event.getQiscusComment(); // to get the comment
    }
}
```

> *Plesae note: **QiscusCommentReceivedEvent** not guarantee the event only published once per unique comment. So there are will be the same **QiscusCommentReceivedEvent** published, so you need to handle it.*

* On *user typing*

> *Different from listening receive comment, listening to user typing doesn't automatically started. So you must trigger Qiscus mqtt first to listen an event on specific room id. We make it like that for performance reason, listening event to so many room at the same time is not good, so we highly recommend to you just listen room event only to active room page. To listening room event you can call method from **QiscusPusherApi** class like this:*

```java
QiscusPusherApi.getInstance().listenRoom(qiscusChatRoom);
```

> *Don't forget to unlisten it after you don't need to listen event anymore by calling this method:*

```java
QiscusPusherApi.getInstance().unListenRoom(qiscusChatRoom);
```

> *After you call listen room method from **QiscusPusherApi** now you can subscribe to room event class which is **com.qiscus.sdk.event.QiscusChatRoomEvent** same like at listen receive comment, the method name is up to you too.*

```java
@Subscribe
public void onReceiveRoomEvent(QiscusChatRoomEvent roomEvent) {
    if (roomEvent.getEvent() == QiscusChatRoomEvent.Event.*TYPING*) {
        roomEvent.getRoomId(); // this is the room id        
        roomEvent.getUser(); // this is the qiscus user id        
        roomEvent.isTyping(); // true if the user is typing    
    }
}
```

* On *comment *status change

> *This is same like above we need to listen to room event. So the code will be like this:*

```java
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
            case CUSTOM:
                //here, you can listen custom event
                roomEvent.getRoomId(); // this is the room id
                roomEvent.getUser(); // this is the qiscus user id
                roomEvent.getEventData(); //event data (JSON)
                break;
        }
}
```

* on *sync* event

```java
@Subscribe
public void onSyncEvent(QiscusSyncEvent event) {
    // TODO : you can get the event here
    boolean isStarted = event == QiscusSyncEvent.STARTED;
    boolean isFailed = event == QiscusSyncEvent.FAILED;
    boolean isCompleted = event == QiscusSyncEvent.COMPLETED;
}
```

* on *MQTT* event

```java
@Subscribe
public void onMqttEvent(QiscusMqttStatusEvent event) {
    // TODO : you can get the event here
    boolean isConnected = event == QiscusMqttStatusEvent.CONNECTED;
    boolean isDisconnected = event == QiscusMqttStatusEvent.DISCONNECTED; 
}
```

* Listen *participants *online statuses

> *Same as listen room event, to get user online status you need to listen it using **QiscusPusherApi** first.*

```java
QiscusPusherApi.getInstance().listenUserStatus("qiscus_user_id");
```

> *Then create method that subscribe to event with **com.qiscus.sdk.event.QiscusUserStatusEvent** class*

```java
@Subscribe
public void onUserStatusChanged(QiscusUserStatusEvent event) {
    event.getUser(); // this is the qiscus user id    
    event.isOnline(); // true if user is online    
    event.getLastActive(); // Date of last active user
}
```

> *Don't forget to unlisten it after you don't need to listen event anymore by calling this method:*

```java
QiscusPusherApi.getInstance().unListenUserStatus("qiscus_user_id");
```

> *So now the MyActivity class after that all will be like this:*

```java
public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_my);

        // listen room event
        QiscusPusherApi.*getInstance*().listenRoom(qiscusChatRoom);

        // listen user status
        QiscusPusherApi.getInstance().listenUserStatus("qiscus_user_id");
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

    @Subscribe
    public void onReceiveComment(QiscusCommentReceivedEvent event) {
        event.getQiscusComment(); // to get the comment    
    }
    
    @Subscribe
    public void onReceiveRoomEvent(QiscusChatRoomEvent roomEvent) {
        switch (roomEvent.getEvent()) {
            case *TYPING*:
                roomEvent.getRoomId(); // this is the room id                
                roomEvent.getUser(); // this is the qiscus user id                
                roomEvent.isTyping(); // true if the user is typing                
                break;
            case *DELIVERED*:
                roomEvent.getRoomId(); // this is the room id                
                roomEvent.getUser(); // this is the qiscus user id                
                roomEvent.getCommentId(); // the comment id was delivered                
                break;
            case *READ*:
                roomEvent.getRoomId(); // this is the room id                
                roomEvent.getUser(); // this is the qiscus user id                
                roomEvent.getCommentId(); // the comment id was read               
                break;
            case CUSTOM:
                //here, you can listen custom event
                roomEvent.getRoomId(); // this is the room id
                roomEvent.getUser(); // this is the qiscus user id
                roomEvent.getEventData(); //event data (JSON)
                break;
        }
    }

    @Subscribe
    public void onUserStatusChanged(QiscusUserStatusEvent event) {
        event.getUser(); // this is the qiscus user id    
        event.isOnline(); // true if user is online    
        event.getLastActive(); // Date of last active user
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // stop listening room event
        QiscusPusherApi.*getInstance*().unListenRoom(qiscusChatRoom);

        // stop listening user status
        QiscusPusherApi.getInstance().unListenUserStatus("qiscus_user_id");
    }
}
```

> *For all event Class that available on Qiscus you can check **com.qiscus.sdk.event** package https://github.com/qiscus/qiscus-sdk-android/tree/master/chat/src/main/java/com/qiscus/sdk/event*

* Custom Event

> *You can publish and listen custom event using Qiscus, to get started using this feature you can look example below*

```java
// publish 
QiscusPusherApi.getInstance().setEvent(long roomId, JSONObject data)

// listening event
QiscusPusherApi.getInstance().listenEvent(roomId);

//unlisten event
QiscusPusherApi.getInstance().unlistenEvent(roomId);
```

> *To retrieve Event Data, you can use EventBus by using **@Subscribe** annotation like below*

```java
@Subscribe
public void onReceiveRoomEvent(QiscusChatRoomEvent roomEvent) {
    switch (roomEvent.getEvent()) {
        case CUSTOM:
            //here, you can listen custom event
            roomEvent.getRoomId(); // this is the room id
            roomEvent.getUser(); // this is the sender's qiscus user id
            roomEvent.getEventData(); //event data (JSON)
        break;
    }
}
```

### Notification

> *Qiscus SDK has 2 source for notification, using **MQTT** and **FCM** . Default notification is from **MQTT**, you must implement **setNotificationListener() **in **chatConfig** like below :*



* Default

```java
QiscusCore.getChatConfig().setNotificationListener((context, qiscusComment) -> {
*    // TODO: Handle Notification here*
    **showNotification**(context, qiscusComment);
});

/**
 * 
 * ***showNotification****() is your method for show push notification,**
 * when you call **notify **method in **NotificationManagerCompat** you must run on **UI Thread
** */

*private void showNotification(Context context, QiscusComment qiscusComment) {

        //check if qiscusComment object already in local database
        if (QiscusCore.getDataStore().isContains(qiscusComment)) {
            return;
        }

        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        
        String notificationChannelId = QiscusCore.getApps().getPackageName() + ".qiscus.sdk.notification.channel";
        if (BuildVersionUtil.isOreoOrHigher()) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(notificationChannelId, "Chat", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        PendingIntent pendingIntent;
        Intent openIntent = new Intent(context, **NotificationClickReceiver.class**);
        openIntent.putExtra("data", qiscusComment);
        pendingIntent = PendingIntent.getBroadcast(context, QiscusNumberUtil.convertToInt(qiscusComment.getRoomId()),
                openIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, notificationChannelId);
        notificationBuilder.setContentTitle(qiscusComment.getSender())
                .setContentIntent(pendingIntent)
                .setContentText(qiscusComment.getMessage())
                .setTicker(qiscusComment.getMessage())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setGroupSummary(true)
                .setGroup("CHAT_NOTIF_" + qiscusComment.getRoomId())
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        QiscusAndroidUtil.runOnUIThread(() -> NotificationManagerCompat.from(context)
                .notify(QiscusNumberUtil.convertToInt(qiscusComment.getRoomId()), notificationBuilder.build()));
    }
/**
 * you must create **NotificationClickReceiver **class like below
 */
 public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // get qiscusComment object
        QiscusComment qiscusComment = intent.getParcelableExtra("data");

        // get qiscusChatRoom from API
        QiscusApi.getInstance()
                .getChatRoom(qiscusComment.getRoomId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom))
                .map(qiscusChatRoom -> getChatRoomActivity(context, qiscusChatRoom))
                .subscribe(newIntent -> start(context, newIntent), throwable ->
                        Toast.makeText(context, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private Intent getChatRoomActivity(Context context, QiscusChatRoom qiscusChatRoom) {
        return qiscusChatRoom.isGroup() ? GroupChatRoomActivity.generateIntent(context, qiscusChatRoom) :
                ChatRoomActivity.generateIntent(context, qiscusChatRoom);
    }

    private void start(Context context, Intent newIntent) {
        context.startActivity(newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
 

/**
 * add to your manifest
 */
 <receiver
    android:name=".service.NotificationClickReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="com.qiscus.OPEN_COMMENT_PN" />
    </intent-filter>
  </receiver>
    *
/**
 * or you can use **PushNotificationUtil** class inside chat module here : link
 * and you can use like this 
 * QiscusCore.getChatConfig().setNotificationListener(**QiscusPushNotificationUtil::handlePushNotification**)
 *
 */*
```

* FCM (Firebase Cloud Messaging) Support

> *To enable FCM for your Application, you need to enable FCM like below :*

```java
QiscusCore.getChatConfig().setEnableFcmPushNotification(true); // default is **false**
```

> *After enable FCM in **ChatConfig, **you please notify Qiscus about FCM Token like below :*

```java
public class **AppFirebaseInstanceIdService** extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        
        // Notify Qiscus about FCM token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        QiscusCore.setFcmToken(refreshedToken);
        
        //TODO : Application part here, maybe you need to send FCM token to your Backend
    }
}
```

> *Add the service to your Manifest :*

```xml
<service android:name=".service.**AppFirebaseInstanceIdService**">
    <intent-filter>
        <action android:name="com.google.firebase.INSTANCE_ID_EVENT" />
    </intent-filter>
</service>
```

> *Handle incoming **qiscusComment** object like below :*

```java
public class **AppFirebaseMessagingService** extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Qiscus will handle incoming message
        if (QiscusFirebaseMessagingUtil.handleMessageReceived(remoteMessage)) {
            return;
        }

        // TODO : here is hanlder for your application
        
    }
}
```

> *Add to Manifest :*

```xml
<service android:name=".service.**AppFirebaseMessagingService**">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

> *Add FCM Secret key to your [Qiscus Dashboard Settings](https://www.qiscus.com/dashboard)*

### Debugger

* Enable *debug*

```java
/**
 * *enableLog boolean*
 */
 
QiscusCore.getChatConfig().setEnableLog(*enableLog*);
```


