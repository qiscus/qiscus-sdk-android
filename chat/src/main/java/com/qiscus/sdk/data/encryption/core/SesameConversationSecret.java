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

/**
 * This class represents a Sesame conversation secret
 */

public class SesameConversationSecret implements Serializable {
    int size = 0;
    byte[] message = new byte[0];
    byte[] ad = new byte[0];

    public SesameConversationSecret(byte[] message, int size, byte[] ad) {
        this.size = size;
        this.ad = ad;
        this.message = message;
    }

    public SesameConversationSecret() {
        // Empty
    }

    public SesameConversationSecret(byte[] ad) {
        this.ad = ad;
    }
}
