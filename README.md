Qiscus SDK
======
Qiscus SDK is a lightweight and powerful android chat library. Qiscus SDK will allow you to easily integrating Qiscus engine with your apps to make cool chatting application.

# Instalation
Add to your project build.gradle
```groovy
allprojects {
    repositories {
        .....
        maven { url "https://jitpack.io" }
        maven { url 'http://clojars.org/repo' }
    }
}
```

Then add to your app module build.gradle
```groovy
dependencies {
    compile 'com.github.qiscus:qiscus-sdk-android:1.4'
}
```
# Let's make cools chatting apps!
#### Init Qiscus
Init Qiscus at your application class
```java
public class SampleApps extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Qiscus.init(this, "yourQiscusAppId");
    }
}
```
#### Login to Qiscus engine
Before user can start chatting each other, they must login to qiscus engine.
```java
Qiscus.setUser("user@email.com", "password")
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
              throwable.printStackTrace();
              showError(throwable.getMessage());
          }
      });
```
### Start the chatting
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
              throwable.printStackTrace();
              showError(throwable.getMessage());
          }
      });
```
### Customize the chat UI
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
### RxJava Support
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
          throwable.printStackTrace();
          showError(throwable.getMessage());
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
          throwable.printStackTrace();
          showError(throwable.getMessage());
      });
```

Check sample apps -> [DragonFly](https://github.com/qiscus/qiscus-sdk-android-sample)
