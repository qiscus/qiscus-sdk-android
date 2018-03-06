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

package com.qiscus.sdk.data.encryption.core;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class Bundle {
    public final BundlePrivate bundlePrivate;
    public final BundlePublic bundlePublic;

    public Bundle() throws IllegalDataSizeException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        KeyPair pair = new KeyPair();
        SignedPreKey spk = new SignedPreKey(pair.privateKey);

        bundlePrivate = new BundlePrivate(pair.privateKey, spk.getPrivateKey());
        bundlePublic = new BundlePublic(pair.publicKey, spk.getPublic());
    }

    public void populatePreKeys() throws IllegalDataSizeException, NoSuchAlgorithmException, InvalidKeyException {
        for (int i = 0; i < Constants.MaxPreKeys; i++) {
            KeyPair pair = new KeyPair();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(pair.publicKey.raw());
            PreKeyId id = new PreKeyId(md.digest());

            bundlePrivate.insert(id, pair.privateKey);
            bundlePublic.insert(id, pair.publicKey);
        }
    }
}
