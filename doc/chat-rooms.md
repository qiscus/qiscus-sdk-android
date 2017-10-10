# Chat Rooms

### Creating 1-to-1 chat

Start chat with target is very easy, all you need is just call

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


### Creating a Group Room

Qiscus also support group chat. To create new group chat, all you need is just call


```java
Qiscus.buildGroupChatRoom("GroupName", Arrays.asList("user1@gmail.com", "user2@gmail.com", "user3@gmail.com"))
      .withAvatar("http://avatar.url.com/group.jpg")
      .build(new Qiscus.ChatBuilderListener() {
          @Override
          public void onSuccess(QiscusChatRoom qiscusChatRoom) {
              startActivity(QiscusGroupChatActivity.generateIntent(MainActivity.this, qiscusChatRoom));
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

for accessing room that created by this call, you need to call it with its roomId. This methode is always creating new chat room.


### Get a room by room id


When you already know your chat room id, you can easily go to that room. Just call

```java
QiscusApi.getChatRoom(int roomId);
```

For example :

```java
QiscusApi.getInstance()
        .getChatRoom(123)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(qiscusChatRoom -> QiscusGroupChatActivity.generateIntent(this, qiscusChatRoom))
        .subscribe(this::startActivity, throwable -> {
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

### Create or join room by defined id

You probably want to set defined id for the room you are creating so that the id can be reference for users to get into.

Usual usage for this is when user create common room or channel which expecting other users can join to the same channel by knowing the channel name or id, you can use the channel name or id as qiscus room defined id.

Additional note:
If room with predefined unique id is not exist then it will create a new one with requester as the only one participant. Otherwise, if room with predefined unique id is already exist, it will return that room and add requester as a participant.

When first call (room is not exist), if requester did not send avatar_url and/or room name it will use default value. But, after the second call (room is exist) and user (requester) send avatar_url and/or room name, it will be updated to that value. Object changed will be true in first call and when avatar_url or room name is updated.

```java
Qiscus.buildGroupChatRoomWith("UniqueId")
        .withName("RoomName")
        .withAvatar("http://avatar.url.com/group.jpg")
        .build(new Qiscus.ChatBuilderListener() {
            @Override
            public void onSuccess(QiscusChatRoom qiscusChatRoom) {
                startActivity(QiscusGroupChatActivity.generateIntent(MainActivity.this, qiscusChatRoom));
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

### Inviting users to an existing Room

Currently we recommend to invite user into existing room through our [**REST API**](https://www.qiscus.com/docs/restapi) for simplicity and security reason

### Leaving a Group Room

Currently we recommend to kick user out of specific room through our [**REST API**](https://www.qiscus.com/docs/restapi) for simplicity and security reason

### Get room list
To get all room list you can call QiscusApi.getInstance().getChatRooms(int page, int limit, boolean showMembers), page start from 1, limit indicate the max rooms per page, showMembers is flag for load room members also or not. Here sample code:
```java
QiscusApi.getInstance().getChatRooms(1, 20, true)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(qiscusChatRooms -> {
            //qiscusChatRooms is list of rooms result.
        }, throwable -> {
            //Something went wrong
        });
```
