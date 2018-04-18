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

package com.qiscus.sdk.data;

import android.os.Environment;
import android.support.annotation.RestrictTo;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.encryption.core.Aead;
import com.qiscus.sdk.data.encryption.core.HashId;
import com.qiscus.sdk.util.QiscusFileUtil;

import java.io.File;

/**
 * Created on : April 18, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QiscusFileEncryptionHandler {
    public static final String FILES_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + Qiscus.getAppsName() + File.separator + "Files";

    private QiscusFileEncryptionHandler() {

    }

    public static File encrypt(HashId hashId, File file) throws Exception {
        Aead fileEncryptor = new Aead(hashId.raw(), Qiscus.getAppId().toLowerCase());
        byte[] raw = fileEncryptor.encrypt(QiscusFileUtil.readFileToByteArray(file), "FILE_ATTACHMENT".getBytes());
        File encryptedFile = new File(FILES_PATH + File.separator + ".encrypted", file.getName());
        QiscusFileUtil.writeByteArrayToFile(encryptedFile, raw);
        return encryptedFile;
    }

    public static File decrypt(HashId hashId, File encryptedFile) throws Exception {
        Aead fileDecryptor = new Aead(hashId.raw(), Qiscus.getAppId().toLowerCase());
        byte[] raw = fileDecryptor.decrypt(QiscusFileUtil.readFileToByteArray(encryptedFile), "FILE_ATTACHMENT".getBytes());
        File file = new File(FILES_PATH + File.separator + ".decrypted", encryptedFile.getName());
        QiscusFileUtil.writeByteArrayToFile(file, raw);
        return file;
    }
}
