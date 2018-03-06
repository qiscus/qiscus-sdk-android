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

public final class Constants {
    public static final String RidonSalt = "RidonSalt";
    public static final int MaxPreKeys = 10;
    public static final String X3DhMessageInfo = "RidonX3DMessage";
    public static final String RidonRatchetInfo = "Ridon";
    public static final int MaxSkippedMessages = 1024 * 1024;
    public static final String RidonSesameSharedKey = "RidonSesame-SharedKey";
    public static final String RidonSecretMessage = "R";
    public static final int RidonMagix = 0x201801;

    public static byte[] getRidonSalt512() {
        byte[] salt = new byte[64];
        System.arraycopy(RidonSalt.getBytes(Charset.forName("UTF-8")), 0, salt, 0,
                RidonSalt.length() > 64 ? 64 : RidonSalt.length());
        return salt;
    }
}
