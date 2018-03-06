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
import java.util.Arrays;

import static java.lang.System.arraycopy;

public class Key {
    protected byte[] key;

    public static final int SIZE = 32;
    public static final int ESIZE = SIZE + 1;


    public Key() {
        key = new byte[32];
    }

    /**
     * Creates a new Key object
     *
     * @param key Key byte sequence
     */
    public Key(final byte[] key) throws IllegalDataSizeException {
        this.key = new byte[32];
        if (key.length != 32) {
            throw new IllegalDataSizeException();
        }
        System.arraycopy(key, 0, this.key, 0, 32);
    }

    /**
     * Serializes the key
     *
     * @return byte sequence containing serialized key
     */
    public byte[] encode() {
        byte[] retval = new byte[33];

        retval[0] = 0x5;
        System.arraycopy(key, 0, retval, 1, 32);
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
     * Returns the raw key
     *
     * @return byte sequence containing the key
     */
    public final byte[] raw() {
        return key;
    }

    /**
     * Clears the key
     */
    public void clear() {
        for (int i = 0; i < key.length; i++) {
            key[i] = 0;
        }
    }

    /**
     * Checks equality of another Key with this Key
     *
     * @param other Other Key object
     * @return whether this Key is the same with the other
     */
    public boolean equals(Key other) {
        if (other == null) {
            return false;
        }
        return Arrays.equals(key, other.key);
    }

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

        return Arrays.equals(key, ((Key) other).key);
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < key.length; i++) {
            h |= key[i];
        }
        return h;
    }

    public boolean isNull() {
        int c = 0;

        for (int i = 0; i < key.length; i++) {
            c += key[i];
            if (c > 0) {
                return false;
            }
        }
        return true;
    }
}
