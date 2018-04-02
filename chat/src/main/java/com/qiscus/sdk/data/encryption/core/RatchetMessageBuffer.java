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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;

public class RatchetMessageBuffer implements Serializable {
    public final int number;
    public final Key key;

    public RatchetMessageBuffer(int number, Key key) {
        this.number = number;
        this.key = key;
    }

    public byte[] encode() {
        byte[] ret = new byte[4 + 33];
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(number);
        System.arraycopy(b.array(), 0, ret, 0, 4);
        System.arraycopy(key.encode(), 0, ret, 4, 33);
        return ret;
    }

    public static RatchetMessageBuffer decode(final byte[] raw) throws InvalidKeyException, IllegalDataSizeException {
        ByteBuffer b = ByteBuffer.wrap(raw, 0, 4);
        int number = b.getInt();
        return new RatchetMessageBuffer(number, Key.decode(raw, 4));
    }

    @Override
    public boolean equals(Object other) {
        return ((RatchetMessageBuffer) other).number == number &&
                ((RatchetMessageBuffer) other).key.equals(key);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}



