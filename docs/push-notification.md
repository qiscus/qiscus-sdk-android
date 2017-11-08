# Push Notifications

First install FCM to your apps, you can follow [this steps.](https://firebase.google.com/docs/cloud-messaging/android/client) You can skip this step, if your apps already use FCM. Then put your api key to qiscus dashboard.

Now lets integrate with Qiscus client sdk, first enable FCM at Qiscus chat config.

```java
Qiscus.getChatConfig().setEnableFcmPushNotification(true);
```

After that, lets change your firebase service to extend Qiscus firebase service instead of firebase service class.
```java
public class MyFirebaseIdService extends QiscusFirebaseIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh(); // Must call super

        //Below is your own apps specific code
        // e.g register the token to your backend
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendTokenToMyBackend(refreshedToken);
    }
}
```

```java
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

        //Register token to qiscus
        Qiscus.setFcmToken(refreshedToken);

        //Below is your own apps specific code
        // e.g register the token to your backend
        sendTokenToMyBackend(refreshedToken);
    }
}
```

```java
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (QiscusFirebaseService.handleMessageReceived(remoteMessage)) { // For qiscus
            return;
        }

        //Your FCM PN here
    }
}
```