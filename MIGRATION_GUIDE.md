# Models / Response

All method are now using this structure for their response,

## Getting started

In version 3, we are basically rewrite and/or improve our chat sdk
so all method have unified response and/or method signature.

## User

In v3 all user related response are separated into 3 model

- `QUser` which stand for general user model
- `QAccount` which stand for user whom currently active user
  response of `setUser` method
- `QParticipant` which stand for user which is part of chat room

> Note: some api might only return this data partially

```
public class QUser {
    protected String id;
    protected String avatarUrl;
    protected String name;
    protected JSONObject extras;
}
public class QAccount {
    protected String id;
    protected String avatarUrl;
    protected String token;
    protected String name;
    protected JSONObject extras;
    protected Long lastMessageId;
    protected Long lastSyncEventId;
}
public class QParticipant {
    private String id;
    private String name;
    private String avatarUrl;
    private long lastMessageDeliveredId;
    private long lastMessageReadId;
    private JSONObject extras;
}
```

## Message

For all message related data, in v3 it will use the following structure.

> Note: some api might return this data partially

```
public class  QMessage {
    protected long id;
    protected long chatRoomId;
    protected String uniqueId;
    protected long previousMessageId;
    protected String text;
    protected QUser sender;
    protected Date timestamp;
    protected int status;
    protected JSONObject extras;
    protected String rawType;
    protected String payload;
}
```

## Chat Room

In version 3, all data that are related to chat room
will use this structure

> Note: some api might return this data partially

```
public class QChatRoom {
    protected long id;
    protected String uniqueId;
    protected String name;
    protected JSONObject extras;
    protected String type;
    protected String avatarUrl;
    protected List<QParticipant> participants;
    protected int unreadCount;
    protected QMessage lastMessage;
    protected int totalParticipants;
}
```

# Method

In version 3 you can use multiple appID in 1 app, any different how to call our method. for example :

```
public QiscusCore qiscusCore1;
public QiscusCore qiscusCore2;

qiscusCore1 = new QiscusCore();
qiscusCore1.setup(this, QISCUS_SDK_APP_ID1);

qiscusCore2 = new QiscusCore();
qiscusCore2.setup(this, QISCUS_SDK_APP_ID2);
```

In version 3 for call api is different from version 2.
In version 2:
```
QiscusApi.getInstance().nameMethod
```

In version 3:
```
 qiscusCore1.getApi().nameMethod
```

## There are some method changes in this version 3, following the changes :

## `QiscusCore.setup` version 2 and 3 is same

In version 2 and 3, any 2 way call setup. first you can use default server, and seconds you can use manual or custom server

- `qiscusCore1.setup` for default configuration
- `qiscusCore1.setupWithCustomServer` for a more advanced option

```
qiscusCore1.setup(this, APPID1, "localKeyUser1")
```

if you have your own server (`On-Premise`) you can change the URL, here's the example:
```
qiscusCore1.setupWithCustomServer(this, AppID2, baseUrl, brokerUrl, brokerLBUrl, "localKeyUser1");
```

## `QiscusCore.setUser` version 2 and 3 is same

```
qiscusCore1.setUser(userId , userKey)
        .withUsername(username)
        .withAvatarUrl(avatarUrl)
        .withExtras(extras)
        .save(new QiscusCore.SetUserListener() {
            @Override
            public void onSuccess(QiscusAccount qiscusAccount) {
               // on success
            }

            @Override
            public void onError(Throwable throwable) {
               // on error
            }
        });
```

## `QiscusCore.setUserWithIdentityToken` version 2 and 3 is same

```
qiscusCore1.setUserWithIdentityToken('your jwt token', new QiscusCore.SetUserListener() {
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

## `QiscusApi.getInstance().blockUser` version 2 and 3 name method is same, just different how to call this method

in version 2 :
```
QiscusApi.getInstance().blockUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(account -> {
                    // on success
                }, throwable -> {
                    // on error
                });
```
in version 3:
```
 qiscusCore1.getApi().blockUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(account -> {
                    // on success
                }, throwable -> {
                    // on error
                });
```

## `QiscusApi.getInstance().unblockUser`  version 2 and 3 name method is same, just different how to call this method

In version 2
```
 QiscusApi.getInstance().unblockUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(account -> {
                    // on success
                }, throwable -> {
                    // on error
                });
```

In version 3
```
 qiscusCore1.getApi().unblockUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(account -> {
                    // on success
                }, throwable -> {
                    // on error
                });
```

## `QiscusApi.getInstance().getBlockedUsers` version 2 and 3 name method is same, just different how to call this method

In version 2
```
QiscusApi.getInstance().getBlockedUsers(page, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(blockUsers -> {
                    // on success
                }, throwable -> {
                    // on error
                });
```

In version 3
```
 qiscusCore1.getApi().getBlockedUsers(page, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(blockUsers -> {
                    // on success
                }, throwable -> {
                    // on error
                });
```

## `QiscusCore.clearUser` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusCore.clearUser()
```

In version 3
```
 qiscusCore1.clearUser()
```

## `QiscusCore.updateUser` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusCore.updateUser(name, avatarUrl, extras, new QiscusCore.SetUserListener() {    
    @Override    
    public void onSuccess(QiscusAccount qiscusAccount) {        
        //do anything after it successfully updated    
    }    

    @Override    
    public void onError(Throwable throwable) {        
    //do anything if error occurs    
    }}
);
```

In version 3
```
 qiscusCore1.updateUser(name, avatarUrl, extras, new QiscusCore.SetUserListener() {    
     @Override    
     public void onSuccess(QiscusAccount qiscusAccount) {        
         //do anything after it successfully updated    
     }    
 
     @Override    
     public void onError(Throwable throwable) {        
     //do anything if error occurs    
     }}
 );
```

## `QiscusApi.getInstance().getUsers` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().getUsers(searchUsername,page,limit)        
    .subscribeOn(Schedulers.io())       
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(users -> {            
        // on success        
    }, throwable -> {            
     // on error        
    });
```

In version 3
```
 qiscusCore1.getApi().getUsers(searchUsername,page,limit)        
     .subscribeOn(Schedulers.io())       
     .observeOn(AndroidSchedulers.mainThread())        
     .subscribe(users -> {            
         // on success        
     }, throwable -> {            
      // on error        
     });
```

## `QiscusApi.getInstance().getJWTNonce` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance()        
    .getJWTNonce()        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(qiscusNonce -> {            
        // on success        
    }, throwable -> {            
        // on error        
    });
```

In version 3
```
 qiscusCore1.getApi()        
     .getJWTNonce()        
     .subscribeOn(Schedulers.io())        
     .observeOn(AndroidSchedulers.mainThread())        
     .subscribe(qiscusNonce -> {            
         // on success        
     }, throwable -> {            
         // on error        
     });
```

## `QiscusApi.getInstance().getUserData` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().getUserData()                
    .subscribeOn(Schedulers.io())                
    .observeOn(AndroidSchedulers.mainThread())                
    .subscribe(new Action1<QiscusAccount>() {                    
        @Override                    
        public void call(QiscusAccount qiscusAccount) {                       
            System.out.println("profile" + qiscusAccount.getUsername());                    
        }                
    }, new Action1<Throwable>() {                    
        @Override                    
        public void call(Throwable throwable) {                        
            throwable.printStackTrace();                    
        }                
});
```

In version 3
```
qiscusCore1.getApi().getUserData()                
    .subscribeOn(Schedulers.io())                
    .observeOn(AndroidSchedulers.mainThread())                
    .subscribe(new Action1<QiscusAccount>() {                    
        @Override                    
        public void call(QiscusAccount qiscusAccount) {                       
            System.out.println("profile" + qiscusAccount.getUsername());                    
        }                
    }, new Action1<Throwable>() {                    
        @Override                    
        public void call(Throwable throwable) {                        
            throwable.printStackTrace();                    
        }                
});
```

## `QiscusCore.registerDeviceToken()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
String refreshedToken = FirebaseInstanceId.getInstance().getToken();
QiscusCore.registerDeviceToken(refreshedToken);
```

In version 3
```
String refreshedToken = FirebaseInstanceId.getInstance().getToken();
qiscusCore1.registerDeviceToken(refreshedToken);
```

## `QiscusCore.removeDeviceToken` version 2 and 3 name method is same, just different how to call this method
In version 2
```
String refreshedToken = FirebaseInstanceId.getInstance().getToken();
QiscusCore.removeDeviceToken(refreshedToken);
```

In version 3
```
String refreshedToken = FirebaseInstanceId.getInstance().getToken();
qiscusCore1.registerDeviceToken(refreshedToken);
```

## `QiscusApi.getInstance().updateChatRoom` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().updateChatRoom(roomId, roomName, avatarUrl, extras)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
    }
);
```

In version 3
```
qiscusCore1.getApi().updateChatRoom(roomId, roomName, avatarUrl, extras)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
    }
);
```

## `QiscusApi.getInstance().getChannel` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().createChannel(uniqueId, name,avatarUrl,extras)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
});
```

In version 3
```
qiscusCore1.getApi().createChannel(uniqueId, name,avatarUrl,extras)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
});
```

## `QiscusApi.getInstance().chatUser()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().chatUser(userId,avatarUrl,extras)        
    .doOnNext(chatRoom ->  QiscusCore.getDataStore().addOrUpdate(chatRoom))        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
    });
```

In version 3
```
qiscusCore1.getApi().chatUser(userId,avatarUrl,extras)        
    .doOnNext(chatRoom ->  QiscusCore.getDataStore().addOrUpdate(chatRoom))        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
});   
```

## `QiscusApi.getInstance().addParticipants()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().addParticipants(roomId, userIds)        
    .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        //on error                
});
```

In version 3
```
qiscusCore1.getApi().addParticipants(roomId, userIds)        
    .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        //on error                
});    
    
```

## `QiscusApi.getInstance().removeParticipants()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().removeParticipants(roomId, userIds)        
    .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        //on error                
});
```

In version 3
```
qiscusCore1.getApi().addParticipants(roomId, userIds)        
    .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        //on error                
});    
```

## `QiscusApi.getInstance().clearMessagesByChatRoomId` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().clearMessagesByChatRoomIds(roomIds)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(voids -> {            
        // on success        
    }, throwable -> {            
        // on error        
});

QiscusApi.getInstance().clearMessagesByChatRoomUniqueIds(roomUniqueIds)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(voids -> {            
        // on success        
    }, throwable -> {            
        // on error        
});
```

In version 3
```
qiscusCore1.getApi().clearMessagesByChatRoomIds(roomIds)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(voids -> {            
        // on success        
    }, throwable -> {            
        // on error        
});

qiscusCore1.getApi().clearMessagesByChatRoomUniqueIds(roomUniqueIds)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(voids -> {            
        // on success        
    }, throwable -> {            
        // on error        
});
```

## `QiscusApi.getInstance().createGroupChat()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().createGroupChat(name,userIds,avatarUrl,extras)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
});
```

In version 3
```
qiscusCore1.getApi().createGroupChat(name,userIds,avatarUrl,extras)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
});   
```

## `QiscusApi.getInstance().createChannel()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().createChannel(uniqueId, name,avatarUrl,extras)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
});
```

In version 3
```
qiscusCore1.getApi().createChannel(uniqueId, name,avatarUrl,extras)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoom -> {            
        // on success        
    }, throwable -> {            
        // on error        
}); 
```

## `QiscusApi.getInstance().getParticipants()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().getParticipants(roomUniqueId,0,asc)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(participants -> {        

    }, throwable -> {            
        throwable.printStackTrace();        
});
```

In version 3
```
qiscusCore1.getApi().getParticipants(roomUniqueId,0,asc)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(participants -> {        

    }, throwable -> {            
        throwable.printStackTrace();        
});
```

## `QiscusApi.getInstance().getChatRooms()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().getChatRooms(roomIds,page,showRemoved,showParticipant)                 
    .subscribeOn(Schedulers.io())                 
    .observeOn(AndroidSchedulers.mainThread())                 
    .subscribe(chatRoomList -> {                     
        //onSuccess                 
    }, throwable -> {                     
        //onError                 
});
```

In version 3
```
qiscusCore1.getApi().getChatRooms(roomIds,page,showRemoved,showParticipant)                 
    .subscribeOn(Schedulers.io())                 
    .observeOn(AndroidSchedulers.mainThread())                 
    .subscribe(chatRoomList -> {                     
        //onSuccess                 
    }, throwable -> {                     
        //onError                 
});
```

## `QiscusApi.getInstance().getAllChatRooms()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().getAllChatRooms(showParticipant,showRemoved,showEmpty,page,limit)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoomList -> {            
        // on success        
    }, throwable -> {            
        // on error        
});
```

In version 3
```
qiscusCore1.getApi().getAllChatRooms(showParticipant,showRemoved,showEmpty,page,limit)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoomList -> {            
        // on success        
    }, throwable -> {            
        // on error        
});
```

## `QiscusApi.getInstance().getChatRoomWithMessages()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().getChatRoomWithMessages(roomId)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoomListPair -> {            
        // on success getting chat room            
        QiscusChatRoom qiscusChatRoom = chatRoomListPair.first;            
        // on success getting comments            
        List<QiscusComment> comments = chatRoomListPair.second;        
    }, throwable -> {            
        // on error        
});
```

In version 3
```
qiscusCore1.getApi().getChatRoomWithMessages(roomId)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(chatRoomListPair -> {            
        // on success getting chat room            
        QiscusChatRoom qiscusChatRoom = chatRoomListPair.first;            
        // on success getting comments            
        List<QiscusComment> comments = chatRoomListPair.second;        
    }, throwable -> {            
        // on error        
});
```

## `QiscusApi.getInstance().getTotalUnreadCount()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().getTotalUnreadCount()        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(totalUnreadCount -> {            
        //success        
    }, throwable -> {            
        //error                
});
```

In version 3
```
qiscusCore1.getApi().getTotalUnreadCount()        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(totalUnreadCount -> {            
        //success        
    }, throwable -> {            
        //error                
});
```

## `QiscusApi.getInstance().sendMessage()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusComment qiscusComment = QiscusComment.generateMessage(roomId, text);
QiscusApi.getInstance().sendMessage(qiscusComment)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(commentSent -> {            
        // success        
    }, throwable -> {            
    // error        
});
```

In version 3
```
QMessage qiscusComment = QMessage.generateMessage(roomId, text);
qiscusCore1.getApi().sendMessage(qiscusComment)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(commentSent -> {            
        // success        
    }, throwable -> {            
    // error        
});
```

## `QiscusApi.getInstance().sendFileMessage()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().sendFileMessage(qMessage, file, new QiscusApi.ProgressListener() {
       @Override
       public void onProgress(long total) {
                onProgressUploadListener.onProgress(total);
       }})
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Action1<QMessage>() {
       @Override
       public void call(QMessage qMessage) {
            onProgressUploadListener.onSuccess(qMessage);
       }
    }, new Action1<Throwable>() {
       @Override
       public void call(Throwable throwable) {
            onProgressUploadListener.onFailed(throwable, qMessage);
       }
});
```

In version 3
```
qiscusCore1.getApi().sendFileMessage(qMessage, file, new QiscusApi.ProgressListener() {
       @Override
       public void onProgress(long total) {
                onProgressUploadListener.onProgress(total);
       }})
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Action1<QMessage>() {
       @Override
       public void call(QMessage qMessage) {
            onProgressUploadListener.onSuccess(qMessage);
       }
    }, new Action1<Throwable>() {
       @Override
       public void call(Throwable throwable) {
            onProgressUploadListener.onFailed(throwable, qMessage);
       }
});
```

## `QiscusPusherApi.getInstance().markAsDelivered()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusPusherApi.getInstance().markAsDelivered(roomId, commentId)                 
    .subscribeOn(Schedulers.io())                 
    .observeOn(AndroidSchedulers.mainThread())                 
    .subscribe(aVoid -> {                      
        // success                 
    }, throwable -> {                     
        // error                 
});           
```

In version 3
```
qiscusCore1.getPusherApi().markAsDelivered(roomId, commentId)                 
    .subscribeOn(Schedulers.io())                 
    .observeOn(AndroidSchedulers.mainThread())                 
    .subscribe(aVoid -> {                      
        // success                 
    }, throwable -> {                     
        // error                 
});
```

## `QiscusPusherApi.getInstance().markAsRead` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusPusherApi.getInstance().markAsRead(roomId, commentId)                 
    .subscribeOn(Schedulers.io())                 
    .observeOn(AndroidSchedulers.mainThread())                 
    .subscribe(aVoid -> {                      
        // success                 
    }, throwable -> {                     
        // error                 
});             
```

In version 3
```
qiscusCore1.getPusherApi().markAsRead(roomId, commentId)                 
    .subscribeOn(Schedulers.io())                 
    .observeOn(AndroidSchedulers.mainThread())                 
    .subscribe(aVoid -> {                      
        // success                 
    }, throwable -> {                     
        // error                 
});  
```

## ` QiscusApi.getInstance().deleteMessages()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
Observable.from(comments)        
    .map(QiscusComment::getUniqueId)        
    .toList()           
    .flatMap(uniqueIds -> QiscusApi.getInstance().deleteMessages(uniqueIds))        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .compose(bindToLifecycle())        
    .subscribe(deletedComments -> {            
        if (view != null) {                

        }        
    }, throwable -> {            
        if (view != null) {            
        
        }            
        QiscusErrorLogger.print(throwable);        
});           
```

In version 3
```
Observable.from(comments)        
    .map(QiscusComment::getUniqueId)        
    .toList()           
    .flatMap(uniqueIds -> qiscusCore1.getApi().deleteMessages(uniqueIds))        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .compose(bindToLifecycle())        
    .subscribe(deletedComments -> {            
        if (view != null) {                

        }        
    }, throwable -> {            
        if (view != null) {            
        
        }            
        QiscusErrorLogger.print(throwable);        
});             
```

## `QiscusApi.getInstance().getPreviousMessagesById()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().getPreviousMessagesById(roomId,limit,messageId)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(comment -> {            
        // success        
    }, throwable -> {            
        // error        
    });        
```

In version 3
```
qiscusCore1.getApi().getPreviousMessagesById(roomId,limit,messageId)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(comment -> {            
        // success        
    }, throwable -> {            
        // error        
    });
```

## `QiscusApi.getInstance().getNextMessagesById()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().getNextMessagesById(roomId,limit,messageId)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(comment -> {            
        // success        
    }, throwable -> {            
        // error       
     });          
```

In version 3
```
qiscusCore1.getApi().getNextMessagesById(roomId,limit,messageId)        
    .subscribeOn(Schedulers.io())        
    .observeOn(AndroidSchedulers.mainThread())        
    .subscribe(comment -> {            
        // success        
    }, throwable -> {            
        // error       
     }); 
```

## `QiscusCore.hasSetupUser()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusCore.hasSetupUser()        
```

In version 3
```
qiscusCore1.hasSetupUser() 
```

## `QiscusApi.getInstance().upload()` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusApi.getInstance().upload(file, progressListener -> {                     
        // here you can get the progress total file uploaded                 
    })
    .subscribeOn(Schedulers.io())                 
    .observeOn(AndroidSchedulers.mainThread())                 
    .subscribe(uri -> {                    
        // on success get Uri                 
    }, throwable -> {                     
        // on error                 
});
```

In version 3
```
qiscusCore1.getApi().upload(file, progressListener -> {                     
        // here you can get the progress total file uploaded                 
    })
    .subscribeOn(Schedulers.io())                 
    .observeOn(AndroidSchedulers.mainThread())                 
    .subscribe(uri -> {                    
        // on success get Uri                 
    }, throwable -> {                     
        // on error                 
});
```

## `QiscusFileUtil.getThumbnailURL` version 2 and 3 name method is same, just different how to call this method
In version 2
```
QiscusFileUtil.getThumbnailURL(fileUrl)
```

In version 3
```
QiscusFileUtil.getThumbnailURL(fileUrl)
```

# Realtime Event

while in version 2, realtime event are passed as an arguments when initializing qiscus sdk,
in version 3, in comes with it own method, so you can initialized realtime event handling at a later code.

## Subscribe custom events `publishCustomEvent(), subscribeCustomEvent(), and unsubscribeCustomEvent()` 

In version 1
```
QiscusPusherApi.getInstance().publishCustomEvent(long roomId, JSONObject data);
QiscusPusherApi.getInstance().subsribeCustomEvent(long roomId);
QiscusPusherApi.getInstance().unsubsribeCustomEvent(long roomId);
```
In version 2
```
qiscusCore1.getPusherApi().publishCustomEvent(long roomId, JSONObject data);
qiscusCore1.getPusherApi().subsribeCustomEvent(long roomId);
qiscusCore1.getPusherApi().unsubsribeCustomEvent(long roomId);
```

## Subscribe Chat Room related events

this event include message being read and delivered, and user typing on that room

In version 1
```
QiscusPusherApi.getInstance().publishOnlinePresence(boolean isOnline);
QiscusPusherApi.getInstance().publishTyping(long roomId, boolean isTyping);
QiscusPusherApi.getInstance().subscribeChatRoom(QiscusChatRoom qiscusChatRoom);
QiscusPusherApi.getInstance().unsubsribeChatRoom(QiscusChatRoom qiscusChatRoom);
```
In version 2
```
qiscusCore1.getPusherApi().publishOnlinePresence(boolean isOnline);
qiscusCore1.getPusherApi().publishTyping(long roomId, boolean isTyping);
qiscusCore1.getPusherApi().subscribeChatRoom(QChatRoom qiscusChatRoom);
qiscusCore1.getPusherApi().unsubsribeChatRoom(QChatRoom qiscusChatRoom);
```

## Subscribe user online presence

In version 1
```
QiscusPusherApi.getInstance().subscribeUserOnlinePresence(String userId);
QiscusPusherApi.getInstance().unsubscribeUserOnlinePresence(String userId);
```
In version 2
```
qiscusCore1.getPusherApi().subscribeUserOnlinePresence(String userId);
qiscusCore1.getPusherApi().unsubscribeUserOnlinePresence(String userId);
```

## Handler
```
@Subscribe
public void onMessageReceived(QiscusCommentReceivedEvent event) {
    when (event.qiscusComment.type) {
       
    }
}

@Subscribe
public void onUserOnlinePresence(QiscusUserStatusEvent event) {    
    event.getUser(); 
    // this is the qiscus user id    
    event.isOnline(); 
    // true if user is online    
    event.getLastActive(); 
    // Date of last active user
}

@Subscribe
public void onMessageDelivered(QiscusChatRoomEvent roomEvent) {    
    switch (roomEvent.getEvent()) {        
        case DELIVERED:            
        roomEvent.getRoomId(); 
        // this is the room id            
        roomEvent.getUser(); 
        // this is the qiscus user id            
        roomEvent.getCommentId(); 
        // the comment id was delivered            
        break;    
}}

@Subscribe
public void onMessageRead(QiscusChatRoomEvent roomEvent) {    
    switch (roomEvent.getEvent()) {        
        case READ:            
        roomEvent.getRoomId(); 
        // this is the room id            
        roomEvent.getUser(); 
        // this is the qiscus user id            
        roomEvent.getCommentId(); 
        // the comment id was read            
        break;    
}}

@Subscribe
public void onChatRoomEvent(QiscusChatRoomEvent event) {
    QiscusAndroidUtil.runOnBackgroundThread(() -> handleEvent(event));
}


@Subscribe
public void onMessageDeleted(QiscusCommentDeletedEvent event) {    
    if (event.getQiscusComment().getRoomId() == room.getId()) {        
    QiscusAndroidUtil.runOnUIThread(() -> {            
        if (view != null) {                
            view.onCommentDeleted(event.getQiscusComment());            
        }        
    });    
}}

@Subscribe
public void onChatRoomCleared(QiscusClearCommentsEvent event) {    
    if (event.getRoomId() == room.getId()) {        
        QiscusAndroidUtil.runOnUIThread(() -> {            
        if (view != null) {                
            view.clearCommentsBefore(event.getTimestamp());            
        }        
    });    
}}
```

## Subscribe realtime server connection state

this event related to connection state of mqtt, which is our realtime mechanism

```
@Subscribe
public void onConneted(QiscusMqttStatusEvent mqttStatusEvent){    
    switch (mqttStatusEvent) {        
        case CONNECTED:            
        // you can do anyting            
        break;    
    }}

@Subscribe
public void onDisconnected(QiscusMqttStatusEvent mqttStatusEvent){    
    switch (mqttStatusEvent) {        
        case DISCONNECTED:            
        // you can do anyting            
        break;    
}}

@Subscribe
public void onReconnecting(QiscusMqttStatusEvent mqttStatusEvent){    
    switch (mqttStatusEvent) {        
        case RECONNETING:            
        // you can do anyting            
        break;    
}}
```

