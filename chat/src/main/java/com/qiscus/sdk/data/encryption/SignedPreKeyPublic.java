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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * This class is a representation of public part of SignedPreKey
 */
public class SignedPreKeyPublic {
    public PublicKey publicKey;
    public Signature signature;

    /**
     * Creates a new SignedPreKeyPublic instance
     *
     * @param pubKey The public key
     * @param sig    The signature
     */
    public SignedPreKeyPublic(PublicKey pubKey, Signature sig) throws SignatureException {
        publicKey = pubKey;
        signature = sig;
    }

    /**
     * Verifies a PublicKey against this SignedPreKey
     *
     * @param pub The PublicKey to inspect
     * @return whether the public key is verified for this SignedPreKey
     */
    public boolean verify(PublicKey pub) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        return pub.verify(publicKey.encode(), signature);
    }

    /**
     * Serialize a SignedPreKeyPublic
     *
     * @return byte sequence containing serialized SignedPreKeyPublic
     */
    public byte[] encode() {
        int size = 33 + 64;
        byte[] ret = new byte[size];
        System.arraycopy(publicKey.encode(), 0, ret, 0, 33);
        System.arraycopy(signature.getBytes(), 0, ret, 33, 64);
        return ret;
    }

    public static SignedPreKeyPublic decode(byte[] raw, int offset) throws InvalidKeyException, IllegalDataSizeException, SignatureException {
        if (raw[offset] == 0x5 && raw.length >= (64 + 33) + offset) {
            PublicKey publicKey = PublicKey.decode(raw, offset);
            byte[] sig = new byte[64];
            System.arraycopy(raw, offset + 33, sig, 0, 64);
            SignedPreKeyPublic spk = new SignedPreKeyPublic(publicKey, new Signature(sig));

            return spk;
        }
        throw new IllegalDataSizeException();
    }
}