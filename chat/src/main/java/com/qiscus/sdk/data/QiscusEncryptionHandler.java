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

import com.qiscus.sdk.data.model.QiscusComment;

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

    //TODO implement the encryption algorithm
    public static void encrypt(QiscusComment comment) {
        comment.setMessage(encrypt(comment.getMessage()));
    }

    //TODO implement the decryption algorithm
    public static void decrypt(QiscusComment comment) {
        comment.setMessage(decrypt(comment.getMessage()));
    }

    public static String encrypt(String message) {
        try {
            return Base64.encodeToString(message.getBytes(), Base64.DEFAULT);
        } catch (Exception e) {
            return message;
        }
    }

    public static String decrypt(String message) {
        try {
            return new String(Base64.decode(message.getBytes(), Base64.DEFAULT));
        } catch (Exception e) {
            return message;
        }
    }
}
