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

package com.qiscus.sdk.data.encryption;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Random;

public final class KeyPair {
    public final PrivateKey privateKey;
    public final PublicKey publicKey;


    public KeyPair() throws IllegalDataSizeException {
        Random r = new SecureRandom();
        byte[] priv = new byte[32];
        r.nextBytes(priv);

        byte[] pubKey = new byte[32];
        byte[] privSignature = new byte[32];
        Curve.keygen(pubKey, privSignature, priv);
        privateKey = new PrivateKey(priv, privSignature);
        publicKey = new PublicKey(pubKey);
    }

    public KeyPair(final PrivateKey privateKey, final PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream ss = new ByteArrayOutputStream();
        ss.write(privateKey.encode());
        ss.write(publicKey.encode());
        return ss.toByteArray();
    }

    public static KeyPair decode(final byte[] raw) throws InvalidKeyException, IOException, IllegalDataSizeException {
        PrivateKey privateKey = PrivateKey.decode(raw, 0);
        PublicKey publicKey = PublicKey.decode(raw, 65);
        return new KeyPair(privateKey, publicKey);
    }

    @Override
    public boolean equals(Object other) {
        return ((KeyPair) other).privateKey.equals(privateKey) &&
                ((KeyPair) other).publicKey.equals(publicKey);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
