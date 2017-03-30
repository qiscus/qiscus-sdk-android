Qiscus SDK [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Qiscus%20SDK-green.svg?style=true)](https://android-arsenal.com/details/1/4438) [![Build Status](https://travis-ci.org/qiscus/qiscus-sdk-android.svg?branch=master)](https://travis-ci.org/qiscus/qiscus-sdk-android)
======
<p align="center"><img src="https://github.com/qiscus/qiscus-sdk-android/raw/develop/screenshot/device-2016-09-16-102736.png" width="40%" /><img src="https://github.com/qiscus/qiscus-sdk-android/raw/develop/screenshot/device-2016-09-16-102923.png" width="40%" /></p>
Qiscus SDK is a lightweight and powerful android chat library. Qiscus SDK will allow you to easily integrating Qiscus engine with your apps to make cool chatting application.

# Quick Start
### 1. Create a new SDK application in the Dashboard and get app_id 
### 2. When integrating SDK with an existing app 
### 3. Rx Java support?

# Authentication 
### Initializing with APP_ID
### Login or register
### Disconnecting/logout 
### Updating a User Profile and Avatar
    
# Room Types  
### Group Room
### 1 on 1 
    
# 1-to-1 Chat
### Creating and starting 1-to-1 chat 
    
# Group Room 
### Creating a Group Room 
### Getting a Group Room instance with room id 
### Inviting users to an existing room 
### Leaving a Group Room 
### Advanced 
#### Getting a list of all room members 
#### Getting participants' online statuses 
#### Typing indicators
#### Read Receipts 
#### Admin messages 
#### Room cover images
#### Custom room types 
#### Custom message types 
#### Message auto-translation 
#### File Message thumbnails 
        
# Messaging
### Sending messages 
### Receiving messages 
### Loading previous messages 
### Loading messages by timestamp 
### Getting a list of participants in a room 
### Getting participants' online statuses 
### Getting a list of banned or muted users in a room 
### Deleting messages
    
# Room Metadata
### MetaData 
### MetaCounter 
    
# Event Handler
### Room Handler 

# UI Customization
### Theme Customization 
### UI Source code
    
# Push Notifications
### (1) Generate FCM Server API Key and FCM Sender ID
### (2) Register FCM Server API Key and FCM Sender ID 
### (3) Set up FCM client code in your Android application 
### (4) Register FCM Registration Token in the SendBird SDK and parse SendBird FCM messages 
### Push notification message templates 
    
# Caching Data
# Miscellaneous 
# Change Log 
# API Reference 

# Instalation
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
    compile 'com.qiscus.sdk:chat:1.16.1'
}
```
# Let's make cools chatting apps!
#### Init Qiscus
Init Qiscus at your application class with your application ID, you can get app ID here [http://sdk.qiscus.com](http://sdk.qiscus.com)
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
### Advanced Chat Customizing
Check [CustomChatActivity.java](https://github.com/qiscus/qiscus-sdk-android/blob/develop/app/src/main/java/com/qiscus/dragonfly/CustomChatActivity.java)
<p align="center"><img src="https://github.com/qiscus/qiscus-sdk-android/raw/develop/screenshot/device-2016-09-28-232326.png" width="33%" /><img src="https://github.com/qiscus/qiscus-sdk-android/raw/develop/screenshot/device-2016-09-28-232535.png" width="33%" /><img src="https://github.com/qiscus/qiscus-sdk-android/raw/develop/screenshot/device-2016-09-28-232714.png" width="33%" /></p>

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

Check sample apps -> [DragonGo](https://github.com/qiscus/qiscus-sdk-android-sample)

License
-------
    Copyright (c) 2016 Qiscus.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
