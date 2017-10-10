# QiscusChatSDK

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
    compile 'com.qiscus.sdk.chat:core:3.0.0-beta2'
}
```
