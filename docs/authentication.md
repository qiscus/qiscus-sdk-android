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
