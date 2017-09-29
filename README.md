# Qiscus SDK Android

<p align="center"><br/><img src="https://raw.githubusercontent.com/qiscus/qiscus-sdk-android/master/screenshot/device-2017-06-02-093226.png" width="37%" /><br/></p>

# Quick Start

### Create a new app

Register on [https://www.qiscus.com/dashboard](https://www.qiscus.com/dashboard) using your email and password and then create new application

You should create one application per service, regardless of the platform. For example, an app released in both Android and iOS would require only one application to be created in the Dashboard.

All users within the same Qiscus application are able to communicate with each other, across all platforms. This means users using iOS, Android, web clients, etc. can all chat with one another. However, users in different Qiscus applications cannot talk to each other.

Done! Now you can use the APP_ID into your apps and get chat functionality by implementing Qiscus into your app.



### integrating SDK with an existing app

Add to your project build.gradle


```groovy
allprojects {
    repositories {
        .....
        maven { url  "https://dl.bintray.com/qiscustech/maven" }
    }
}
```

Then add to your app module build.gradle


```groovy
dependencies {
    compile 'com.qiscus.sdk:chat:2.13.2'
}
```

# Authentication

### Init with APP ID

Init Qiscus at your application class with your application ID

```java
public class SampleApps extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Qiscus.init(this, "APP_ID");
    }
}
```

### Setup user using userId and userKey
Before user can start chatting each other, they must login to qiscus engine.

```java
Qiscus.setUser("user@email.com", "userKey")
      .withUsername("Tony Stark")
      .withAvatarUrl("http://avatar.url.com/handsome.jpg")
      .save(new Qiscus.SetUserListener() {
          @Override
          public void onSuccess(QiscusAccount qiscusAccount) {
              DataManager.saveQiscusAccount(qiscusAccount);
              startActivity(new Intent(this, ConsultationListActivity.class));
          }
          @Override
          public void onError(Throwable throwable) {
              if (throwable instanceof HttpException) { //Error response from server
                  HttpException e = (HttpException) throwable;
                  try {
                      String errorMessage = e.response().errorBody().string();
                      Log.e(TAG, errorMessage);
                      showError(errorMessage);
                  } catch (IOException e1) {
                      e1.printStackTrace();
                  }
              } else if (throwable instanceof IOException) { //Error from network
                  showError("Can not connect to qiscus server!");
              } else { //Unknown error
                  showError("Unexpected error!");
              }
          }
      });
```

### Setup user using JWT Token
Another alternative is using jwt token. Using this authorization schema, you can only save your user unique identity such as email in your back-end server. You will no need to save two password, one for Qiscus SDK and one for your authorization logic. All you need is generating identity token using JWT in order to login or register an user.

First, you need to get nonce using **QiscusApi.requestNonce()** method. You do not need to send any parameter. Nonce will be expired 10 minutes after request. Afterwards, in your application back-end server you must generate the JWT token using this:

**JOSE header**
```
{
  "alg": "HS256",  // must be HMAC algorithm
  "typ": "JWT", // must be JWT
  "ver": "v2" // must be v2
}
```
**JWT claim set**
```
{
  "iss": "QISCUS SDK APP ID", // your qiscus app id, can obtained from dashboard
  "iat": 1502985644, // current timestamp in unix
  "exp": 1502985704, // An arbitrary time in the future when this token should expire. In epoch/unix time. We encourage you to limit 2 minutes
  "nbf": 1502985644, // current timestamp in unix
  "nce": "nonce", // nonce string from nonce API
  "prn": "sharklaser@mailinator.com", // your user identity such as email
  "name": "shark laser", // optional, string for user name
  "avatar_url": "" // optional, string url of user avatar
}
```
Above JOSE header and claim set must be signed using QISCUS SDK SECRET key that you can get from dashboard with algorithm HMAC (HS256, HS384 or HS512). Then you can verify your identity token using **Qiscus.setUser(String token, Qiscus.SetUserListener listener)** method or **Qiscus.setUserAsObservable(String token)** method if you want to using RxJava. Here sample code how to set user using jwt token.

```java
QiscusApi.getInstance().requestNonce() //Request nonce from qiscus api
        .flatMap(nonce -> YourAppApi.getInstance().getJwtToken(nonce)) //Get jwt token from your backend api
        .flatMap(Qiscus::setUserAsObservable) //Set qiscus user with the jwt token
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(qiscusAccount -> {
              DataManager.saveQiscusAccount(qiscusAccount);
              startActivity(new Intent(this, ConsultationListActivity.class));
        }, throwable -> {
              if (throwable instanceof HttpException) { //Error response from server
                  HttpException e = (HttpException) throwable;
                  try {
                      String errorMessage = e.response().errorBody().string();
                      Log.e(TAG, errorMessage);
                      showError(errorMessage);
                  } catch (IOException e1) {
                      e1.printStackTrace();
                  }
              } else if (throwable instanceof IOException) { //Error from network
                  showError("Can not connect to qiscus server!");
              } else { //Unknown error
                  showError("Unexpected error!");
              }
        });
```

### Updating a User Profile and Avatar

Updating user profile calls Qiscus.updateUser(name, avatar, listener) :

```java
Qiscus.updateUser("Tony Stark", "http://avatar.url.com/handsome.jpg", new Qiscus.SetUserListener() {
            @Override
            public void onSuccess(QiscusAccount qiscusAccount) {
                DataManager.saveQiscusAccount(qiscusAccount);
                startActivity(new Intent(this, ConsultationListActivity.class));
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable instanceof HttpException) { //Error response from server
                    HttpException e = (HttpException) throwable;
                    try {
                        String errorMessage = e.response().errorBody().string();
                        Log.e(TAG, errorMessage);
                        showError(errorMessage);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else if (throwable instanceof IOException) { //Error from network
                    showError("Can not connect to qiscus server!");
                } else { //Unknown error
                    showError("Unexpected error!");
                }
            }
        });
```

### Disconnect or Logout

Whenever you no longer want the user to receive update 

```java
Qiscus.clearUser();
```



# Chat Rooms 

### Creating 1-to-1 chat

Start chat with target is very easy, all you need is just call

```java
Qiscus.buildChatWith("jhon.doe@gmail.com")
      .withTitle("Jhon Doe")
      .build(this, new Qiscus.ChatActivityBuilderListener() {
          @Override
          public void onSuccess(Intent intent) {
              startActivity(intent);
          }
          @Override
          public void onError(Throwable throwable) {
              if (throwable instanceof HttpException) { //Error response from server
                  HttpException e = (HttpException) throwable;
                  try {
                      String errorMessage = e.response().errorBody().string();
                      Log.e(TAG, errorMessage);
                      showError(errorMessage);
                  } catch (IOException e1) {
                      e1.printStackTrace();
                  }
              } else if (throwable instanceof IOException) { //Error from network
                  showError("Can not connect to qiscus server!");
              } else { //Unknown error
                  showError("Unexpected error!");
              }
          }
      });
```


### Creating a Group Room

Qiscus also support group chat. To create new group chat, all you need is just call


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
              if (throwable instanceof HttpException) { //Error response from server
                  HttpException e = (HttpException) throwable;
                  try {
                      String errorMessage = e.response().errorBody().string();
                      Log.e(TAG, errorMessage);
                      showError(errorMessage);
                  } catch (IOException e1) {
                      e1.printStackTrace();
                  }
              } else if (throwable instanceof IOException) { //Error from network
                  showError("Can not connect to qiscus server!");
              } else { //Unknown error
                  showError("Unexpected error!");
              }
          }
       });
```

for accesing room that created by this call, you need to call it with its roomId. This methode is always creating new chat room.


### Get a room by room id


When you already know your chat room id, you can easily go to that room. Just call

```java
QiscusApi.getChatRoom(int roomId);
```

For example :

```java
QiscusApi.getInstance()
        .getChatRoom(123)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(qiscusChatRoom -> QiscusGroupChatActivity.generateIntent(this, qiscusChatRoom))
        .subscribe(this::startActivity, throwable -> {
              if (throwable instanceof HttpException) { //Error response from server
                  HttpException e = (HttpException) throwable;
                  try {
                      String errorMessage = e.response().errorBody().string();
                      Log.e(TAG, errorMessage);
                      showError(errorMessage);
                  } catch (IOException e1) {
                      e1.printStackTrace();
                  }
              } else if (throwable instanceof IOException) { //Error from network
                  showError("Can not connect to qiscus server!");
              } else { //Unknown error
                  showError("Unexpected error!");
              }
        });
```

### Create or join room by defined id

You probably want to set defined id for the room you are creating so that the id can be reference for users to get into.

Usual usage for this is when user create common room or channel which expecting other users can join to the same channel by knowing the channel name or id, you can use the channel name or id as qiscus room defined id.

Additional note: 
If room with predefined unique id is not exist then it will create a new one with requester as the only one participant. Otherwise, if room with predefined unique id is already exist, it will return that room and add requester as a participant.

When first call (room is not exist), if requester did not send avatar_url and/or room name it will use default value. But, after the second call (room is exist) and user (requester) send avatar_url and/or room name, it will be updated to that value. Object changed will be true in first call and when avatar_url or room name is updated.

```java
Qiscus.buildGroupChatRoomWith("UniqueId")
        .withName("RoomName")
        .withAvatar("http://avatar.url.com/group.jpg")
        .build(new Qiscus.ChatBuilderListener() {
            @Override
            public void onSuccess(QiscusChatRoom qiscusChatRoom) {
                startActivity(QiscusGroupChatActivity.generateIntent(MainActivity.this, qiscusChatRoom));
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable instanceof HttpException) { //Error response from server
                    HttpException e = (HttpException) throwable;
                    try {
                        String errorMessage = e.response().errorBody().string();
                        Log.e(TAG, errorMessage);
                        showError(errorMessage);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else if (throwable instanceof IOException) { //Error from network
                    showError("Can not connect to qiscus server!");
                } else { //Unknown error
                    showError("Unexpected error!");
                }
            }
        });
```

### Inviting users to an existing Room

Currently we recommend to invite user into existing room through our [**REST API**](https://www.qiscus.com/docs/restapi) for simplicity and security reason

### Leaving a Group Room

Currently we recommend to kick user out of specific room through our [**REST API**](https://www.qiscus.com/docs/restapi) for simplicity and security reason

# Get room list
To get all room list you can call QiscusApi.getInstance().getChatRooms(int page, int limit, boolean showMembers), page start from 1, limit indicate the max rooms per page, showMembers is flag for load room members also or not. Here sample code:
```java
QiscusApi.getInstance().getChatRooms(1, 20, true)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(qiscusChatRooms -> {
            //qiscusChatRooms is list of rooms result.
        }, throwable -> {
            //Something went wrong
        });
```

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

# UI Customization

### Theme Customization

Boring with default template? You can customized it, try it!, we have more items than those below code, its just example.

```java
Qiscus.getChatConfig()
      .setStatusBarColor(R.color.blue)
      .setAppBarColor(R.color.red)
      .setTitleColor(R.color.white)
      .setLeftBubbleColor(R.color.green)
      .setRightBubbleColor(R.color.yellow)
      .setRightBubbleTextColor(R.color.white)
      .setRightBubbleTimeColor(R.color.grey)
      .setTimeFormat(date -> new SimpleDateFormat("HH:mm").format(date));
```



### UI Source code

If you want full customisations, you can modify everything on the view by forking our repository or just right away modifying our **[CustomChatActivity.java](https://github.com/qiscus/qiscus-sdk-android/blob/develop/app/src/main/java/com/qiscus/dragonfly/CustomChatActivity.java) **based on your needs.

# Push Notifications 

Currently we recommend to use our Webhook-API to push notification from your own server to client app for simplicity and flexibility handling

# Offline Messages

## Post Messages

During post message, if you dont have any internet connection, message will be store locally and will be automatically being send once your internet connection is back. 

## Get Messages

Messages are stored locally so you can still access the messages when you dont have internet conenction. However any new messages will not being received after you have your internet connection back.

# Search Messages
For searching message you can call QiscusApi.getInstance().searchComments(query, roomId, lastCommentId) method for searching message on specific room or QiscusApi.getInstance().searchComments(query, lastCommentId) without roomId. Here sample code:
```java
QiscusApi.getInstance().searchComments("some query", 123, 0)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(qiscusComments -> {
            //qiscusComments is list of messages result.
        }, throwable -> {
            //Something went wrong
        });
```
And if user click the comment, you can open the chat room using Qiscus Api get room by Id, and start with passing the comment to make activity automatically scroll to it. Here sample code:
```java
QiscusApi.getInstance()
        .getChatRoom(commentSearchClicked.getRoomId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(qiscusChatRoom ->
                QiscusChatActivity.generateIntent(this, qiscusChatRoom, null,
                        null, false, null, commentSearchClicked))
        .subscribe(this::startActivity, throwable -> {
            //Something went wrong
        });
```


# Miscellaneous

### Android Support Libraries
Qiscus SDK is using appcompat libraries to support some features. If your apps using appcompat too, we highly recommended to using the latest stable appcompat version, or using the same version with Qiscus SDK. You can check the appcompat version of Qiscus SDK [here](https://github.com/qiscus/qiscus-sdk-android/blob/master/chat/build.gradle#L102). You can also force Qiscus SDK to use your apps appcompat verion. Use "exclude group" at your build.gradle, for example:

```groovy
//Qiscus sdk without android support libraries
compile('com.qiscus.sdk:chat:2.13.2') {
    transitive = true
    exclude group: 'com.android.support'
}

//Qiscus sdk needs all of this android support libraries
//Just add the same version with your apps dependencies
compile 'com.android.support:support-v4:yourVersion'
compile 'com.android.support:appcompat-v7:yourVersion'
compile 'com.android.support:recyclerview-v7:yourVersion'
compile 'com.android.support:cardview-v7:yourVersion'
compile 'com.android.support:design:yourVersion'
compile 'com.android.support:customtabs:yourVersion'
compile 'com.android.support:support-v13:yourVersion'
```

### RxJava support

```java
// Setup qiscus account with rxjava example
Qiscus.setUser("user@email.com", "password")
      .withUsername("Tony Stark")
      .withAvatarUrl("http://avatar.url.com/handsome.jpg")
      .save()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(qiscusAccount -> {
          DataManager.saveQiscusAccount(qiscusAccount);
          startActivity(new Intent(this, ConsultationListActivity.class));
      }, throwable -> {
              if (throwable instanceof HttpException) { //Error response from server
                  HttpException e = (HttpException) throwable;
                  try {
                      String errorMessage = e.response().errorBody().string();
                      Log.e(TAG, errorMessage);
                      showError(errorMessage);
                  } catch (IOException e1) {
                      e1.printStackTrace();
                  }
              } else if (throwable instanceof IOException) { //Error from network
                  showError("Can not connect to qiscus server!");
              } else { //Unknown error
                  showError("Unexpected error!");
              }
      });
      
// Start a chat activity with rxjava example      
Qiscus.buildChatWith("jhon.doe@gmail.com")
      .withTitle("Jhon Doe")
      .build(this)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(intent -> {
          startActivity(intent);
      }, throwable -> {
              if (throwable instanceof HttpException) { //Error response from server
                  HttpException e = (HttpException) throwable;
                  try {
                      String errorMessage = e.response().errorBody().string();
                      Log.e(TAG, errorMessage);
                      showError(errorMessage);
                  } catch (IOException e1) {
                      e1.printStackTrace();
                  }
              } else if (throwable instanceof IOException) { //Error from network
                  showError("Can not connect to qiscus server!");
              } else { //Unknown error
                  showError("Unexpected error!");
              }
      });
```

### Doesn't like RxJava
For you who doesn't comport with RxJava method, we provide utility class to execute RxJava method.
Here sample code how to get specific qiscus chat room by id.
```java
QiscusRxExecutor.execute(QiscusApi.getInstance().getChatRoom(123), new QiscusRxExecutor.Listener<QiscusChatRoom>() {
        @Override
        public void onSuccess(QiscusChatRoom result) {
            //Success getting the room
        }

        @Override
        public void onError(Throwable throwable) {
            //Something went wrong
        }
    });
```

# Sample Application

You can get the sample apps [here](https://github.com/qiscus/qiscus-sdk-android-sample)



