# Getting Started

## Requirement
To integrate your app with Qiscus Chat SDK, it can be done in 2 steps. Firstly, you need to add URL reference in your .gradle project. This reference is a guide for .gradle to get Qiscus Chat SDK from the right repository. Here is how to do that :

```java
allprojects {
    repositories {
        .....
        maven { url  "https://dl.bintray.com/qiscustech/maven" }
    }
}

Secondly, you need to add SDK dependencies inside your app .gradle. Then, you need to synchronize to compile the Qiscus Chat SDK for your app. 

dependencies {
    compile 'com.qiscus.sdk:chat:2.15.1'
}
```

## Get your app id

To start building app using Qiscus  Chat SDK you need a key called APP ID. This APP ID acts as identifier of your Application so that Qiscus can connect user to other users on the sample APP ID. You can get your APP ID [here](https://www.qiscus.com/dashboard/register).
You can find your APP ID on your Qiscus app dashboard. Here you can see the picture as a reference.

> *All users within the same APP ID are able to communicate with each other, across all platforms. This means users using iOS, Android, Web clients, etc. can all chat with one another. However, users in different Qiscus applications cannot talk to each other.*

## Authentication

To authenticate to SDK server, app needs to have user credential locally stored for further requests. The credential consists of a token that will identify a user in SDK server.
When you want to disconnect from SDK server, terminating authentication will be done by clearing the stored credential.
Qiscus SDK authentication can be done separately with your main app authentication, especially if your main app has functionality before the messaging features. 
There are 2 type of authentication that you can opt to use: Basic Authentication and Authentication using JWT.
Here somet comparison to help you decide between the two options:

* Basic Authentication can be done simply by providing userID and userKey through your client app. On the other hand, JWT authentication, the credential information is provided by your Server App. In this case, you need o prepare your own Backend. 
* The Basic Authentication is easier to implement but JWT Authentication is more secure.

### Configuration

After successfully installing Qiscus SDK, you need to first initiate your app id for your chat app before accry out to User Authentication. This initialization only need to be done once in the app lifecycle.

Qiscus.init(context, APP_ID);

Initialization can be implemented in the initial startup. Here is how you can do that:

```java
public class SampleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Qiscus.init(this, "APP_ID");
    }
}
```

### Basic Authentication

This authentication is done by calling `Qiscus.setUser()` function. This function will retrieve or create user credential based on the unique UserId. Here is example:

```java
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

Here are the explanation for the parameters on user setup:

* ***userId*** (string, unique): A User identifier that will be used to identify a user and used whenever another user need to chat with this user. It can be anything, wheter is is user's email, your user database index, etc. As long as it is unique and a string.
* ***userKey*** (string): userKey is used as for authentication purpose, so even if a stranger knows your userId, he cannot access the user data.
* ***username*** (string): Username is used as a display name inside chat room.
* ***avatarURL*** (string, optional): used to display user's avatar, fallback to default avatar if not provided.

You can learn from the figure below to understand what really happened when calling `setUser()` function:

<p align="center"><br/><img src="https://raw.githubusercontent.com/qiscus/qiscus-sdk-android/develop/screenshot/set_user.png" width="80%" /><br/></p>

### JWT Authentication

Another option is to authenticate using Json Web Token (JWT) (read [here ](https://jwt.io/)for more detail about JWT). Json web token contains your app account details which typically consists of a single string which contains information of two parts, Jose header and JWT claims set. 

The steps to authenticate with JWT goes like this:

1. The Client App request a nonce from Qiscus SDK server
2. Qiscus SDK Server will send Nonce to client app
3. Client App send user credentials and Nonce that is obtained from Qiscus SDK Server to Client app backend
4. The Client App backend will send the token to client app
5. The Client App send that token to Qiscus Chat SDK
6. Qiscus Chat SDK send Qiscus Account to Client app

<p align="center"><br/><img src="https://raw.githubusercontent.com/qiscus/qiscus-sdk-android/develop/screenshot/jwt.png" width="80%" /><br/></p>

You need to request Nonce from Qiscus Chat SDK Server. Nonce (Number Used Once) is a unique, randomly generated string used to identify a single request. Please be noted that a Nonce will expire in 10 minutes. So you need to implement your code to request JWT from your backend right after you got the returned Nonce. Here is how to authenticate to Qiscus Chat SDK using JWT :

```java
QiscusRxExecutor.execute(QiscusApi.getInstance().requestNonce(), new QiscusRxExecutor.Listener<QiscusNonce>() {
        @Override
        public void onSuccess(QiscusNonce qiscusNonce) {
            //do request jwt from your backend using returned Nonce
        }

        @Override
        public void onError(Throwable throwable) {
            //do anything if error occurred 
        }
    });
```

The code above is a sample of method you can implement in your app. By calling `QiscusRxExecutor.execute(QiscusApi.getInstance().requestNonce()` , you will request Nonce from Qiscus SDK server and a Nonce will be returned. If it is success, you can request JWT from your backend by sending Nonce you got from Qiscus SDK Server. 
When you got the JWT Token, you can pass that JWTto` Qiscus.setUser()` method to allow Qiscus to authenticate your user and return user account, as shown in the code below :

```java
Qiscus.setUser('your jwt token', new Qiscus.SetUserListener() {
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

### Setting jose header and jwt claim set in your backend

When your backend returns a JWT after receiving Nonce from your client app, the JWT will be caught by client app and will be forwarded to Qiscus Chat SDK Server. In this phase, Qiscus Chat SDK Server will verify the JWT before returning Qiscus Account for your user. To allow Qiscus Chat SDK Server successfully recognize the JWT, you need to setup Jose Header and JWT claim set in your backend as follow :

**Jose Header :**

```
{
  "alg": "HS256",  // must be HMAC algorithm
  "typ": "JWT", // must be JWT
  "ver": "v2" // must be v2
}
```

**JWT Claim Set :**

```
{
  "iss": "QISCUS SDK APP ID", // your qiscus app id, can obtained from dashboard
  "iat": 1502985644, // current timestamp in unix
  "exp": 1502985704, // An arbitrary time in the future when this token should expire. In epoch/unix time. We encourage you to limit 2 minutes
  "nbf": 1502985644, // current timestamp in unix
  "nce": "nonce", // nonce string as mentioned above
  "prn": "YOUR APP USER ID", // your user identity such as email or id, should be unique and stable
  "name": "displayname", // optional, string for user display name
  "avatar_url": "" // optional, string url of user avatar
}
```

### Updating User Profile

After your user account is created, sometimes you may need to update a user information, such as changing user avatar. You can use method `Qiscus.updateUser()` to make changes to your account.


```java
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

### Clear User Data and disconnect

As mentioned in previous section, when you did setUser(), user's data will be stored locally. When user need to disconnect from Qiscus Chat SDK service, you need to clear the user data that is related to Qiscus Chat SDK, such as token, profile, messages, rooms, etc, from local device. You can do this by calling clearUser() method :

```java
Qiscus.clearUser();
```

## Create Chat Room

**Chat Room** is a place where 2 or more users can chat each other. There are 2 type of Chat Room that can be created using Qiscus Chat SDK: 1-on-1 Chat Room and Group Chat Room. For some cases, a room can be identified by room unique id or room name. 

## 1-on-1 Chat Room

We assume that you already know a targeted user you want to chat with. Make sure that your targeted user has been registered in Qiscus Chat SDK through setUser() method, as explained in the previous section. To start a conversation with your targeted user, it can be done with `buildChatWith("targeted_userID")` method. Qiscus Chat SDK, then, will serve you a new Chat Room, asynchronously. When the room is succesfully created, Qiscus Chat SDK will return a Chat Room package through `onSuccess()` listener. To use the created room, you can call `startActivity()` inside the `onSuccess()` listener.   
Here is the example to start a conversation:

```java
Qiscus.buildChatWith("jhon.doe@gmail.com") //here we use email as userID. But you can make it whatever you want.
      .build(this, new Qiscus.ChatActivityBuilderListener() {
          @Override
          public void onSuccess(Intent intent) {
              startActivity(intent);
          }
          @Override
          public void onError(Throwable throwable) {
             //do anything if error occurs 
          }
      });
```

## Group Chat Room

When you want your many users to chat together in a single room, you need to create Group Room. Basically Group Room has the same concept as 1-on-1 Chat Room, but the different is that Group Room will target array of userID in a single method. Here how you can create Group Room:

```java
Qiscus.buildGroupChatRoom("GroupName", Arrays.asList("user1@gmail.com", "user2@gmail.com", "user3@gmail.com"))
      .withAvatar("http://avatar.url.com/group.jpg")
      .build(new Qiscus.ChatBuilderListener() {
          @Override
          public void onSuccess(QiscusChatRoom qiscusChatRoom) {
              startActivity(QiscusGroupChatActivity.generateIntent(MainActivity.this, qiscusChatRoom));
          }

          @Override
          public void onError(Throwable throwable) {
              //do anything if error occurs
          }
       });
```

## Room Participant Management

In some cases, you may need to add additional participants into your room chat or even removing any participant. Currently, Qiscus Chat SDK only allow you to manage your users server to server. You cannot do it on your client app side. Hence, we recommend to invite and remove user out of specific room through our [**SERVER API**](https://www.qiscus.com/docs/restapi) for simplicity and security reason. You can learn how to use Server API [here](https://www.qiscus.com/docs/restapi). 

### Get List of Rooms

To get all room list you can call QiscusApi.getInstance().getChatRooms(int page, int limit, boolean showMembers), page start from 1, limit indicate the max rooms per page, showMembers is flag for load room members also or not. Here sample code:

```java
QiscusRxExecutor.execute(QiscusApi.getInstance().getChatRooms(1, 20, true), new QiscusRxExecutor.Listener<List<QiscusChatRoom>>() {
        @Override
        public void onSuccess(List<QiscusChatRoom> qiscusChatRooms) {
            //Success getting the rooms
        }

        @Override
        public void onError(Throwable throwable) {
            //Something went wrong
        }
    });
```
## More About Rooms

After successfully creating your room, you may need to do advanced development for your chat app. This may include inviting more participant to your room, entering a specific room without invitation, and so on. Hence, in this section you will learn about the following things :

1. **Get Room List**, to get data of your user list so that you can use that data to load a specific room or many more.
2. **Enter to Existing Room**, to enable you to open a room that you have already created by passing a room ID that is obtained by Get Room List.
3. **Participant Management**, to educate you on adding more participants to your room or managing users in your room.

### Get room list

To get all room list you can call ```QiscusApi.getInstance().getChatRooms(int page, int limit, boolean showMembers)```, page start from 1, limit indicate the max rooms per page, showMembers is flag for load room members also or not. Here sample code:

```java
QiscusRxExecutor.execute(QiscusApi.getInstance().getChatRooms(1, 10, true), 
 new QiscusRxExecutor.Listener<List<QiscusChatRoom>>() {
 @Override
 public void onSuccess(List<QiscusChatRoom> chatRoomList) {
    //success get chat room list   
 }

 @Override
 public void onError(Throwable throwable) {
    //error get chat room list
 }
 });
```

After executing the code above, here is what you will get in return :
```java
protected int id;
 protected String distinctId;
 protected String name;
 protected String subtitle = "";
 protected int lastTopicId;
 protected JSONObject options;
 protected boolean group;
 protected String avatarUrl;
 protected List<QiscusRoomMember> member;
 protected int unreadCount;
 protected QiscusComment lastComment;
```
### Enter to Existing Room

After successfully getting your room list, you may want to enter an existing room. Remember that there are 2 type of rooms, 1-on-1 Chat Room and Group Room. Qiscus Android Chat SDK uses API to allow you get existing room. Here is how to do that:

```java
    QiscusRxExecutor.execute(QiscusApi
                    .getInstance().getChatRoom(roomID),
                    new QiscusRxExecutor.Listener<QiscusChatRoom>() {
                        @Override
                        public void onSuccess(QiscusChatRoom qiscusChatRoom) {
                            context.startActivity(QiscusGroupChatActivity.
                                    generateIntent(context, qiscusChatRoom));
                        }
                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
        }
```

### Participant Management

In some cases, you may need to add additional participants into your room chat or even removing any participant. Currently, Qiscus Chat SDK only allow you to manage your users server to server. You cannot do it on your client app side. Hence, we recommend to invite and remove user out of specific room through our [**SERVER API**](https://www.qiscus.com/docs/restapi) for simplicity and security reason. You can learn how to use Server API here. 


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
        Qiscus.setFcmToken(refreshedToken);

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
