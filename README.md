# Introduction

Qiscus Chat SDK (Software Development Kit) is a product provided by Qiscus that enables you to embed an in-app chat/chat feature in your applications quickly and easily. With our chat SDK, you can implement chat feature without dealing with the complexity of real-time communication infrastructure. We provide a powerful API to let you implement chat feature into your apps in the most seamless development process.

Qiscus Chat SDK provides many features such as:

* 1-on-1 chat
* Group chat
* Channel chat
* Typing indicator
* Image and file attachment
* Online presence
* Delivery receipt
* Read receipt
* Delete message
* Offline message
* Block user
* Custom real-time event
* Server-side integration with Server API and Webhook
* Embed bot engine in your app
* Enable push notification
* Export and import messages from your app

## Try Sample App

We have provided a sample app to help you get to know with our chat SDK. This sample app is built with full functionalities so that you can figure out the flow and main activities using Qiscus Chat SDK. Now you can freely customize your own UI. You can also build your own app on top of our sample app. For further details you can download this sample.

```
git clone https://github.com/qiscus/qiscus-chat-sdk-android-sample.git
```
This sample app uses sample APP ID, which means, by using this sample app, you will share the data with others. In case you want to try by your own, you can change the APP ID into your own APP ID. You can find your APP ID in your dashboard. Click [here](https://dashboard.qiscus.com/dashboard/login) to access your dashboard


# Getting Started

## **Step 1: Get Your APP ID**
First, you need to create your application in dashboard, by accessing [Qiscus Chat Dashboard](https://dashboard.qiscus.com/dashboard/login). You can create more than one App ID.


## **Step 2: Install Qiscus Chat SDK**
Qiscus Chat SDK requires minimum Android API 16 (Jelly Bean). To integrate your app with Qiscus, it can be done in 2 (two) steps. First, you need to add URL reference in your .gradle project. This reference is a guide for .gradle to get Qiscus Chat SDK from the right repository. Below is how to do that:

```
allprojects { 
      repositories { 
           ... 
           maven { url "https://dl.bintray.com/qiscustech/maven" } 
      } 
}
```

Second, you need to add SDK dependencies inside your app .gradle. Then, you need to synchronize to compile the Qiscus Chat SDK for your app.

```
dependencies { 
       ... 
       implementation 'com.qiscus.sdk:chat-core:1.3.21'
}
```

## **Step 3: Initialization Qiscus Chat SDK**
You need to initiate your APP ID for your chat app before carry out to authentication. This initialization only needs to be done once in the app lifecycle. Initialization can be implemented in the initial startup. Here is how you can do that:

```java
public class SampleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        QiscusCore.initWithAppId(this, APPID);

  }
 }
```
> The initialization should be called once across an Android app . The best practice you can put in application class.

## **Step 4: Authenticate to Qiscus**
To use Qiscus Chat SDK features, a user need to authenticate to Qiscus Server, for further detail you might figure out [Authentication section](https://documentation.qiscus.com/chat-sdk-android/authentications). This authentication is done by calling setUser() function. This function will retrieve or create user credential based on the unique userId, for example:

```java
QiscusCore.setUser(userId, userKey)
      .withUsername(username)
      .withAvatarUrl(avatarUrl)
      .withExtras(extras)
      .save(new QiscusCore.SetUserListener() {
          @Override
          public void onSuccess(QiscusAccount qiscusAccount) {
              //on success
          }
          @Override
          public void onError(Throwable throwable) {
              //on error 
      });
```
Where:

**userId** (string, unique): A user identifier that will be used to identify a user and used whenever another user need to chat with this user. It can be anything, whether is is user's email, your user database index, etc. As long as it is unique and a string.

**userKey** (string): userKey for authentication purpose, so even if a stranger knows your user Id, he cannot access the user data.

**username** (string): Username for display name inside Chat Room purposes.

**avatarURL** (string, optional): to display user's avatar, fallback to default avatar if not provided.

You can learn from the figure below to understand what really happened when calling `setUser()` function:

<p align="center"><br/><img src="https://raw.githubusercontent.com/qiscus/qiscus-sdk-android/develop/screenshot/set_user.png" width="80%" /><br/></p>


### Updating User Profile

After your user account is created, sometimes you may need to update a user information, such as changing user avatar. You can use method `QiscusCore.updateUser()` to make changes to your account.


```java
QiscusCore.updateUser(userName, avatarUrl, new QiscusCore.SetUserListener() {
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

### Clear User Data and disconnect

As mentioned in previous section, when you did setUser(), user's data will be stored locally. When user need to disconnect from Qiscus Chat SDK service, you need to clear the user data that is related to Qiscus Chat SDK, such as token, profile, messages, rooms, etc, from local device. You can do this by calling clearUser() method :

```java
QiscusCore.clearUser();
```

## Create Chat Room

**Chat Room** is a place where 2 or more users can chat each other. There are 3 type of Chat Room that can be created using Qiscus Chat SDK: 1-on-1 Chat Room, Group Chat Room, and Channel. For some cases, a room can be identified by room unique id or room name. 

### 1-on-1 Chat Room

We assume that you already know a targeted user you want to chat with. Make sure that your targeted user has been registered in Qiscus Chat SDK through setUser() method, as explained in the previous section. To start a conversation with your targeted user, it can be done with `getChatRoom()` method. Qiscus Chat SDK, then, will serve you a new Chat Room, asynchronously. When the room is succesfully created, Qiscus Chat SDK will return a Chat Room package through `onSuccess()` listener.

```java
QiscusApi.getInstance().chatUser(userId, options)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRoom -> {
                    // on success         
                }, throwable -> {
                    // on error        
                });
```

Where:

**userId**: A User identifier that will be used to identify a user and used whenever another user needs to chat with this user. It can be anything, whether is user's email, your user database index, etc. As long as it is unique and a string.

**distinctId**: (deprecated) you can fill “ ” (empty string).

**options**: metadata that can be as additional information to Chat Room, which consists key-value, for example, key: background, and value: red.

### Group Chat Room

When you want your many users to chat together in a 1-on-1 chat room, you need to create Group Room. Basically Group Room has the same concept as 1-on-1 Chat Room, but the different is that Group Room will target array of userIds in a single method. Here how you can create Group Room:

```java
QiscusApi.getInstance().createGroupChat(roomName, userIds, avatarUrl, options);
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRoom -> {
                    // on success
                }, throwable -> {
                    // on error
                });
```

## Channel

Creating Channel Chat Room is ideal for a use case that requires a lot of number participants, more than 100.

You need to set uniqueId to identify a Channel Chat Room. If a Chat Room with predefined **uniqueId** doesn't exist, it will create a new one with requester as the only one participant. Otherwise, if Chat Room with predefined **uniqueId** already exists, it will return that room and add requester as a participant.

When the room doesn't exist at the very first call and you do not send avatarUrl and/or roomName, it will use the default value. But, after the second call (room does exist) and you send avatarUrl and/or roomName, it will be updated to that value.
For example the implementation creating channel:

```java
QiscusApi.getInstance().createChannel(uniqueId, roomName, avatarUrl, options)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRoom -> {
                    // on success
                }, throwable -> {
                    // on error
                });
``` 

### Room Participant Management

In some cases, you may need to add additional participants into your room chat or even removing any participant.

### Get Chat Room List

To get all room list you can call QiscusApi.getInstance().getChatRooms(int page, int limit, boolean showMembers), page start from 1, limit indicate the max rooms per page, showMembers is flag for load room members also or not. Here sample code:

```java
QiscusApi.getInstance().getAllChatRooms(showParticipant, showRemoved, showEmpty, page, limit)
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(chatRooms -> {
        //on success
    }, throwable -> {
        //on error
    });
```

### Add Participant in Chat Room
You can add more than a participant in Chat Room by calling addRoomMember method. You can also pass multiple userIds. Once a participant succeeds to join the Chat Room, they will get a new Chat Room in their Chat Room list.

```java
QiscusApi.getInstance().addParticipants(roomId, userId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(chatRoom -> {
           // on success
        }, throwable -> {
            //on error        
        });
```

### Remove Participant in Chat Room
You can remove more than a participant in Chat Room by calling removeRoomMember method. You can also pass multiple userId. Once a participant is removed from the Chat Room, they will not find related Chat Room in their Chat Room list.

```java
QiscusApi.getInstance().removeParticipants(roomId, userId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(chatRoom -> {
           //success
        }, throwable -> {
            //error        
        });
```

## Enable Push Notification

First install FCM to your apps, you can follow [this steps.](https://firebase.google.com/docs/cloud-messaging/android/client) You can skip this step, if your apps already use FCM. Then put your api key to qiscus dashboard.
Now lets integrate with Qiscus client sdk, first enable FCM at Qiscus chat config.

```java
Qiscus.getChatConfig().setEnableFcmPushNotification(true);
```

After that, you need to change your firebase service to extend 'Qiscus firebase service' instead of firebase service class.

```java
public class MyFirebaseIdService extends QiscusFirebaseIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh(); // Must call super
        
        // Below is your own apps specific code
        // e.g register the token to your backend
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendTokenToMyBackend(refreshedToken);
    }
}

public class MyFirebaseMessagingService extends QiscusFirebaseService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (handleMessageReceived(remoteMessage)) { // For qiscus
            return;
        }

        //Your FCM PN here
    }
}
```

If extension is not possible or desirable, use the following code the ensure Qiscus handle the FCM.

```java
public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Register token to qiscus
        QiscusCore.setFcmToken(refreshedToken);

        // Below is your own apps specific code
        // e.g register the token to your backend
        sendTokenToMyBackend(refreshedToken);
    }
}

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

## Event Handler

Qiscus Chat SDK provides a simple way to let applications publish and listen to some real-time events. You can publish typing, read, user status, custom event so that you can handle freely in the event handler. This lets you inform users that another participant is actively engaged in communicating with them.

Qiscus Chat SDK is using EventBus for broadcasting event to the entire applications. You can learn more about EventBus on this [website](http://greenrobot.org/eventbus/). What you need to do is register the object which will receive event from EventBus. You can call it like this:

```java
EventBus.getDefault().register(this);
```

Once you don't need to listen to the event anymore, you have to unregister the receiver by calling this method:

```java
EventBus.getDefault().unregister(this);
```

This is an example on how to register an activity to receive event from EventBus:


```java
public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_my);

        // listen room event
        QiscusPusherApi.getInstance().subscribeChatRoom(qiscusChatRoom);

        // listen user status
        QiscusPusherApi.getInstance().subscribeUserOnlinePresence("userId");
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this); // register to EventBus
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this); // unregister from EventBus
    }

    @Subscribe
    public void onReceiveComment(QiscusCommentReceivedEvent event) {
        event.getQiscusComment(); // to get the comment    
    }

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
        QiscusPusherApi.getInstance().unsubsribeChatRoom(qiscusChatRoom);

        // stop listening user status
        QiscusPusherApi.getInstance().unsubscribeUserOnlinePresence("qiscus_user_id");
    }
}
```

## Using Proguard

ProGuard is the most popular optimizer for Java bytecode. It makes your Java and Android applications smaller and faster. Read [here](https://www.guardsquare.com/en/proguard) for more detail about Proguard. 
If you are using Proguard in your application, make sure you add Proguard rules of Qiscus from [Qiscus Proguard Rules](https://github.com/qiscus/qiscus-sdk-android/blob/master/app/proguard-rules.pro) to your Proguard rules.

## RXJava Support

For you who prefer to code with RXJava, Qiscus Chat SDK does support RXJava. So, you can do anything as you do with Native Java. For example, to set a user, as has been explained in [Basic Authentication](#basic-authentication) section, you can do the same with RXJava. Here an example of how you can set user with it:  

```java
// Setup qiscus account with rxjava example
Qiscus.setUser("user@email.com", "password")
      .withUsername("Tony Stark")
      .withAvatarUrl("http://avatar.url.com/handsome.jpg")
      .save()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(qiscusAccount -> {
          //do anything if success
      }, throwable -> {
          //do anything if error occurs
      });
```
