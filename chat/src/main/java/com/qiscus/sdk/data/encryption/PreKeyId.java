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
 * This class represents a prekey id
 */

public class PreKeyId {
    final byte[] preKeyId;

    public PreKeyId(byte[] id) throws InvalidKeyException {
        if (id.length != 32) {
            throw new InvalidKeyException();
        }
        preKeyId = id.clone();
    }

    public PreKeyId(byte[] id, int offset) throws InvalidKeyException {
        if (id.length + offset < 32) {
            throw new InvalidKeyException();
        }
        preKeyId = new byte[32];
        System.arraycopy(id, offset, preKeyId, 0, 32);
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

        return Arrays.equals(preKeyId, ((PreKeyId) other).preKeyId);
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < preKeyId.length; i++) {
            result |= hashCode(preKeyId[i]);
        }

        return result;
    }

    public final byte[] raw() {
        return preKeyId;
    }

    private static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }
}
