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

/**
 * This class represents a hash id
 */
public class HashId {
    public static final int SIZE = 64;
    final byte[] hashId;

    public HashId(byte[] id) throws InvalidKeyException {
        if (id.length != SIZE) {
            throw new InvalidKeyException();
        }
        hashId = id.clone();
    }

    public HashId(byte[] id, int offset) throws InvalidKeyException {
        if (id.length + offset < SIZE) {
            throw new InvalidKeyException();
        }
        hashId = new byte[SIZE];
        System.arraycopy(id, offset, hashId, 0, SIZE);
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

        return Arrays.equals(hashId, ((HashId) other).hashId);
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < hashId.length; i++) {
            result |= hashCode(hashId[i]);
        }

        return result;
    }

    public final byte[] raw() {
        return hashId;
    }

    private static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }
}