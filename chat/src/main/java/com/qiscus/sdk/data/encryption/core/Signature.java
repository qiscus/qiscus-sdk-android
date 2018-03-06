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

import java.security.SignatureException;

public final class Signature {
    final byte[] sig;

    /**
     * Creates a new Signature
     *
     * @param seq A byte sequence containing the signature value
     */
    public Signature(byte[] seq) throws SignatureException {
        if (seq.length != 64) {
            throw new SignatureException("Signature raw size invalid");
        }
        sig = seq.clone();
    }

    /**
     * Returns a byte sequence containing the raw data of the signature
     *
     * @return A byte sequence
     */
    public final byte[] getBytes() {
        return sig;
    }
}
