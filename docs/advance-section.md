# Advanced Section

## Without Emoji
Add Qiscus SDK to your apps without emoji library, change gradle file to like this.

```groovy
dependencies {
    implementation 'com.qiscus.sdk:chat:2.30.15-without-emoji'
}
```

## Core Only
If you want to use Core SDK only, change gradle file to like below :

```groovy
dependencies {
    implementation 'com.qiscus.sdk:chat-core:1.3.4'
}
```

 And you need change `Qiscus` main class to `QiscusCore`
 
 ```java
 
//example

// from
Qiscus.init(this, "yourappid");

//to
QiscusCore.init(this, "yourappid");

```


## Server Authentication

Server Authentication is another option, which allow you to authenticate using Json Web Token [(JWT)](https://jwt.io/). Json web token contains your app account details which typically consists of a single string which contains information of two parts, Jose header and JWT claims set. 

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
**Signature :**

JWT need to be signed using Qiscus Secret Key, the one you get in [dashboard](https://www.qiscus.com/dashboard/login). The signature is used to verify that the sender of the JWT is who it says it is. To create the signature part you have to take the encoded Jose header, the encoded JWT Claim Set, a Qiscus Secret Key, the algorithm specified in the header, and sign that. 

The signature is computed using the following pseudo code :

```
data = base64urlEncode( Jose Header ) + “.” + base64urlEncode( JWT Claim Set )
hashedData = hash( data, secret )
signature = base64urlEncode( hashedData )
```

Here's example using PHP (You can use any other language or platform), the signature will be created in the following:

```
 public function generateIdentityToken($nonce, $user_id, $user_name = "", $avatar_url = "")
 {
// Using HMAC (HS256) signing method
    $signer = new Sha256();

    $now = time();

    $token = (new Builder())
      // Set header
      // Header "alg" has been set to HS256 by library
      // Header "typ" has been set to "JWT" by library
      ->setHeader("ver", "v2")

      // Set the payload
      // Configures the issuer (iss claim). It is a qiscus app id, can be obtain from dashboard
      ->setIssuer($this->qiscus_sdk_app_id)

      // Configures payload, "prn" is a client provider's internal ID for the authenticating user. Don't copy to header
      ->set("prn", $user_id, false)

      // Configures the time that the token was issue (iat claim)
      ->setIssuedAt($now)

      // Configures the time that the token can be used (nbf claim)
      ->setNotBefore($now)

      // Token expiration set to 2 minutes only (exp claim)
      ->setExpiration($now + 120)

      // nce claim, is a nonce from client
      ->set("nce", $nonce, false)

      // String - Optional - Name of user.
      ->set("name", $user_name, false)

      // String - Optional - Avatar url of user.
      ->set("avatar_url", $avatar_url, false)

      // **Signing**
      // creates a signature using qiscus sdk secret as key
      ->sign($signer, $this->qiscus_sdk_secret)

      // Retrieves the generated token
      ->getToken();

    // var_dump($token->verify($signer, $this->qiscus_sdk_secret));

    return (string) $token;
}
```

## UI Customization

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

For advance customization, you can only use our Core SDK API for the data flow and use your own full UI. By using this approach you will have full control over the UI. We have sample on how you can do it and there are documentation on list of core api that we provide in the SDK. check it here [https://bitbucket.org/qiscus/qiscus-sdk-core-android-sample](https://bitbucket.org/qiscus/qiscus-sdk-core-android-sample/overview)

## Event Handler

An Event Handler is a callback routine that operates asynchronously and handles inputs received into a program. Event Handlers are important in Qiscus because it allows a client to react to any events happening in Qiscus Chat SDK. For example, if a client wants to know any important events from server, such as success login event, client's app can be notified by calling a specific Event Handler. Client, then, can do things with data returned by the event.
To enable client app to publish or subscribe something to Qiscus Chat SDK, we use **EventBus** library , so you can listen event from anywhere. **You can learn more about Eventbus [here](http://greenrobot.org/eventbus/)**. 

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