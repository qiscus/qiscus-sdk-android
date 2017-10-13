# Offline Messages

## Post Messages

During post message, if you don't have any internet connection, message will be store locally and will be automatically being send once your internet connection is back. For you want to enqueue a message manually you can call this api:
```java
QiscusApi.getInstance().postComment(qiscusComment)
        .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
        .doOnError(throwable -> {
            qiscusComment.setState(QiscusComment.STATE_PENDING);
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(commentSend -> {
            //Success
        }, throwable -> {
            //we will automatically retry again later
        });
```

## Get Messages

Messages are stored locally so you can still access the messages when you don't have internet connection. However any new messages will not being received after you have your internet connection back. To access data locally, you can use QiscusDataStore, here sample code to get local room and message

```java
QiscusChatRoom room = Qiscus.getDataStore().getChatRoom(roomId);
List<QiscusComment> comments = Qiscus.getDataStore().getComments(room.getLastTopicId(), count);
```
