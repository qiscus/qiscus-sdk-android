/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.data;

import android.support.annotation.RestrictTo;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Base64;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.encryption.QiscusE2EDataStore;
import com.qiscus.sdk.data.encryption.QiscusMyBundleCache;
import com.qiscus.sdk.data.encryption.core.GroupConversation;
import com.qiscus.sdk.data.encryption.core.HashId;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusRawDataExtractor;

import org.json.JSONObject;

import rx.Observable;

/**
 * Created on : May 08, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QiscusGroupEncryptionHandler {

    public static void initSenderKey(long roomId) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> {
            try {
                getConversation(roomId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void reInitSenderKey(long roomId, boolean needReply) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> {
            try {
                String deviceId = QiscusMyBundleCache.getInstance().getDeviceId();
                GroupConversation groupConversation = new GroupConversation();
                groupConversation.initSender(new HashId(deviceId.getBytes()));
                notifyMembers(roomId, Base64.encodeToString(groupConversation.getSenderKey(), Base64.DEFAULT), needReply);
                saveConversation(roomId, groupConversation);
            } catch (Exception e) {
                //Do nothing;
            }
        });
    }

    public static Pair<String, String> createEncryptedPayload(long roomId, QiscusComment comment) throws Exception {
        String message = QiscusEncryptionHandler.encryptAbleMessage(comment.getRawType())
                ? encrypt(roomId, comment.getMessage()) : comment.getMessage();
        String payload = "";
        if (!TextUtils.isEmpty(comment.getExtraPayload())) {
            payload = encrypt(roomId, comment.getRawType(), new JSONObject(comment.getExtraPayload()));
        }
        return Pair.create(message, payload);
    }

    private static String encrypt(long roomId, String rawType, JSONObject payload) throws Exception {
        switch (rawType) {
            case "reply":
                payload.put("text", encrypt(roomId, payload.optString("text")));
                break;
            case "file_attachment":
                payload.put("url", encrypt(roomId, payload.optString("url")));
                payload.put("file_name", encrypt(roomId, payload.optString("file_name")));
                payload.put("caption", encrypt(roomId, payload.optString("caption")));
                payload.put("encryption_key", encrypt(roomId, payload.optString("encryption_key")));
                break;
            case "contact_person":
                payload.put("name", encrypt(roomId, payload.optString("name")));
                payload.put("value", encrypt(roomId, payload.optString("value")));
                break;
            case "location":
                payload.put("name", encrypt(roomId, payload.optString("name")));
                payload.put("address", encrypt(roomId, payload.optString("address")));
                payload.put("encrypted_latitude", encrypt(roomId, payload.optString("latitude")));
                payload.put("encrypted_longitude", encrypt(roomId, payload.optString("longitude")));
                payload.put("latitude", 0.0);
                payload.put("longitude", 0.0);
                payload.put("map_url", encrypt(roomId, payload.optString("map_url")));
                break;
            case "custom":
                payload.put("content", encrypt(roomId, payload.optJSONObject("content").toString()));
                break;
        }
        return payload.toString();
    }

    private static String encrypt(long roomId, String message) throws Exception {
        GroupConversation conversation = getConversation(roomId);
        byte[] encrypted = conversation.encrypt(message.getBytes());
        saveConversation(roomId, conversation);
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    private static void saveConversation(long roomId, GroupConversation conversation) {
        QiscusE2EDataStore.getInstance()
                .saveGroupConversation(roomId, conversation)
                .toCompletable()
                .await();
    }

    private static GroupConversation getConversation(long roomId) throws Exception {
        GroupConversation conversation = QiscusE2EDataStore.getInstance().getGroupConversation(roomId).toBlocking().first();
        if (conversation == null) {
            return createConversation(roomId);
        }

        return conversation;
    }

    private static GroupConversation createConversation(long roomId) throws Exception {
        String deviceId = QiscusMyBundleCache.getInstance().getDeviceId();
        GroupConversation groupConversation = new GroupConversation();
        groupConversation.initSender(new HashId(deviceId.getBytes()));
        notifyMembers(roomId, Base64.encodeToString(groupConversation.getSenderKey(), Base64.DEFAULT), true);
        saveConversation(roomId, groupConversation);
        return groupConversation;
    }

    private static void notifyMembers(long roomId, String senderKey, boolean needReply) {
        QiscusChatRoom groupRoom = getChatRoom(roomId);
        QiscusAccount account = Qiscus.getQiscusAccount();
        Observable<QiscusChatRoom> singleRooms = Observable.from(groupRoom.getMember())
                .map(QiscusRoomMember::getEmail)
                .filter(email -> !email.equalsIgnoreCase(account.getEmail()))
                .flatMap(email -> {
                    QiscusChatRoom savedRoom = Qiscus.getDataStore().getChatRoom(email);
                    if (savedRoom != null) {
                        return Observable.just(savedRoom);
                    }
                    return QiscusApi.getInstance().getChatRoom(email, null, null)
                            .doOnNext(qiscusChatRoom -> qiscusChatRoom.setLastComment(null))
                            .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom));
                });

        singleRooms.doOnNext(qiscusChatRoom -> {
            QiscusComment qiscusComment = QiscusComment.generateGroupSenderKeyMessage(qiscusChatRoom.getId(),
                    groupRoom.getId(), groupRoom.getName(), senderKey, needReply);
            qiscusComment.setState(QiscusComment.STATE_PENDING);
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
            QiscusResendCommentHandler.tryResendPendingComment();
        }).toList().toCompletable().await();
    }

    private static void sendSenderKey(String recipientEmail, long roomId, String senderKey, boolean needReply) {
        QiscusChatRoom groupRoom = getChatRoom(roomId);
        Observable.fromCallable(() -> Qiscus.getDataStore().getChatRoom(recipientEmail))
                .flatMap(savedRoom -> {
                    if (savedRoom != null) {
                        return Observable.just(savedRoom);
                    }
                    return QiscusApi.getInstance().getChatRoom(recipientEmail, null, null)
                            .doOnNext(qiscusChatRoom -> qiscusChatRoom.setLastComment(null))
                            .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom));
                })
                .doOnNext(qiscusChatRoom -> {
                    QiscusComment qiscusComment = QiscusComment.generateGroupSenderKeyMessage(qiscusChatRoom.getId(),
                            groupRoom.getId(), groupRoom.getName(), senderKey, needReply);
                    qiscusComment.setState(QiscusComment.STATE_PENDING);
                    Qiscus.getDataStore().addOrUpdate(qiscusComment);
                    QiscusResendCommentHandler.tryResendPendingComment();
                })
                .toCompletable().await();

    }

    private static void exchangeSenderKey(String recipientEmail, long roomId) {
        try {
            GroupConversation conversation = QiscusE2EDataStore.getInstance()
                    .getGroupConversation(roomId).toBlocking().first();
            if (conversation == null) {
                conversation = createConversation(roomId);
            } else {
                sendSenderKey(recipientEmail, roomId,
                        Base64.encodeToString(conversation.getSenderKey(), Base64.DEFAULT), true);
            }
            saveConversation(roomId, conversation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static QiscusChatRoom getChatRoom(long roomId) {
        QiscusChatRoom savedChatRoom = Qiscus.getDataStore().getChatRoom(roomId);
        if (savedChatRoom != null) {
            return savedChatRoom;
        }

        return QiscusApi.getInstance().getChatRoom(roomId)
                .doOnNext(qiscusChatRoom -> qiscusChatRoom.setLastComment(null))
                .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom))
                .toBlocking()
                .first();
    }

    public static void decrypt(QiscusComment comment) {
        if (!QiscusEncryptionHandler.decryptAbleType(comment)) {
            return;
        }

        //Don't decrypt if we already have it
        QiscusComment decryptedComment = Qiscus.getDataStore().getComment(comment.getUniqueId());
        if (decryptedComment != null) {
            comment.setMessage(decryptedComment.getMessage());
            comment.setExtraPayload(decryptedComment.getExtraPayload());
            return;
        }

        //We only can decrypt opponent's comment
        if (comment.isMyComment()) {
            QiscusEncryptionHandler.setPlaceHolder(comment);
            return;
        }

        //Only decrypt if room is group type
        QiscusChatRoom chatRoom = getChatRoom(comment.getRoomId());
        if (!chatRoom.isGroup() || chatRoom.isChannel()) {
            return;
        }

        //Decrypt message
        if (QiscusEncryptionHandler.encryptAbleMessage(comment.getRawType())) {
            comment.setMessage(decrypt(comment.getRoomId(), comment.getSenderEmail(), comment.getMessage()));
        }

        //Decrypt payload
        if (!comment.getRawType().equals("text")) {
            try {
                comment.setExtraPayload(decrypt(comment.getRoomId(), comment.getSenderEmail(),
                        comment.getRawType(), new JSONObject(comment.getExtraPayload())));
            } catch (Exception ignored) {
                // ignored
            }
        }

        //We need to update payload with saved comment
        if (comment.getRawType().equals("reply")) {
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(comment);
                comment.setMessage(payload.optString("text", comment.getMessage()));
                QiscusComment savedRepliedComment = Qiscus.getDataStore()
                        .getComment(payload.optLong("replied_comment_id"));
                if (savedRepliedComment != null) {
                    payload.put("replied_comment_message", savedRepliedComment.getMessage());
                    try {
                        payload.put("replied_comment_payload", new JSONObject(savedRepliedComment.getExtraPayload()));
                    } catch (Exception ignored) {
                        //ignored
                    }
                    comment.setExtraPayload(payload.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (comment.getRawType().equals("file_attachment")) {
            // We need to replace text with url from payload
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(comment);
                comment.setMessage(String.format("[file] %s [/file]", payload.optString("url")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (comment.getRawType().equals("location")) {
            // We need to replace text with data from payload
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(comment);
                comment.setMessage(
                        payload.optString("name") + " - " + payload.optString("address")
                                + "\n" + payload.optString("map_url")
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (comment.getRawType().equals("contact_person")) {
            // We need to replace text with data from payload
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(comment);
                comment.setMessage(
                        payload.optString("name") + " - " + payload.optString("value")
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String decrypt(long roomId, String senderEmail, String rawType, JSONObject payload) {
        try {
            switch (rawType) {
                case "reply":
                    payload.put("text", decrypt(roomId, senderEmail, payload.optString("text")));
                    break;
                case "file_attachment":
                    payload.put("url", decrypt(roomId, senderEmail, payload.optString("url")));
                    payload.put("file_name", decrypt(roomId, senderEmail, payload.optString("file_name")));
                    payload.put("caption", decrypt(roomId, senderEmail, payload.optString("caption")));
                    payload.put("encryption_key", decrypt(roomId, senderEmail, payload.optString("encryption_key")));
                    break;
                case "contact_person":
                    payload.put("name", decrypt(roomId, senderEmail, payload.optString("name")));
                    payload.put("value", decrypt(roomId, senderEmail, payload.optString("value")));
                    break;
                case "location":
                    payload.put("name", decrypt(roomId, senderEmail, payload.optString("name")));
                    payload.put("address", decrypt(roomId, senderEmail, payload.optString("address")));
                    Double latitude = Double.parseDouble(decrypt(roomId, senderEmail, payload.optString("encrypted_latitude")));
                    Double longitude = Double.parseDouble(decrypt(roomId, senderEmail, payload.optString("encrypted_longitude")));
                    payload.put("latitude", latitude);
                    payload.put("longitude", longitude);
                    payload.put("encrypted_latitude", latitude.toString());
                    payload.put("encrypted_longitude", longitude.toString());
                    payload.put("map_url", decrypt(roomId, senderEmail, payload.optString("map_url")));
                    break;
                case "custom":
                    payload.put("content", new JSONObject(decrypt(roomId, senderEmail, payload.optString("content"))));
                    break;
            }
        } catch (Exception ignored) {
            // Ignored
        }
        return payload.toString();
    }

    private static String decrypt(long roomId, String senderEmail, String message) {
        try {
            byte[] rawData = Base64.decode(message.getBytes(), Base64.DEFAULT);
            GroupConversation conversation = getConversation(roomId);
            byte[] decrypted = conversation.decrypt(rawData);
            saveConversation(roomId, conversation);
            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            exchangeSenderKey(senderEmail, roomId);
            return QiscusEncryptionHandler.ENCRYPTED_PLACE_HOLDER;
        }
    }

    public static void updateRecipient(String sender, long roomId, String senderKey, boolean needReply) {
        try {
            GroupConversation conversation = QiscusE2EDataStore.getInstance().getGroupConversation(roomId).toBlocking().first();
            if (conversation == null) {
                conversation = createConversation(roomId);
            } else if (needReply) {
                sendSenderKey(sender, roomId, Base64.encodeToString(conversation.getSenderKey(), Base64.DEFAULT), false);
            }
            conversation.initRecipient(Base64.decode(senderKey, Base64.DEFAULT));
            saveConversation(roomId, conversation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
