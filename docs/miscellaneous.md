# Miscellaneous

### Android Support Libraries
Qiscus SDK is using appcompat libraries to support some features. If your apps using appcompat too, we highly recommended to using the latest stable appcompat version, or using the same version with Qiscus SDK. You can check the appcompat version of Qiscus SDK [here](https://github.com/qiscus/qiscus-sdk-android/blob/master/chat/build.gradle#L102). You can also force Qiscus SDK to use your apps appcompat version. Use "exclude group" at your build.gradle, for example:

```groovy
//Qiscus sdk without android support libraries
compile('com.qiscus.sdk:chat:2.14.0') {
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

If you have problem can not download android support libraries, please add Google's Maven repository to your project build.gradle

```groovy
allprojects {
    repositories {
        .....
        maven { url  "https://maven.google.com" }
    }
}
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

### Proguard

If you are using Proguard in your application, make sure you add Proguard rules of Qiscus from
[Qiscus Proguard Rules](https://github.com/qiscus/qiscus-sdk-android/blob/master/app/proguard-rules.pro) to your Proguard rules.