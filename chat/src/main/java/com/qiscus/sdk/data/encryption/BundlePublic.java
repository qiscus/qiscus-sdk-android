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

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BundlePublic {
    public final PublicKey identity;
    public final SignedPreKeyPublic spk;
    Map<PreKeyId, PublicKey> preKeys = new HashMap<>();

    public BundlePublic(PublicKey identity, SignedPreKeyPublic spk) {
        this.identity = identity;
        this.spk = spk;
    }

    public void insert(PreKeyId id, PublicKey key) {
        preKeys.put(id, key);
    }

    public PreKey pop() throws NullPointerException {
        Set<PreKeyId> keyIds = preKeys.keySet();
        Iterator<PreKeyId> it = keyIds.iterator();
        if (it.hasNext()) {
            PreKeyId id = it.next();
            PublicKey k = preKeys.get(id);
            PreKey retval = new PreKey(id, k);

            preKeys.remove(id);
            return retval;
        }
        throw new NullPointerException();
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
        return identity.equals(((BundlePublic) other).identity);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public boolean verify() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return spk.verify(identity);
    }

    public byte[] encode() {
        int len = 33 + 97 + // identity + spk
                4 + // prekeys.size
                (preKeys.size() * (33 + 32)); // prekeys
        byte[] result = new byte[len];

        int offset = 0;
        System.arraycopy(identity.encode(), 0, result, offset, 33);
        offset += 33;
        System.arraycopy(spk.encode(), 0, result, offset, 97);
        offset += 97;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(preKeys.size());
        System.arraycopy(buffer.array(), 0, result, offset, 4);
        offset += 4;

        Set<PreKeyId> keyIds = preKeys.keySet();
        Iterator<PreKeyId> it = keyIds.iterator();
        while (it.hasNext()) {
            PreKeyId id = it.next();
            PublicKey k = preKeys.get(id);

            System.arraycopy(id.raw(), 0, result, offset, 32);
            offset += 32;
            System.arraycopy(k.encode(), 0, result, offset, 33);
            offset += 33;
        }
        return result;
    }

    public static final BundlePublic decode(byte[] raw) throws IllegalDataSizeException, InvalidKeyException, SignatureException {
        int offset = 0;
        PublicKey identity = PublicKey.decode(raw, offset);
        offset += 33;
        SignedPreKeyPublic spk = SignedPreKeyPublic.decode(raw, offset);
        offset += 97;

        BundlePublic bp = new BundlePublic(identity, spk);
        ByteBuffer b = ByteBuffer.wrap(raw, offset, 4);
        int len = b.getInt();
        offset += 4;
        for (int i = 0; i < len; i++) {
            byte[] preKeyId = new byte[32];
            System.arraycopy(raw, offset, preKeyId, 0, 32);
            offset += 32;
            bp.insert(new PreKeyId(preKeyId), PublicKey.decode(raw, offset));
            offset += 33;
        }
        return bp;
    }
}