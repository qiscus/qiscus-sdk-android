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

import org.apache.commons.codec.binary.Hex;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;

public class PublicKey extends Key {

    public PublicKey(final byte[] key) throws IllegalDataSizeException {
        super(key);
    }

    /**
     * Decodes a raw data into a PublicKey object
     *
     * @param raw    Raw data byte sequence
     * @param offset The offset of the data we want to inspect
     * @return a new PublicKey object
     */
    public static PublicKey decode(byte[] raw, int offset) throws InvalidKeyException, IllegalDataSizeException {
        return new PublicKey(Key.decode(raw, offset).raw());
    }

    /**
     * Returns a hex string representation of the raw public key
     *
     * @return a hex string
     */
    public final String hexString() {
        return new String(Hex.encodeHex(keyBytes));
    }

    /**
     * Verifies a message given a signature
     *
     * @param message   The message to be verified
     * @param signature The signature to verify
     * @return Whether the signature is valid or not
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public boolean verify(byte[] message, Signature signature) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {

        byte[] y = new byte[32];
        byte[] s1 = new byte[32];
        byte[] sig = signature.getBytes();
        System.arraycopy(sig, 0, s1, 0, 32);
        byte[] s2 = new byte[32];
        System.arraycopy(sig, 32, s2, 0, 32);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(message);
        md.update(keyBytes);
        byte[] digest = md.digest();

        Curve.verify(y, s2, digest, keyBytes);

        MessageDigest md2 = MessageDigest.getInstance("SHA-256");
        md2.update(y);

        return Arrays.equals(md2.digest(), s1);
    }
}