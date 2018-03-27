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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * This class stores a mapping between a device id and a public bundle
 */

public class BundlePublicCollection implements Serializable {
    HashMap<HashId, BundlePublic> map = new HashMap<>();

    public BundlePublicCollection() {
    }

    public BundlePublicCollection(HashId id, BundlePublic bundlePublic) {
        map.put(id, bundlePublic);
    }

    public void put(HashId id, BundlePublic bundlePublic) {
        map.put(id, bundlePublic);
    }

    public BundlePublic get(HashId id) {
        return map.get(id);
    }

    public Set<HashId> getIds() {
        return map.keySet();
    }

    public static BundlePublicCollection decode(byte[] raw) throws IOException, IllegalDataSizeException,
            InvalidKeyException, SignatureException {
        BundlePublicCollection me = new BundlePublicCollection();
        ByteArrayInputStream input = new ByteArrayInputStream(raw);
        byte[] b = new byte[4];
        input.read(b);
        ByteBuffer buffer = ByteBuffer.wrap(b);
        int size = buffer.getInt();

        byte[] b64 = new byte[HashId.SIZE];
        for (int i = 0; i < size; i++) {
            input.read(b64);
            HashId id = new HashId(b64);
            input.read(b);
            buffer = ByteBuffer.wrap(b);
            int pubRawSize = buffer.getInt();
            byte[] pubRaw = new byte[pubRawSize];
            input.read(pubRaw);
            BundlePublic pub = BundlePublic.decode(pubRaw);
            me.map.put(id, pub);
        }
        return me;
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(map.size());
        output.write(buffer.array());

        Set<HashId> ids = map.keySet();
        Iterator<HashId> it = ids.iterator();
        while (it.hasNext()) {
            HashId hashId = it.next();
            BundlePublic pub = map.get(hashId);
            byte[] pubRaw = pub.encode();
            output.write(hashId.raw());
            buffer.clear();
            buffer.putInt(pubRaw.length);
            output.write(buffer.array());
            output.write(pubRaw);
        }
        return output.toByteArray();
    }
}
