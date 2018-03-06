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

import java.nio.charset.Charset;

import at.favre.lib.crypto.HKDF;

public class Kdf {
    byte[] key;

    byte[] prepareData(byte[] secret, byte[] salt) {
        byte[] data;

        data = new byte[32 + secret.length];
        for (int i = 0; i < 32; i++) {
            data[i] = -1;
        }
        System.arraycopy(secret, 0, data, 32, secret.length);

        return data;
    }

    public static Kdf kdfSha512(byte[] secret, byte[] salt) {
        Kdf kdf = new Kdf();

        byte[] data = kdf.prepareData(secret, salt);
        kdf.key = HKDF.fromHmacSha512().extract(salt, data);

        return kdf;
    }

    public byte[] get(final String info, int length) {
        return HKDF.fromHmacSha512().expand(key, info.getBytes(Charset.forName("UTF-8")), length);
    }
}