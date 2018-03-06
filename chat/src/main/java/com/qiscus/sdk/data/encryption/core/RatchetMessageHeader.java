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

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;

public class RatchetMessageHeader {
    public final PublicKey publicKey;
    public final int chainLength;
    public final int messageNumber;
    public static final int SIZE = 4 + 4 + 33;

    public RatchetMessageHeader(final PublicKey publicKey, int chainLength, int messageNumber) {
        this.publicKey = publicKey;
        this.chainLength = chainLength;
        this.messageNumber = messageNumber;
    }

    public byte[] encode() {
        byte[] ret = new byte[SIZE];
        System.arraycopy(publicKey.encode(), 0, ret, 0, 33);
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(chainLength);
        System.arraycopy(b.array(), 0, ret, 33, 4);
        b.clear();
        b.putInt(messageNumber);
        System.arraycopy(b.array(), 0, ret, 33 + 4, 4);
        return ret;
    }

    public static RatchetMessageHeader decode(byte[] raw) throws InvalidKeyException, IllegalDataSizeException {
        if (raw.length != (SIZE)) {
            throw new IllegalDataSizeException();
        }

        PublicKey k = PublicKey.decode(raw, 0);
        ByteBuffer b = ByteBuffer.wrap(raw, 33, 4);
        int chainLength = b.getInt();
        b = ByteBuffer.wrap(raw, 33 + 4, 4);
        int messageNumber = b.getInt();

        RatchetMessageHeader h = new RatchetMessageHeader(k, chainLength, messageNumber);
        return h;
    }


}
