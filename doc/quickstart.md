# Quick Start

<p align="center"><br/><img src="https://raw.githubusercontent.com/qiscus/qiscus-sdk-android/master/screenshot/device-2017-06-02-093226.png" width="37%" /><br/></p>

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
    compile 'com.qiscus.sdk:chat:2.13.3'
}
```