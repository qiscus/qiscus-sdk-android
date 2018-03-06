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
import java.util.Arrays;

import static java.lang.System.arraycopy;

public class Key {
    protected byte[] keyBytes;

    public static final int SIZE = 32;
    public static final int ESIZE = SIZE + 1;


    public Key() {
        keyBytes = new byte[32];
    }

    /**
     * Creates a new Key object
     *
     * @param keyBytes Key byte sequence
     */
    public Key(final byte[] keyBytes) throws IllegalDataSizeException {
        this.keyBytes = new byte[32];
        if (keyBytes.length != 32) {
            throw new IllegalDataSizeException();
        }
        arraycopy(keyBytes, 0, this.keyBytes, 0, 32);
    }

    /**
     * Serializes the keyBytes
     *
     * @return byte sequence containing serialized keyBytes
     */
    public byte[] encode() {
        byte[] retval = new byte[33];

        retval[0] = 0x5;
        arraycopy(keyBytes, 0, retval, 1, 32);
        return retval;
    }

    /**
     * Decodes an encoded data into a byte array
     *
     * @param raw    Raw data byte sequence
     * @param offset The offset of the data we want to inspect
     * @return a new PublicKey object
     */
    public static Key decode(byte[] raw, int offset) throws InvalidKeyException, IllegalDataSizeException {
        if (raw[offset] != 0x5) {
            throw new InvalidKeyException();
        }
        if (raw.length >= 32 + offset) {
            byte[] k = new byte[32];
            arraycopy(raw, offset + 1, k, 0, 32);
            return new Key(k);
        }
        throw new IllegalDataSizeException();
    }


    /**
     * Returns the raw keyBytes
     *
     * @return byte sequence containing the keyBytes
     */
    public final byte[] raw() {
        return keyBytes;
    }

    /**
     * Clears the keyBytes
     */
    public void clear() {
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = 0;
        }
    }

    /**
     * Checks equality of another Key with this Key
     *
     * @param other Other Key object
     * @return whether this Key is the same with the other
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (getClass() != other.getClass()) {
            return false;
        }

        return Arrays.equals(keyBytes, ((Key) other).keyBytes);
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < keyBytes.length; i++) {
            h |= keyBytes[i];
        }
        return h;
    }

    public boolean isNull() {
        int c = 0;

        for (int i = 0; i < keyBytes.length; i++) {
            c += keyBytes[i];
            if (c > 0) {
                return false;
            }
        }
        return true;
    }
}
