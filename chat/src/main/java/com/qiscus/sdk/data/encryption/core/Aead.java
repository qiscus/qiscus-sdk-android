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

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static javax.crypto.Mac.getInstance;

public class Aead {
    private byte[] key;
    private String info;
    private byte[] iv;
    private byte[] authKey;
    private byte[] opKey;

    /**
     * Creates an Aead object
     *
     * @param key  The key in byte array
     * @param info The string containing the information of the key
     */
    public Aead(byte[] key, String info) {
        this.info = info;
        this.key = key;
        iv = new byte[16];
        authKey = new byte[32];
        opKey = new byte[16];
    }

    private void generateKeys() {
        byte[] salt = Constants.getRidonSalt512();
        Kdf kdf = Kdf.kdfSha512(key, salt);
        byte[] kdfResult = kdf.get(info, 64);

        System.arraycopy(kdfResult, 0, opKey, 0, 16);
        System.arraycopy(kdfResult, 16, authKey, 0, 32);
        System.arraycopy(kdfResult, 48, iv, 0, 16);
    }

    /**
     * Encrypts plain text with additional data
     *
     * @param plainText the plain text to be encrypted
     * @param ad        The additional data
     * @return byte array of encrypted data followed with authentication data
     * @throws EncryptionFailedException
     */
    public byte[] encrypt(byte[] plainText, byte[] ad) throws EncryptionFailedException {
        generateKeys();

        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(opKey, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            int padLength = 16 - plainText.length % 16;
            byte[] padding = new byte[plainText.length + padLength];
            System.arraycopy(plainText, 0, padding, 0, plainText.length);
            for (int i = 0; i < padLength; i++) {
                padding[plainText.length + i] = (byte) padLength;
            }
            byte[] encrypted = cipher.doFinal(padding);

            SecretKeySpec hks = new SecretKeySpec(authKey, "HmacSHA512");
            Mac m = getInstance("HmacSHA512");
            m.init(hks);
            m.update(ad);
            byte[] hmac = m.doFinal(encrypted);

            byte[] retval = new byte[encrypted.length + hmac.length];
            System.arraycopy(encrypted, 0, retval, 0, encrypted.length);
            System.arraycopy(hmac, 0, retval, encrypted.length, hmac.length);
            for (int i = 0; i < encrypted.length; i++) {
                encrypted[i] = 0;
            }
            return retval;
        } catch (Exception e) {
            throw new EncryptionFailedException();
        }

    }

    /**
     * Decrypts and authenticate a cipher text with additional data
     *
     * @param cipherText The cipher text
     * @param ad         The additional data
     * @return decrypted byte array
     * @throws EncryptionFailedException
     */
    public byte[] decrypt(byte[] cipherText, byte[] ad) throws DecryptionFailedException,
            IllegalDataSizeException, AuthenticationException {
        generateKeys();

        if (cipherText.length <= 64) {
            throw new IllegalDataSizeException();
        }

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(opKey, "AES");

        int pos = cipherText.length - 64;

        byte[] check = new byte[64];
        System.arraycopy(cipherText, pos, check, 0, 64);

        byte[] hmac;
        try {
            SecretKeySpec hks = new SecretKeySpec(authKey, "HmacSHA512");
            Mac m = getInstance("HmacSHA512");
            m.init(hks);
            m.update(ad);
            m.update(cipherText, 0, cipherText.length - 64);
            hmac = m.doFinal();
        } catch (Exception e) {
            throw new DecryptionFailedException();
        }

        if (!Arrays.equals(hmac, check)) {
            throw new AuthenticationException();
        }

        byte[] decrypted;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            decrypted = cipher.doFinal(cipherText, 0, cipherText.length - 64);
        } catch (Exception e) {
            throw new DecryptionFailedException();
        }

        int resultLength = decrypted.length;
        if (resultLength <= 0) {
            throw new DecryptionFailedException();
        }
        int endIndex = decrypted[resultLength - 1];
        if (endIndex <= 16) {
            if (1 < endIndex) {
                for (int i = resultLength - endIndex; i < resultLength; i++) {
                    if (decrypted[resultLength - 1] != decrypted[i]) {
                        throw new DecryptionFailedException();
                    }
                }
            }
            byte[] result = new byte[resultLength - endIndex];
            System.arraycopy(decrypted, 0, result, 0, resultLength - endIndex);
            for (int i = 0; i < resultLength; i++) {
                decrypted[i] = 0;
            }
            return result;
        }

        throw new DecryptionFailedException();
    }
}