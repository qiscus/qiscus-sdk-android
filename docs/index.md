## Introduction

With Qiscus Chat SDK (Software Development Kit), You can embed chat feature inside your Application quickly and easily without dealing with complexity of real-time communication infrastructure. We provide powerful API to let you quickly and seamlessly implement it into your App.

Qiscus Chat SDK provides features such as:

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
* Server side integration with Server API and Webhook
* Embed bot engine in your App
* Enable Push notification
* Export and import messages from your App

### How Qiscus works

We recommend that you understand the concept before proceeding with the rest

* Messaging

The messaging flow is simple: a user register to Qiscus Server, a user open a room, send a message to a Chat Room, and then other participants will receive the message within the room. As long as user connect to Qiscus Server user will get events in event handler section [event handler section](#event-handler), such as **on receive message, read receipt**, and so on.  

* Application

To start building your application using Qiscus Chat SDK you need a key called APP ID. This APP ID acts as identifier of your Application so that Qiscus Chat SDK can connect a user to other users. You can get your APP ID [here](https://dashboard.qiscus.com/dashboard/login). You can find your APP ID on your Qiscus application dashboard. Here you can see the picture as a reference.

<p align="center"><br/><img src="https://d3p8ijl4igpb16.cloudfront.net/docs/assets/app_id_docs.png" width="100%" /><br/></p>


> **Note**
*All users within the same APP ID are able to communicate with each other, across all platforms. This means users using iOS, Android, Web clients, etc. can all chat with one another. However, users in different Qiscus applications cannot talk to each other.*

* Stage (Sandbox) or Production environment

All created APP ID will be automatically recognised as a trial APP ID with certain periods of time to cease. In order to keep your APP ID active, you may want to upgrade it to a paid plan. By doing so, you can have additional APP ID as a sandbox. Once your APP ID trail is expired we may disable your APP ID from accessing Qiscus Chat SDK. Given that you can upgrade plan to continue your apps accessing Qiscus Chat SDK.


## Try Sample App

In order to help you to get to know with our chat SDK, we have provided a sample app. This sample app is built with full functionalities so that you can figure out the flow and main activities using Qiscus Chat SDK. And you can freely customize your own UI, for further detail you can download [Sample github link](https://bitbucket.org/qiscus/qiscus-sdk-core-android-sample.git). You can also build your own app on top of our sample app

```
git clone https://bitbucket.org/qiscus/qiscus-sdk-core-android-sample.git
```

This sample use **sample APP ID**, means, you will share data with others, in case you want to try by your own you can change the APP ID into your own APP ID, you can find your APP ID in your dashboard. [dashboard link](https://dashboard.qiscus.com/dashboard/login)

## Getting Started

This section help you to start building your integration, start with send your first message.

### Step 1 : Get Your APP ID

Firstly, you need to create your application in dashboard, by accessing [Qiscus Chat Dashboard](https://www.qiscus.com/dashboard/login). You can create more than one APP ID, for further information you can refer to [How Qiscus works - Application Link](#aplication)

### Step 2 : Install Qiscus Chat SDK

Qiscus Chat SDK requires minimum Android API 16 (Jelly Bean). To integrate your app with Qiscus, it can be done in 2 steps. Firstly, you need to add URL reference in your .gradle project. This reference is a guide for **.gradle** to get Qiscus Chat SDK from the right repository. Here is how to do that :

```
allprojects {
    repositories {
        ...
        maven { url  "https://dl.bintray.com/qiscustech/maven" }
    }
}
```

Secondly, you need to add SDK dependencies inside your app .gradle. Then, you need to synchronize to compile the Qiscus Chat SDK for your app.

```
dependencies {
    ...
    implementation 'com.qiscus.sdk:chat-core:3.1.0-beta.2'
}
```

### Step 3 : Initialization Qiscus Chat SDK

You need to initiate your APP ID for your chat App before carry out to Authentication. This initialization only need to be done once in the App lifecycle. Initialization can be implemented in the initial startup. Here is how you can do that:

```
public class SampleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        Qiscus.setup(this, APP_ID);
        
 }
```

> **Note:  
**The initialization should be called once across an Android app . The best practise you can put in Application class 

### Step 4 : Authentication To Qiscus 

To use Qiscus Chat SDK features a user firstly need to authenticate to Qiscus Server, for further detail you might figure out [Authentication section link](#authentication). This authentication is done by calling `setUser()` function. This function will retrieve or create user credential based on the unique **User Id, **for example:

```
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

### Step 5 : Create 1-On-1 Chat Room

There are three Chat Room types, 1-on-1, group, and channel, for further detail you can see [Chat Room type](#chat-room-type) for this section let's use 1-on-1. We assume that you already know a targeted user you want to chat with. To start a conversation with your targeted user, it can be done with **getChatRoom** method. Qiscus Chat SDK, then, will serve you a new Chat Room, asynchronously. When the room is successfully created, Qiscus Chat SDK will return a Chat Room package through `onSuccess()` listener. 

```
QiscusApi.getInstance().getChatRoom(userId, distinctId, options)
        .subscribeOn(Schedulers.io()) //need to run this task on IO thread
        .observeOn(AndroidSchedulers.mainThread()) //deliver result on main thread or UI thread
        .subscribe(qiscusChatRoom -> {
            // on success        
        }, throwable -> {
            // on error        
        });
```

> **Note:  ** Make sure that your targeted user has been registered in Qiscus Chat SDK 

### Step 6 : Send Message

You can send any type of data through Qiscus Chat SDK, in this section let's send a “Hi” **message**, 
with type value is **text** .For further detail about message you can find at [Message section](#message)

* Generate `QiscusComment` **object**, **text** type :

```
QiscusComment qiscuscomment = QiscusComment.generateMessage(roomId, text)
```

* Send a Message:

```
QiscusApi.getInstance().postComment(qiscusComment)
        .subscribeOn(Schedulers.io()) // need to run this task on IO thread
        .observeOn(AndroidSchedulers.mainThread()) // deliver result on main thread or UI thread
        .subscribe(qiscusChatRoom -> {
            // on success        
        }, throwable -> {
            // on error        
        });
```

> Note : You can define type and data freely, you can use it for custom UI purposes



## Authentication 

To use Qiscus Chat SDK features, authentication to Qiscus Server is needed, your application needs to have user credential locally stored for further requests. The credential consists of a token that will identify a user in Qiscus Server. When you want to disconnect from Qiscus server, terminating authentication will be done by clearing the stored credential. 

You need to initiate your APP ID for your chat App before carry out to Authentication. This initialization only need to be done once in the app lifecycle. Initialization can be implemented in the initial startup. Here is how you can do that:

```
QiscusCore.init(application, appId);
```

If you have your own server **(on Premise) **you can change the URL, here's the example 

```
QiscusCore.initCustomServer(application, appId, serverBaseUrl, mqttBrokerUrl);
```

For further detail on premise information you can contact us mailto:contact.us@qiscus.com


> **Note:  
**The initialization should be called once across an Android app . The best practise you can put in Application class 


There are 2 type of authentications that you can choose to use: Client Authentication and Server Authentication

* Client Authentication can be done simply by providing userID and userKey through your client app. On the other hand, Server Authentication, the credential information is provided by your Server App. In this case, you need o prepare your own Backend.
* The Client Authentication is easier to implement but Server Authentication is more secure.

### Client Authentication

This authentication is done by calling `setUser()` function. This function will retrieve or create user credential based on the unique user Id. Here is example:

```
QiscusCore.setUser(userId , userKey)
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

* `userId` (string, unique): A User identifier that will be used to identify a user and used whenever another user need to chat with this user. It can be anything, whether is is user's email, your user database index, etc. HiAs long as it is unique and a string.
* `userKey` (string): userKey for authentication purpose, so even if a stranger knows your user Id, he cannot access the user data.
* `username` (string): Username for display name inside Chat Room purposes.
* `avatarURL` (string, optional): to display user's avatar, fallback to default avatar if not provided.
* `extras`** **(JSON, optional): to give additional information (metadata) to user, which consist key-value, for example **key: position, **and** value: engineer**.** **

You can learn from the figure below to understand what really happened when calling `setUser()` function:

<p align="center"><br/><img src="https://s3-ap-southeast-1.amazonaws.com/qiscus-sdk/docs/assets/docs-screenshot-android/docs_ss_set_user_client_auth.png" width="100%" /><br/></p>

> **Note**
Email addresses are a bad choice for user IDs because users may change their email address. It also unnecessarily exposes private information. We recommend to be *unique* for every user in your app, and *stable*, meaning that they can never change

### Server Authentication (JWT Token)

Server Authentication is another option, which allow you to authenticate using JSON Web Tokens [(JWT)](https://jwt.io/). JSON Web Tokens contains your app account details which typically consists of a single string which contains information of two parts, JOSE Header, JWT Claims Set.

<p align="center"><br/><img src="https://d3p8ijl4igpb16.cloudfront.net/docs/assets/docs-screenshot-android/docs_ss_jwt_authentication.png" width="100%" /><br/></p>

The steps to authenticate with JWT goes like this:

1. Your App request a Nonce from Qiscus Server
2. Qiscus Server send Nonce to Your App
3. Your App send user credentials and Nonce that is obtained from Qiscus Server to Your backend
4. Your backend send the token to Your App
5. Your App send that token to Qiscus Server
6. Qiscus Server send Qiscus Account to Your App


Do the following authentication tasks as described step above:

* Step 1 : Setting JOSE Header and JWT Claim Set in your backend

When your backend returns a JWT after receiving Nonce from your App, the JWT will be caught by your App and will be forwarded to Qiscus Server. In this phase, Qiscus Server will verify the JWT before returning Qiscus Account for your user. To allow Qiscus Server successfully recognize the JWT, you need to setup JOSE Header and JWT Claim Set in your backend as follow :

* JOSE Header

```
{
  "alg": "HS256",  // must be HMAC algorithm
  "typ": "JWT", // must be JWT
  "ver": "v2" // must be v2
}
```

* JWT Claim Set

```
{
  "iss": "QISCUS SDK APP ID", // your qiscus app id, can obtained from dashboard
  "iat": 1502985644, // current timestamp in unix
  "exp": 1502985704, // An arbitrary time in the future when this token should expire. In epoch/unix time. We encourage you to limit 2 minutes
  "nbf": 1502985644, // current timestamp in unix
  "nce": "nonce", // nonce string as Number used Once
  "prn": "YOUR APP USER ID", // your user identity, (userId), should be unique and stable
  "name": "displayname", // optional, string for user display name
  "avatar_url": "" // optional, string url of user avatar
}
```

* Signature

JWT need to be signed using **Qiscus Secret Key**, the one you get in [dashboard](https://dashboard.qiscus.com/dashboard/login). The signature is used to verify that the sender of the JWT is who it says it is. To create the signature part you have to take the encoded JOSE Header, the encoded JWT Claim Set, a Qiscus Secret Key, the algorithm specified in the header, and sign that.

The signature is computed using the following pseudo code :

```
HMACSHA256(
  base64UrlEncode(JOSE Header) + "." +
  base64UrlEncode(JWT Claim Set),
  Qiscus Secret Key)
```

To make this easier, we provide sample backends in [PHP](https://bitbucket.org/qiscus/qiscus-sdk-jwt-sample/src/master/). You can use any other language or platform.

> Note :
JWT Sample backend in PHP can be found by clicking this [link](https://bitbucket.org/qiscus/qiscus-sdk-jwt-sample/src/master/)

* Step 2 : Start to get a **Nonce **

You need to request a Nonce from Qiscus Server. **Nonce (Number Used Once)** is a unique, randomly generated string used to identify a single request. Please be noted that a Nonce will expire in 10 minutes. So you need to implement your code to request JWT from your backend right after you got the returned Nonce. Here's the how to get a Nonce:

```
QiscusApi.getInstance()
        .requestNonce()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(qiscusNonce -> {
            // on success 
        }, throwable -> {
            // on error
         });
```

* Step 3 : Verify The JWT 

Once you get a Nonce, you can request JWT from your backend by sending Nonce you got from Qiscus Server. When you got the JWT Token, you can pass that JWT to `setUser()` method to allow Qiscus to authenticate your user and return Qiscus Account, as shown in the code below:

```
QiscusCore.setUser('your jwt token', new QiscusCore.SetUserListener() {
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

### Clear User Data And Disconnected 

As mentioned in previous section, when you did setUser(), user's data will be stored locally. When you need to disconnect from Qiscus Server, you need to clear the user data that is related to Qiscus Chat SDK, such as token, profile, messages, rooms, etc, from local device, hence later you will not get any **message, or event**.  You can do this by calling this code:

```
QiscusCore.clearUser();
```

## Term Of User

Qiscus Chat SDK has tree user terms, Qiscus Account, Participant, and Blocked User. Qiscus Account is user who success through authentication phase, hence this user able to use Qiscus Chat SDK features. In other hand, Participant is user who in a Chat Room. At some case, you need add more user to your Chat Room, what you can do you can add participant, then your Chat Room increase the number of participant and decrease whenever you remove participant. To use add participant you can refer to this link [add participant](#add-participant-in-chat-room)

Term of user table:

|Type	|Description	|
|---	|---	|
|Qiscus Account	|The user who can use Qiscus Chat SDK features that has been verified in Qiscus Server	|
|Participant	|The user who is in a Chat Room	|
|Blocked user	|The user who is blocked by another user	|

### Blocked User 

Blocked user is user who is blocked by another user. Once a user is blocked they cannot receive message from another user only in 1-on-1 Chat Room, but still get message in Channel or Group Chat Room. Blocked user do not know they are blocked, hence when send a message to a user, blocked user's message indicator stay sent receipt.


> Note :
Block user feature works only for 1-on-1 Chat Room

## Chat Room Type 

### 1-On-1 Chat Room

Chat Room that consist of 1-on-1 chat between two users. This type of chat room allow you to have always same chat room between two users. Header of the room will be name of the pair. To create single chat, you will need to know the user Id of the opponent.

### Group Chat Room

When you want your many users to chat together in a single room, you need to create Group Chat Room. Basically Group Chat Room has the same concept as 1-on-1 Chat Room, but the different is that Group Chat Room will target array of user Id in a single method. The return of the function is `QiscusChatRoom` object that you can store it in your persistent storage and then use it to enter the same room anytime you want. Maximum number of participant for now is : **100** participants

### Channel 

Channel is Chat Room which allow users to join without invitation. This will allow our user to implement our SDK to create Forum, Live Chat in Video Streaming, or Public Channel like in Forum or Telegram. Maximum number of participants in Channel for now : **5000** participants

### Chat Room Type Comparison Table 

|Item	|1-1	|Group	|Channel	|
|---	|---	|---	|---	|
|Number of participant	|2	|100	|5000	|
|Sent Receipt	|v	|v	|-	|
|Delivered Receipt	|v	|v	|-	|
|Read Receipt	|v	|v	|-	|
|Push Notification	|v	|v	|-	|
|Unread Count	|v	|v	|v	|
|Support Chatbot interface	|v	|v	|v	|
|Block User	|v	|-	|-	|
|Adding or  Removing participant	|-	|v	|v	|

## Support RX Java 

Most of API from Qiscus Chat SDK is using RxJava, but no worry, it's easy to use RxJava method. You just need to call **subscribe()** after you calling the method. The main advantage of using RxJava is easy to create asynchronous method, just need add** subscribeOn() **where thread will be used to run the task and **observeOn() **where thread will be used to deliver result. To learn more about RxJava you can visit https://github.com/ReactiveX/RxJava/wiki . This example how to use RxJava method from QiscusApi.

For example get a Nonce:

```
QiscusApi.getInstance().requestNonce();
```

So to execute it you can call it like this:

```
QiscusApi.getInstance().requestNonce()
        .subscribeOn(Schedulers.io()) // need to run this task on IO thread
        .observeOn(AndroidSchedulers.mainThread()) // deliver result on main thread or UI thread
        .subscribe(qiscusNonce -> {
            // on success        
        }, throwable -> {
            // on error        
        });
```

But if you do want to use Rx Java you can use** QiscusRxExecutor**, that will be like this:

```
QiscusRxExecutor.*execute*(QiscusApi.*getInstance*().requestNonce(), new QiscusRxExecutor.Listener<QiscusNonce>() {
    @Override
    public void onSuccess(QiscusNonce qiscusNonce) {
        // on success    
    }

    @Override
    public void onError(Throwable throwable) {
        // on error    
    }
});
```

## User

This section contains user Qiscus Chat SDK behaviour, you can do **update user profile with additional metadata,** **block user**, **unblock user**, and **get list of blocked user.**

### Update User Profile With Metadata

You can update user's data, for example

```
QiscusCore.updateUser(username, avatarUrl, extras, new QiscusCore.SetUserListener() {
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

Where:

* `username`: username of its user, for display name purpose if in 1-on-1 Chat Room
* `avatarUrl` : Url to display user's avatar, fallback to default avatar if not provided.
* `extras` : metadata that can be as additional information to user, which consist key-value, for example **key: position, **and** value: engineer**.** **

### Check Is User Authenticated

You can check whether user is authenticated or not, and make sure that a user allow to use Qiscus Chat SDK features.
When return **true **means user already authenticated, otherwise **false **means user not yet authenticate.

```
QiscusCore.hasSetupUser(); //boolean
```

### Block User

You can block a user with related **user Id** parameter, this block user only works in 1-on-1 Chat Room. When a user in same Group or Channel with blocked user, a user still receive message from blocked user, for further information you can see this link [User - blocked](#user-block). You can use this function by calling this method, for example: 


```
QiscusApi.getInstance().blockUser(userId)
        .subscribeOn(Schedulers.io()) // need to run this task on IO thread
        .observeOn(AndroidSchedulers.mainThread()) // deliver result on main thread or UI thread
        .subscribe(account -> {
            // on success
         }, throwable -> {
            // on error
         });
```

### Unblock User

You can unblock a user with related `userId` parameter. Unblocked user can send a message again into particular Chat Room, for example: 

```
QiscusApi.getInstance().unBlockUser(userId)
```

### Get Blocked User List

You can get blocked user list with pagination, with `page`  parameter and you can set also the `limit` number of blocked users, for example: 

```
QiscusApi.getInstance().getBlockedUsers(page, limit)
```

## Chat Room

This section consist Chat Room Qiscus Chat SDK behaviour In Chat Room you can add additional information called **options. options** is automatically synchronized by each participant in the conversation. It is important that the amount of data stored in **options** is kept to a minimum to ensure the quickest synchronization possible. You can use **options **tag a room for changing background colour purposes, or you can add a latitude or longitude.


> Note
options consist string key-value pairs

### Create 1-On-1 Chat Room With Metadata

The ideal creating 1-on-1 Chat Room is for use cases that require 2 users, for further information you can see this [Chat Room-1-on-1](#1-on-1-chat-room). After success creating a 1-on-1 Chat room, room name is another userId.

```
QiscusApi.getInstance().getChatRoom(userId, distinctId, options);
```

Where:

* `userId`:  A User identifier that will be used to identify a user and used whenever another user need to chat with this user. It can be anything, whether is is user's email, your user database index, etc. As long as it is unique and a string.
* `distinctId`: **(deprecated) **you can fill **“ ” (empty string).**
* `options:` metadata that can be as additional information to Chat Room, which consist key-value, for example **key: background, **and** value: red. **

if you want to save the Chat Room into local data after creating or getting Chat Room from Qiscus server, here's the code:

```
QiscusApi.getInstance().getChatRoom(userId, distinctId, options)
        .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(chatRoom -> {
           // on success         
        }, throwable -> {
            // on error        
        });
```

> Note
We recommend after creating or getting a Chat Room, you need to update to your local data

### Create Group What Room With Metadata

When you want your many users to chat together in a 1-on-1 Chat Room, you need to create Group Chat Room. Basically Group Chat Room has the same concept as 1-on-1 Chat Room, but the different is that Group Chat Room will target array of user Id in a single method. 

```
QiscusApi.getInstance().createGroupChatRoom(name, userIds, avatarUrl, options);
```

Where: 

* `name`: Group name
* `userIds`: List of `user Id`
* `avatarUrl`: avatar url for group Chat Room
* `options`:  metadata that can be as additional information to Chat Room, which consist key-value, for example **key: background, **and** value: red. **

After success creating a Chat Room, you can save Chat Room to local data like this :

```
QiscusCore.getDataStore().addOrUpdate(chatRoom);
```

### Create Or Get Channel With Metadata

The ideal creating Channel Chat Room is for use cases that requires a lot of number of participant. You need set `uniqueId` for identify a Channel Chat Room, If a Chat Room with predefined `unique id `is not exist then it create a new one with requester as the only one participant. Otherwise, if Chat Room with predefined unique id is already exist, it will return that room and add requester as a participant. 

When first call (room is not exist), if requester did not send `avatar_ur`l and/or room `name` it will use default value. But, after the second call (room is exist) and user (requester) send `avatar_url` and/or room `name`, it will be updated to that value.

```
QiscusApi.getInstance().getGroupChatRoom(uniqueId, name, avatarUrl, options);
```

You can get Channel Chat Room from your local data, for example: 

```
QiscusCore.getDataStore().getChatRoomWithUniqueId(uniqueId);
```

### Get Chat Room By Id (Enter Existing Chat Room)

You can enter existing Chat Room by using `roomId` and creating freely your own chat UI. The return as pair of a Chat Room and List of `Comments` that you can use to init data comment for the first time as reference you can see in [sample](https://bitbucket.org/qiscus/qiscus-sdk-core-android-sample). You can use to 1-on-1 Chat Room, Group Chat room or Channel, here's how to get a Chat Room by `roomId:`

```
QiscusApi.getInstance().getChatRoomComments(roomId);
```

### Get Chat Room Opponent By *User_ID*

You can get a Chat Room by `userId`. This only works 1-on-1 Chat Room.

```
QiscusApi.getInstance().getChatRoom(userId, distinctId, options);
```

You can get a Chat Room from your local data, for example:

```
QiscusCore.getDataStore().getChatRoom(userId);
```

### Get Chat Rooms Information

You can get more than one Chat Room, by passing list of `roomId`, for `uniqueIds` will deprecate soon, for now you can set same as `roomIds` . You can see participant for each room by set `showMembers` to **true**, or you can set **false **to hide participant in each room.

```
QiscusApi.getInstance().getChatRooms(roomIds, uniqueIds, showMembers);
```

You can get Chat Rooms from your local data, for example:

```
QiscusCore.getDataStore().getChatRooms(roomIds, uniqueIds);
```

### Get Chat Room List

Get Chat Room list is ideal case for retrieve all Chat Rooms that Qiscus Account has. Showing maximum 50 data per page.

```
QiscusApi.getInstance().getChatRooms(page, limit, showMembers);
```

You can get Chat Room List from your local data, for example:

```
QiscusCore.getDataStore().getChatRooms(limit, offset);

//or only using limit

QiscusCore.getDataStore().getChatRooms(limit);
```

You need to update your local data from Qiscus server , here's the example:

```
QiscusApi.getInstance().getChatRooms(page, limit, showMembers)
    .doOnNext(chatRooms -> {
        for (QiscusChatRoom chatRoom : chatRooms) {
            QiscusCore.getDataStore().addOrUpdate(chatRoom);
        }
    })
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(chatRooms -> {
        //on success
    }, throwable -> {
        //on error
    });
```

### Update Chat Room

You can update your Chat Room metadata, you need `roomId`, your Chat Room `name`, your Chat Room `avatar Url`, and `options`, for example:

```
QiscusApi.getInstance().updateChatRoom(roomId, name, avatarUrl, options);
```

### Get Participant List In Chat Room

You can get participant list in Chat Room, you can get from `QiscusChatRoom` object directly, from your local data, or you can retrieve from Qiscus Server.

This example code you can retrieve from object `QiscusChatRoom`:

```
qiscusChatRoom.getMember();
```

Retrieving local data you need `roomId`, for example:

```
QiscusCore.getDataStore().getRoomMembers(roomId);
```

Retrieving from Qiscus Server, you need `roomUniqueId`, you get default 100 participants, for example:

```
QiscusApi.getInstance().getRoomMembers(roomUniqueId);
```

You can get advance by adding some parameter, for example you can order the list based on either ascending `(asc)` or descending `(desc)`, or you can sort by `username` . 

```
QiscusApi.getInstance().getRoomMembers(roomUniqueId, offset, sorting, userName, metaRoomMembersListener);
```

Here's example to retrieve participant with additional parameters:

```
 QiscusApi.getInstance().getRoomMembers("roomuniqueid", 0, null, null, null, (currentOffset, perPage, total) -> {
                    /**
                     you can get currentOffset, perPage, and total here
                     * currentOffset -> current offset the data
                     * perPage -> limit data = 100
                     * total -> total data
                     */
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusRoomMembers -> {
                    // success: now you can qiscusRoomMembers here
                }, throwable -> {
                    // error :show error here
                });
```


Where:

* `roomUniqueId`:  unique Id each of Chat Room
* `offset`: number of offset
* `sorting`: filtering based on ascending (**asc)** or descending (**desc)**, by default refer to **asc**
* `username`: (**deprecated**) filtering based on participant's username
* `metaRoomMemberListener`:  metadata from the response Qiscus Server, there are, total page, current offset and page

> Note :
Default return 100 participants

### Add Participant In Chat Room

You can add more than a participant in Chat Room by calling this method `addRoomMember`  you can pass multiple `userId` . Once a participant success join the Chat Room, they get new Chat Room in their Chat Room list.

```
QiscusApi.getInstance().addRoomMember(roomId, userId);
```

After success adding participant in Chat Room participant, you can to update Chat Room object to local data, for example:

```
QiscusApi.getInstance().addRoomMember(roomId, userId)
        .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(chatRoom -> {
           // on success
        }, throwable -> {
            //on error        
        });
```

### Remove Participant In Chat Room

You can remove more than a participant in Chat Room by calling this method `removeRoomMember` you can pass multiple `userId` . Once a participant remove from the Chat Room, they will not find related Chat Room in their Chat Room list.

```
QiscusApi.getInstance().removeRoomMember(roomId, userId)
```

After success removing a participant in Chat Room, you can update Chat Room object to local data for keeping update, for example:

```
QiscusApi.getInstance().removeRoomMember(roomId, userId)
        .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(chatRoom -> {
           //success
        }, throwable -> {
            //error        
        });
```

### Get Total Unread Count In Chat Room

You can get total unread count user have in every Chat Room, ideal this case is when you want to show badge icon, for example getting total unread count:

```
QiscusApi.getInstance().getTotalUnreadCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(totalUnreadCount -> {
                //success
            }, throwable -> {
                //error        
            })
```

## Message

This section consist of Message Qiscus Chat SDK behaviour. In Message you can add metadata called **extras**. **extras** is automatically synchronized by each participant in the Chat Room. Qiscus Chat SDK has 3 statues, Sent, Delivered, and Read for a message. Once message is sent, the OnReceiveMessage event handler will be called, you can refer to [event handler section](#event-handler)

### Send Message 

You can send a **text** message or **custom** message **type**. Ideal case for **custom** message is for creating custom UI message needs by sending structured data, such as you need to **send location** message, a **ticket concert **message, a **product** info, and others UI message that need to be customized. You need to create  `QiscusComment` object first before sending it, for example: 

Generate `QiscusComment`** **object, **text** type :

```
QiscusComment qiscuscomment = QiscusComment.generateMessage(roomId, text)
```

Generate `QiscusComment` object, **custom** type :

```
QiscusComment qiscuscomment = QiscusComment.generateCustomMessage(roomId, text, type, content);
```

Where:

* `roomId`:  Chat Room Identity (Id), you can get this Id in `QiscusChatRoom` object 
* `text`: message text that you send to other participant
* `type`: message type, that you can define freely, there are predefined rich messages **type**, **for example: text, file_attachment, account_linking, buttons, button_postback_response, replay, system_event, card, custom, location, contact_person, carousel**. These type have taken, if you use it you may face your structured data will not work, these type for bot API, hence you need define other type name.
* `content`: Payload for defining the structured message data, for example you want to create your own **file** message, you can fill the `content` using this example JSON :

```
{
  "url": "https://d1edrlpyc25xu0.cloudfront.net/sampleapp-65ghcsaysse/docs/upload/2sxErjgAfp/Android-Studio-Shortcuts-You-Need-the-Most-3.pdf",
  "caption": "",
  "file_name": "Android-Studio-Shortcuts-You-Need-the-Most.pdf"
}
```

You can find how to implement this `content` in [Sample](https://bitbucket.org/qiscus/qiscus-sdk-core-android-sample).  Another example `content` you can craft:

```
{
  "cards": [
    {
      "header": {
        "title": "Pizza Bot Customer Support",
        "subtitle": "pizzabot@example.com",
        "imageUrl": "https://goo.gl/aeDtrS",
        "imageStyle": "IMAGE"
      },
    ...
    }
  ]
}
```

You can add **extras** before sending a message, by intercepting the object, for example:

```
QiscusComment qiscuscomment = QiscusComment.generateCustomMessage(roomId, text, type, content;

String stringJson = "{\"key\" : \"value\", \"key2\" : \"value2\"}";
qiscusComment.setExtras(new JSONObject(stringJson));
```

> Note: 
Metadata is automatically synchronized by each participant in the Chat Room, it is important that the amount of data stored in metadata is kept to a minimum to ensure the quickest synchronization possible.


Secondly, you can send a message using `postComment()` method and need a `QiscusComment` as parameter, for example:

```
QiscusApi.getInstance().postComment(qiscusComment)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(commentSent -> {  
                // success
            }, throwable -> {
                // error
            });
}
```

### Update Messsage Read Status

You can set your message status into **read**, the ideal case of this is to notify other participant that a message has **read.**
You need to pass `roomId ` and `commentId`. When you have **10 messages**, and the latest message Id, let say is **10**, once you set **read** message status with the latest message, in this case is **10, **your previous messages will update into **read** as well. You can update message read status by calling `setUserRead` method, for example:

```
QiscusPusherApi.getInstance().setUserRead(roomId, commentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(void -> {  
                // success
            }, throwable -> {
                // error
            });
```

### Load Message (With Limit And Offset)

You can get previous messages by calling `getComments` method, by default you get 20 messages start from your `lastCommentId`, and also you can use this for load more the older messages, for example:

```
QiscusApi.getInstance().getComments(roomId, lastCommentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(void -> {  
                // success
            }, throwable -> {
                // error
            });
```

Where:

* `roomId` : ChatRoom Id
* `lastCommentId`: messageId that you can get from `QiscusComment` object 

You can a get from local data, you can set `limit` to get number of comments, for example:

```
 QiscusCore.getDataStore().getComments(roomId, limit);
```

### Viewing Who Has Read, Delivered A Message

You can get information who has read your message by passing `commentId` in return you get participants who have 
**pending**, **delivered**, and **read **message status, for example:

```
QiscusApi.getInstance().getCommentInfo(commentId);
```

### Upload File

You can send a raw file by passing `file` Qiscus Chat SDK, in return you will get `Uri`  and `progress listener`.  

```
QiscusApi.getInstance().uploadFile(file, progress -> {
   
}) 
```

### Download Media (The *Path *And % Of Process)

You can download file by passing `url` and `file name`, and return you get `progress listener`. You can use this listener to create your own progress UI, for example:

```
QiscusApi.getInstance().downloadFile(url, fileName, totalDownloaded -> {
      // here you can get the progress total file downloaded      
});
```

That code only download the file without saving information to local data. To save data to local data you can use this example:

```
public void downloadFile(QiscusComment qiscusComment) {
    QiscusApi.getInstance()
        .downloadFile(qiscusComment.getAttachmentUri().toString(), qiscusComment.getAttachmentName(), total -> {
            // here you can get the progress total downloaded 
        })
        .doOnNext(file -> {
            // here we update the local path of file
            QiscusCore.getDataStore()
                    .addOrUpdateLocalPath(qiscusComment.getTopicId(), qiscusComment.getId(), file.getAbsolutePath());
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(file -> {
            //on success            
        }, throwable -> {
            //on error            
        });
}
```

### Delete Message

You can delete a message by calling this `deleteComments` method for example:

```
QiscusApi.getInstance().deleteComments(commentUniqueIds, isHardDelete);
```

Where:

* `commentUniqueIds`:  uniqueId in `QiscusComment` object
* `isHardDelete`: set **true **for** **making your message gone in local data as well, and set **false** still remain in local data base

### Clear All Messages 

You can clear all message by passing array of `roomId`  or `roomUniqueIds` this clear all messages only effect `QiscusAccount` side, other participants still remain. For example:

```
 QiscusApi.getInstance().clearCommentsByRoomIds(roomIds)
```

roomUniqueIds you can get in `QiscusChatRoom` object

```
 QiscusApi.getInstance().clearCommentsByRoomUniqueIds(roomUniqueIds)
```

And optional you can delete from your local data as well, for example:

```
QiscusCore.getDataStore().deleteCommentsByRoomId(roomId)
```

## Proguard 

ProGuard is the most popular optimizer for Java bytecode. It makes your Java and Android applications smaller and faster. Read [here](https://www.guardsquare.com/en/proguard) for more detail about Proguard. If you are using Proguard in your application, make sure you add Proguard rules of Qiscus from [Qiscus Proguard Rules](https://github.com/qiscus/qiscus-sdk-android/blob/master/app/proguard-rules.pro) to your Proguard rules.

## Event Handler

Qiscus Chat SDK provides a simple way to let applications publish and listen some real time event. You can publish **typing, read, user status, custom event** and you can handle freely in event handler. This lets you inform users that another participant is actively engaged in communicating with them.

Qiscus Chat SDK is using EventBus for broadcasting event to entire application. You can learn more about EventBus on this website http://greenrobot.org/eventbus/. What you need to do is registering the object which will receive event from EventBus. You can call it like this:

```
EventBus.getDefault().register(this);
```

You need unregister the receiver after you don't need to listen event anymore by calling this method:

```
EventBus.getDefault().unregister(this);
```

This is example how to register an activity for receiving event from EventBus: 

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
        EventBus.getDefault().register(this); // register to EventBus
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this); // unregister from EventBus
    }
}
```

> Note : After you register the receiver, now you can subscribe to specific event you need to know. What you need to do is you must create a method with **org.greenrobot.eventbus.Subscribe** annotation, the method name is up to you, but the parameter in your method must be equal with the event class which you want to listen.

### On Receive Message

Messages can be received through a `OnReceiveComment`  event. This event is triggered whoever sent a message .Class for this event is **com.qiscus.sdk.event.QiscusCommentReceivedEvent, **you** **can create a method that listen with this class type*. *

```
@Subscribe
public void onReceiveComment(QiscusCommentReceivedEvent event) {
    event.getQiscusComment(); // to get the comment
}
```

You can set method name freely, for example: 

```
@Subscribe
public void onGotQiscusMessage(QiscusCommentReceivedEvent event) {
    event.getQiscusComment(); // to get the comment
}
```

The activity class will be like this below:

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
}
```

> *Note: **QiscusCommentReceivedEvent** not guarantee the event only published once per unique comment. So there are will be the same **QiscusCommentReceivedEvent** published, so you need to handle it.*

### Start And Stop Typing Indicator

You can have typing indicator by publish the typing event. You need to pass `roomId` and `typing` status. Set **true **to indicate the `typing` event is active, set **false **to indicate the event is inactive. The ideal of this case is you can put this to any class, for example, you need to put in Homepage, to notify that there's an active user.:

```
QiscusPusherApi.getInstance().setUserTyping(roomId, typing);
```

### On User Typing (With Information On Which Chat Room)

Different from listening `onReceiveComment` event, listening to user typing doesn't automatically started. You need to listen an event on specific room id. We make it like that for performance reason, listening event too many rooms at the same time is not good, we highly recommend to you just listen room event only to active room page. To listening room event you can call method from **QiscusPusherApi** class. You need to pass `qiscusChatRoom` object to define which room you need to listen, for example:

```
QiscusPusherApi.getInstance().listenRoom(qiscusChatRoom);
```

Once you don't need to listen this event, you need to unlisten an by calling this method `unlistenRoom`, for example: 

```
QiscusPusherApi.getInstance().unListenRoom(qiscusChatRoom);
```

After you call listen room method from **QiscusPusherApi** now you can subscribe to Chat Room event class which is **com.qiscus.sdk.event.QiscusChatRoomEvent** same like listen `OnReceiveComment`, for example:

```
@Subscribe
public void onReceiveRoomEvent(QiscusChatRoomEvent roomEvent) {
    if (roomEvent.getEvent() == QiscusChatRoomEvent.Event.TYPING) {
        roomEvent.getRoomId(); // this is the room id        
        roomEvent.getUser(); // this is the qiscus user id        
        roomEvent.isTyping(); // true if the user is typing    
    }
}
```

### Getting Participants Online Status

Ideal case for this is when you want to know whether the participant is **online** or **last active.  **This only for 1-on-1 Chat Room. You can get participant online by calling passing related `userId` , for example: 

```
QiscusPusherApi.getInstance().listenUserStatus("userId");
```

Then create method that subscribe to event with **com.qiscus.sdk.event.QiscusUserStatusEvent** class

```
@Subscribe
public void onUserStatusChanged(QiscusUserStatusEvent event) {
    event.getUser(); // this is the qiscus user id    
    event.isOnline(); // true if user is online    
    event.getLastActive(); // Date of last active user
}
```

Once you don't need this event, you need to unlisten by calling this method: 

```
QiscusPusherApi.getInstance().unListenUserStatus("userId");
```

### Custom Realtime Event

You can publish and listen any events such as when **participant is listening music**, **writing document, **and many other case that you need to tell an event to other participant in a Chat Room. 

Firstly you need passing `roomId` which ChatRoom you want to set, and the structured `data` for defining what event you want to send. Example of structured `data` of **writing document** event:

```
{
  "sender": "John Doe",
  "event": "writing document...",
  "active": "true"
}
```

Then you can send event using this following method `setEvent:`

```
QiscusPusherApi.getInstance().setEvent(long roomId, JSONObject data)
```

If you need to stop telling other participant that event is ended, you can send a flag to be **false** inside your structured data, for example:

```
{
  "sender": "John Doe",
  "event": "writing document...",
  "active": "false"
}
```

After sending an event, then you need to listen the event with related `roomId`,  for example:

```
QiscusPusherApi.getInstance().listenEvent(roomId);
```

To retrieve event `data`, you can use EventBus by using **@Subscribe** annotation, for example: 

```
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

You need unlisten the event with related `roomId`, for example:

```
QiscusPusherApi.getInstance().unlistenEvent(roomId);
```

### On Message Status Change

After you listen some of events in a ChatRoom, You can receive the real time message status which defined by the event, such as **typing**, **delivered**, **read** and **custom,** for example: 

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
            case CUSTOM:
                //here, you can listen custom event
                roomEvent.getRoomId(); // this is the room id
                roomEvent.getUser(); // this is the qiscus user id
                roomEvent.getEventData(); //event data (JSON)
                break;
        }
}
```

This is an complete version how to use Qiscus Event Handler, for example:

```
public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_my);

        // listen room event
        QiscusPusherApi.getInstance().listenRoom(qiscusChatRoom);

        // listen user status
        QiscusPusherApi.getInstance().listenUserStatus("userId");
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
        QiscusPusherApi.getInstance().unListenRoom(qiscusChatRoom);

        // stop listening user status
        QiscusPusherApi.getInstance().unListenUserStatus("qiscus_user_id");
    }
}
```

## Push Notification

The Qiscus Chat SDK receives pushes through both the Qiscus Chat SDK protocol and Firebase Cloud Messaging (FCM), depending on usage and other conditions. Default notification sent by Qiscus Chat SDK protocol. In order to enable your application to receive FCM push notifications, some setup must be performed in both the Firebase Developer Console and the Qiscus Dashboard.

Do the following steps to setup push notifications:

1. Setup Firebase to your Android app 
2. Get FCM Secret Key in Firebase Console
3. Setup FCM Server key in the Qiscus Chat SDK Dashboard
4. Register your FCM token in the Qiscus Chat SDK   
5. Handling incoming Message from Push Notification

### Step 1: Setup Firebase To Your Android App

If you already have setup firebase in your Android App, you can skip this step and go to next step **Generate FCM Secret key**. Otherwise you can setup firebase to your Android App by following this steps https://firebase.google.com/docs/cloud-messaging/android/client.

### Step 2:  Get FCM Secret Key In Firebase Console

You can get FCM Secret Key by following these steps:

* Go to [Firebase Console](https://console.firebase.google.com/)
* Click your **projects**, to see the overview your project

<p align="center"><br/><img src="https://s3-ap-southeast-1.amazonaws.com/qiscus-sdk/docs/assets/docs-screenshot-android/docs_ss_welcome_firebase.png" width="100%" /><br/></p>

* On the top of left panel, click the** gear icon** on right of project overview menu. From the drop-down menu, click **Project Settings**. 

<p align="center"><br/><img src="https://s3-ap-southeast-1.amazonaws.com/qiscus-sdk/docs/assets/docs-screenshot-android/docs_ss_setting_firebase.png" width="100%" /><br/></p>


* Click the **Cloud Messaging** tab under **Settings**. Under **Project Credentials**, copy your **Server Key**

<p align="center"><br/><img src="https://s3-ap-southeast-1.amazonaws.com/qiscus-sdk/docs/assets/docs-screenshot-android/docs_ss_server_key_docs.png" width="100%" /><br/></p>

### Step 3: Setup FCM Server Key In The Qiscus Dashboard

You can set FCM Secret Key by following these steps:

* Go to your Qiscus Chat Dashboard [dashboard link](https://dashboard.qiscus.com/dashboard/login)  
* Click **Settings **for adding or deleting FCM Secret Key


<p align="center"><br/><img src="https://s3-ap-southeast-1.amazonaws.com/qiscus-sdk/docs/assets/docs-screenshot-android/docs_ss_qiscus_chat_dashboard_fcm_secret_key_add.png" width="100%" /><br/></p>

* In the FCM Secret Keys section, click **+Add **to add your FCM Secret Key
* Paste FCM Secret Key value and click **Save changes**


<p align="center"><br/><img src="https://s3-ap-southeast-1.amazonaws.com/qiscus-sdk/docs/assets/docs-screenshot-android/docs_ss_qiscus_chat_dashboard_save_changes_docs.png" width="100%" /><br/></p>

### Step 4: Register Your FCM Token In The Qiscus Chat SDK

*  Firstly, you need to enable FCM for your Application, you need to enable FCM, for example: 

```
QiscusCore.getChatConfig().setEnableFcmPushNotification(true); // default is **false**
```

* Enable FCM in **ChatConfig, **you need register FCM token to notify Qiscus Chat SDK, for example:  

```
public class AppFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        
        // Notify Qiscus Chat SDK about FCM token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        QiscusCore.setFcmToken(refreshedToken);
        
        //TODO : Application part here, maybe you need to send FCM token to your Backend
    }
}
```

* Add the service to your Manifest, for example: 

```
<service android:name=".service.AppFirebaseInstanceIdService">
    <intent-filter>
        <action android:name="com.google.firebase.INSTANCE_ID_EVENT" />
    </intent-filter>
</service>
```

* Add the service.AppFirebaseMessagingService in Manifest as well, for example: 

```
<service android:name=".service.AppFirebaseMessagingService">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### Step 5: Handle Incoming Message From Push Notification

After registering your FCM token, you will get data from FCM Qiscus Chat SDK, you can handle by  using `handleMessageReceived()` method, for example: 

```
public class AppFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Qiscus will handle incoming message
        if (QiscusFirebaseMessagingUtil.handleMessageReceived(remoteMessage)) {
            return;
        }

        // TODO : here is handle for your notification

    }
}
```

## Change Log 

You can see the change log by clicking this [link](https://github.com/qiscus/qiscus-sdk-android/releases)

## On Premise 

Qiscus Chat SDK is available to be deployed on premise option. For further information you might contact at [contact.us@qiscus.com](mailto:contact.us@qiscus.com.)

## Support  

If you are facing any issue in the Qiscus Chat SDK then you can contact us and share as much information as you can. 
Firstly, you can enable the **debugger **to get the logs, we recommend to use these debugger only in development environment. You can enable or disable the **debugger **using `setEnableLog` method for example: 

```
QiscusCore.getChatConfig().setEnableLog(enableLog);
```

Then, you can sent the inquiries in our support platform https://support.qiscus.com/hc/en-us/requests/new with information that you have.


> Note: Enable debugger only in development environment

