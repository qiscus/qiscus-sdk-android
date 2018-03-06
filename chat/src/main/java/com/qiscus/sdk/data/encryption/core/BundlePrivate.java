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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BundlePrivate {
    public final PrivateKey identity;
    public final PrivateKey spk;
    Map<PreKeyId, PrivateKey> preKeys = new HashMap<>();

    public BundlePrivate(PrivateKey identity, PrivateKey spk) {
        this.identity = identity;
        this.spk = spk;
    }

    public int size() {
        return preKeys.size();
    }

    public void insert(PreKeyId id, PrivateKey key) {
        preKeys.put(id, key);
    }

    public PrivateKey fetch(PreKeyId id) {
        PrivateKey k = preKeys.get(id);
        if (k != null) {
            preKeys.remove(id);
        }
        return k;
    }

    public byte[] encode() {
        int len = 2 * 65 + // identity + spk
                4 + // prekeys.size
                (preKeys.size() * (65 + 32)); // prekeys
        byte[] result = new byte[len];

        int offset = 0;
        System.arraycopy(identity.encode(), 0, result, offset, 65);
        offset += 65;
        System.arraycopy(spk.encode(), 0, result, offset, 65);
        offset += 65;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(preKeys.size());
        System.arraycopy(buffer.array(), 0, result, offset, 4);
        offset += 4;

        Set<PreKeyId> keyIds = preKeys.keySet();
        Iterator<PreKeyId> it = keyIds.iterator();
        while (it.hasNext()) {
            PreKeyId id = it.next();
            PrivateKey k = preKeys.get(id);

            System.arraycopy(id.raw(), 0, result, offset, 32);
            offset += 32;
            System.arraycopy(k.encode(), 0, result, offset, 65);
            offset += 65;
        }

        return result;
    }

    public static final BundlePrivate decode(byte[] raw) throws IllegalDataSizeException, InvalidKeyException {
        int offset = 0;
        PrivateKey identity = PrivateKey.decode(raw, offset);
        offset += 65;
        PrivateKey spk = PrivateKey.decode(raw, offset);
        offset += 65;

        BundlePrivate bp = new BundlePrivate(identity, spk);
        ByteBuffer b = ByteBuffer.wrap(raw, offset, 4);
        int len = b.getInt();
        offset += 4;
        for (int i = 0; i < len; i++) {
            byte[] preKeyId = new byte[32];
            System.arraycopy(raw, offset, preKeyId, 0, 32);
            offset += 32;
            bp.insert(new PreKeyId(preKeyId), PrivateKey.decode(raw, offset));
            offset += 65;
        }
        return bp;
    }
}