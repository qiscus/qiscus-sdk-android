

# 3. Advanced Section

## UI Costomization

Qiscus Chat SDK enable you to customize Chat UI as you like. You can modify colors, change bubble chat design, modify Chat Header, and many more. There are 2 level of UI customization, Basic and Advance Customization. For Basic Customization, you can simply look at the autocomplete suggestion in your IDE to see lots of method to modify your Chat Interface. For Advance Customization, you need to download the UI source code and modify anything as you need.

### Basic Customization

Basic customization allows you to modify things inside Qiscus Chat SDK features. To do that, you can simply look at the autocomplete suggestion under `getChatConfig()` in your IDE to see lots of method to modify your Chat Interface. You can change almost everything inside your chat room such as bubble chat, header chat, read receipt, and many more. Here is the example of modifying status bar color, app bar color, title buble chat color and date format.

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

### Advance Customization

For advance customization, you can modify everything on the chat interface by forking our repository or just right away modifying our **[CustomChatActivity.java](https://github.com/qiscus/qiscus-sdk-android/blob/develop/app/src/main/java/com/qiscus/dragonfly/CustomChatActivity.java)** ****based on your needs.

## Event Handler

An Event Handler is a callback routine that operates asynchronously and handles inputs received into a program. Event Handlers are important in Qiscus because it allows a client to react to any events happening in Qiscus Chat SDK. For example, if a client wants to know any important events from server, such as success login event, client's app can be notified by calling a specific Event Handler. Client, then, can do things with data returned by the event.
To enable client app to publish or subscribe something to Qiscus Chat SDK,**  **we use **EventBus** library , so you can listen event from anywhere, It does not matter whether it's an activity or not**. **You can learn more about Eventbus [here](http://greenrobot.org/eventbus/).

Here is an example to use Eventbus on your application class :

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
                //Someone is typing on this room event.getRoomId()
                break;
            case DELIVERED:
                //Someone just received your message event.getCommentId()
                break;
            case READ:
                //Someone just read your message event.getCommentId()
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
