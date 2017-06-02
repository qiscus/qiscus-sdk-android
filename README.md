# Qiscus SDK Android

<p align="center"><img src="https://raw.githubusercontent.com/qiscus/qiscus-sdk-android/master/screenshot/device-2017-06-02-093226.png" width="30%" /></p>

# Quick Start

### Create a new app

Register on [https://dashboard.qiscus.com](https://dashboard.qiscus.com/) using your email and password and then create new application

You should create one application per service, regardless of the platform. For example, an app released in both Android and iOS would require only one application to be created in the Dashboard.

All users within the same Qiscus application are able to communicate with each other, across all platforms. This means users using iOS, Android, web clients, etc. can all chat with one another. However, users in different Qiscus applications cannot talk to each other.

Done! Now you can use the APP_ID into your apps and get chat functionality by implementing Qiscus into your app.



### integrating SDK with an existing app

Add to your project build.gradle


```groovy
allprojects {
    repositories {
        .....
        maven { url  "http://dl.bintray.com/qiscustech/maven" }
    }
}
```

Then add to your app module build.gradle


```groovy
dependencies {
    compile 'com.qiscus.sdk:chat:1.19.1'
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

### Updating a User Profile and Avatar

Updating user profile and details is simply by re-init the user using new details :


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

### Disconnect or Logout

Whenever you no longer want the user to receive update 

```java
Qiscus.clearUser();
```



# Room Types 

### Creating and starting 1-to-1 chat

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


### Getting a Group Room instance with room id


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



### Inviting users to an existing Room

Currently we recommend to invite user into existing room through our [**REST API**](https://www.qiscus.com/docs/restapi) for simplicity and security reason

### Leaving a Group Room

Currently we recommend to kick user out of specific room through our [**REST API**](https://www.qiscus.com/docs/restapi) for simplicity and security reason

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

# Resources

### Video

Here's a video showing how you can set up sample app for qiscus SDK
[<p align="center"><img src="https://res.cloudinary.com/qiscus/image/upload/c91CeMe08I/Screen%20Shot%202017-05-23%20at%201.57.19%20PM.png" width="50%" /></p>](https://www.youtube.com/watch?v=7tHNliEoths)

### Sample Application

Or you can get the sample apps [here](https://github.com/qiscus/qiscus-sdk-android-sample)

# Miscellaneous
### Android Support Libraries
Qiscus SDK is using appcompat libraries to support some features. If your apps using appcompat too, we highly recommended to using the latest stable appcompat version, or using the same version with Qiscus SDK. You can check the appcompat version of Qiscus SDK [here](https://github.com/qiscus/qiscus-sdk-android/blob/master/chat/build.gradle#L102). You can also force Qiscus SDK to use your apps appcompat verion. Use "exclude group" at your build.gradle, for example:

```groovy
//Qiscus sdk without android support libraries
compile('com.qiscus.sdk:chat:1.19.1') {
    transitive = true
    exclude group: 'com.android.support'
}

//Qiscus sdk needs all of this android support libraries
//Just add the same version with your apps dependencies
compile 'com.android.support:support-v4:yourVersion'
compile 'com.android.support:appcompat-v7:yourVersion'
compile 'com.android.support:recyclerview-v7:yourVersion'
compile 'com.android.support:design:yourVersion'
compile 'com.android.support:customtabs:yourVersion'
```

### Rx Java support

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

