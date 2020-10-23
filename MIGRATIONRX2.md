## Getting started
Now that we've migrated to RxJava 2, there are some notes and changes to sdk and implementation related to this change. Changes from RxJava 1 to RxJava 2 are listed below :


## Implementation Gradle

```
    implementation 'com.qiscus.sdk:chat-core:1.5.0-beta.3'
```

## What's changed?

### Import
We see different import from RxJava 1 to RxJava 2, import differences are as follows :

RxJava 1 import

```
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.Subscription;
import rx.functions.Function;
```

RxJava 2 import

```
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
```

### Code implementation in Chat SDK
 There are no changes in API, so you don't need to change the Chat SDK function, for example when we sending message, it will stay as it's.
 ```
 String message = "Hi"

//Generate message object
QiscusComment qiscusMessage = QiscusComment.generateMessage(roomId, message)

//Send message
Disposable disposable = QiscusApi.getInstance().sendMessage(qiscusMessage)
        .subscribeOn(Schedulers.io()) // need to run this task on IO thread
        .observeOn(AndroidSchedulers.mainThread()) // deliver result on main thread or UI thread
        .subscribe(qiscusChatRoom -> {
            // on success
        }, throwable -> {
            // on error
        });
 ```

### Code implementation in sample
The difference between the RxJava 1 and RxJava 2 implementation in sample Chat SDK.

`Observable.fromIterable()` previously known as `Observable.from()`

`.flatMap(Observable::fromIterable)` previously known as `.flatMap(Observable::from)`

`Disposable` previously known as `Subscription`

`disposable.isDisposed()` previously known as `subscription.isUnsubscribed()`

`disposable.dispose()` previously known as `subscription.unsubscribe();`

`.doOnSubscribe(disposable -> //action)` previously known as ` .doOnSubscribe(() -> //action)`

`.subscribeWith(new DisposableXXXX)` previously known as `.subscribe( nameData -> {})`

`We remove  code .compose(bindToLifecycle()) in implementation` previously known as `.compose(bindToLifecycle())`

## Sample

You can follow our sample in [SampleSdkV2Rx2](https://github.com/qiscus/qiscus-chat-sdk-android-sample/tree/rx2) and [SampleSdkV3Rx2](https://github.com/qiscus/qiscus-chat-sdk-android-sample/tree/rx2-v3)

## Troubleshooting
If you get crash in Okhttp library, we are recomend to use minimum this version
```
implementation 'com.squareup.okhttp3:okhttp:3.12.5'
implementation 'com.squareup.retrofit2:retrofit:2.5.0'
```