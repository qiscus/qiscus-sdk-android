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
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * This class represents signed pre-keys
 */
public class SignedPreKey {

    final SignedPreKeyPublic pub;
    final PrivateKey priv;

    public SignedPreKey(PrivateKey key) throws IllegalDataSizeException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        KeyPair preKey = new KeyPair();

        Signature sig = key.sign(preKey.publicKey.encode());

        pub = new SignedPreKeyPublic(preKey.publicKey, sig);
        priv = preKey.privateKey;
    }

    public final SignedPreKeyPublic getPublic() {
        return pub;
    }

    public final PrivateKey getPrivateKey() {
        return priv;
    }
}
