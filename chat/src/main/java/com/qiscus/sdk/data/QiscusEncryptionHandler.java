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
import android.util.Base64;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.encryption.QiscusE2EDataStore;
import com.qiscus.sdk.data.encryption.QiscusE2ERestApi;
import com.qiscus.sdk.data.encryption.QiscusMyBundleCache;
import com.qiscus.sdk.data.encryption.core.BundlePublic;
import com.qiscus.sdk.data.encryption.core.BundlePublicCollection;
import com.qiscus.sdk.data.encryption.core.HashId;
import com.qiscus.sdk.data.encryption.core.SesameConversation;
import com.qiscus.sdk.data.encryption.core.SesameSenderDevice;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.util.QiscusErrorLogger;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.schedulers.Schedulers;

/**
 * Created on : March 01, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QiscusEncryptionHandler {
    private QiscusEncryptionHandler() {

    }

    public static void initKeyPair() throws Exception {
        if (Qiscus.hasSetupUser() && Qiscus.getChatConfig().isEnableEndToEndEncryption()) {
            if (QiscusMyBundleCache.getInstance().getSenderDevice() != null) {
                return;
            }

            String userId = Qiscus.getQiscusAccount().getEmail();
            String deviceId = QiscusMyBundleCache.getInstance().getDeviceId();
            saveSenderDevice(new SesameSenderDevice(new HashId(deviceId.getBytes()), userId));
        }
    }

    public static void encrypt(String recipientId, QiscusComment comment) {
        comment.setMessage(encrypt(recipientId, comment.getMessage()));
    }

    public static String encrypt(String recipientId, String message) {
        try {
            SesameConversation conversation = createConversation(recipientId);
            conversation.initializeSender();
            byte[] encrypted = conversation.encrypt(message.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return message;
        }
    }

    public static void decrypt(QiscusComment comment) {
        if (comment.isMyComment() || !comment.getRawType().equals("text")) {
            return;
        }
        comment.setMessage(decrypt(comment.getSenderEmail(), comment.getMessage()));
    }

    public static void decrypt(List<QiscusComment> comments) {
        QiscusAccount account = Qiscus.getQiscusAccount();
        Map<String, List<QiscusComment>> data = new HashMap<>();
        for (QiscusComment comment : comments) {
            if (!comment.getSenderEmail().equals(account.getEmail()) && comment.getRawType().equals("text")) {
                String userId = comment.getSenderEmail();
                if (!data.containsKey(userId)) {
                    data.put(userId, new ArrayList<>());
                }
                data.get(userId).add(comment);
            }
        }

        for (String userId : data.keySet()) {
            BundlePublicCollection bundle = getBundle(userId);
            for (QiscusComment comment : data.get(userId)) {
                try {
                    byte[] unpackedData = unpackData(comment.getMessage());
                    if (unpackedData == null) {
                        continue;
                    }

                    int index = comments.indexOf(comment);
                    if (index >= 0) {
                        SesameConversation conversation = createConversation(userId, bundle);
                        comments.get(index).setMessage(new String(conversation.decrypt(unpackedData)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String decrypt(String senderId, String message) {
        try {
            byte[] unpackedData = unpackData(message);
            if (unpackedData == null) {
                return message;
            }
            SesameConversation conversation = createConversation(senderId);
            byte[] decrypted = conversation.decrypt(unpackedData);
            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return message;
        }
    }

    private static void saveSenderDevice(SesameSenderDevice senderDevice) throws Exception {
        String userId = Qiscus.getQiscusAccount().getEmail();
        String deviceId = QiscusMyBundleCache.getInstance().getDeviceId();

        //Save bundle public collection to server
        byte[] bundlePublicRaw = senderDevice.getBundle().bundlePublic.encode();
        BundlePublicCollection bundlePublicCollection =
                new BundlePublicCollection(new HashId(deviceId.getBytes()), BundlePublic.decode(bundlePublicRaw));

        QiscusE2ERestApi.getInstance()
                .saveBundlePublicCollection(bundlePublicCollection)
                .flatMap(bundlePublicCollection1 ->
                        QiscusE2EDataStore.getInstance().saveBundlePublicCollection(userId, bundlePublicCollection1))
                .doOnNext(bundlePublicCollection1 -> QiscusMyBundleCache.getInstance().saveSenderDevice(senderDevice))
                .subscribeOn(Schedulers.io())
                .subscribe(bundlePublicCollection1 -> {
                }, QiscusErrorLogger::print);
    }

    private static byte[] unpackData(String message) throws IOException, InvalidKeyException {
        byte[] rawData = Base64.decode(message.getBytes(), Base64.DEFAULT);
        HashMap<HashId, byte[]> unpacked = SesameConversation.unpackEncrypted(rawData);
        Set<HashId> hashIds = unpacked.keySet();
        Iterator<HashId> it = hashIds.iterator();
        byte[] data = null;
        while (it.hasNext()) {
            HashId id = it.next();
            if (id.equals(QiscusMyBundleCache.getInstance().getSenderDevice().id)) {
                data = unpacked.get(id);
                break;
            }
        }
        return data;
    }

    //TODO better way to get bundle without everytime request to server
    private static BundlePublicCollection getBundle(String userId) {
        return QiscusE2EDataStore.getInstance()
                .getBundlePublicCollection(userId)
                .flatMap(bundlePublicCollection -> {
                    //Disabled to always get it from server
                    /*if (bundlePublicCollection != null) {
                        return Observable.just(bundlePublicCollection);
                    }*/
                    return QiscusE2ERestApi.getInstance()
                            .getBundlePublicCollection(userId)
                            .flatMap(bundlePublicCollection1 ->
                                    QiscusE2EDataStore.getInstance()
                                            .saveBundlePublicCollection(userId, bundlePublicCollection1));
                })
                .toBlocking()
                .first();
    }

    private static SesameConversation createConversation(String userId) throws Exception {
        return createConversation(userId, getBundle(userId));
    }

    private static SesameConversation createConversation(String userId, BundlePublicCollection bundle) throws Exception {
        QiscusAccount account = Qiscus.getQiscusAccount();
        SesameSenderDevice senderDevice = QiscusMyBundleCache.getInstance().getSenderDevice();
        return new SesameConversation(
                account.getEmail(),
                senderDevice.id,
                senderDevice.getBundle(),
                userId,
                bundle
        );
    }
}
