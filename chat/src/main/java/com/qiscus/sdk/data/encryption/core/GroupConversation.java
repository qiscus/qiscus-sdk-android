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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static javax.crypto.Mac.getInstance;

public class GroupConversation {
    private HashId senderId;

    // This is only used by the sender
    private KeyPair signatureKey;
    private Key chainKeySender;

    // This is only used by the recipient
    private HashMap<HashId, PublicKey> signatureMap = new HashMap<>();

    private byte[] senderKey = new byte[64];
    private HashMap<HashId, Key> chainKeyMap = new HashMap<>();

    /**
     * Generates key in the conversation. This is done by sender when initiating conversation within a group
     *
     * @throws IllegalDataSizeException
     */
    void initSender(HashId senderId) throws IllegalDataSizeException {
        SecureRandom random = new SecureRandom();

        byte[] k = new byte[32];
        random.nextBytes(k);
        this.senderId = senderId;

        chainKeySender = new Key(k);
        signatureKey = new KeyPair();
        signatureMap.put(senderId, signatureKey.publicKey);

        System.arraycopy(chainKeySender.raw(), 0, senderKey, 0, 32);
        System.arraycopy(signatureKey.publicKey.raw(), 0, senderKey, 32, 32);
    }

    /**
     * Returns senderKey. The sender key is encoded as sender id and then followed with the
     * sender key byte array.
     *
     * @return byte array containing sender id and sender key
     */
    public byte[] getSenderKey() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(senderId.raw());
        output.write(senderKey);
        return output.toByteArray();
    }

    /**
     * Initializes GroupConversation as sender. This may be called from a saved session on disk
     *
     * @param signatureKey The signature key
     * @param chainKey     The chain key
     */
    public void initSender(HashId senderId, KeyPair signatureKey, Key chainKey) {
        this.senderId = senderId;
        this.signatureKey = signatureKey;
        signatureMap.put(senderId, signatureKey.publicKey);
        chainKeySender = chainKey;
    }

    /**
     * Initializes GroupConversation as recipient. This may be called from a saved session on disk
     *
     * @param senderId           The sender id
     * @param signaturePublicKey The signature public key belongs to the sender id
     * @param chainKey           The chain key belongs to the sender id
     */
    public void initRecipient(HashId senderId, PublicKey signaturePublicKey, Key chainKey) {
        this.senderId = senderId;
        signatureMap.put(senderId, signaturePublicKey);
        chainKeyMap.put(senderId, chainKey);
    }

    /**
     * Initializes GroupConversation as recipient. This may be called when receiving a new sender key
     *
     * @param senderKey The sender key belongs to the sender id
     */
    public void initRecipient(byte[] senderKey) throws IllegalDataSizeException, InvalidKeyException {
        byte[] data = new byte[HashId.SIZE];

        System.arraycopy(senderKey, 0, data, 0, 64);
        senderId = new HashId(data);

        data = new byte[32];
        System.arraycopy(senderKey, 64, data, 0, 32);
        chainKeyMap.put(senderId, new Key(data));

        System.arraycopy(senderKey, 64 + 32, data, 0, 32);
        signatureMap.put(senderId, new PublicKey(data));

    }

    private byte[] getMessageKey() throws IllegalDataSizeException, NoSuchAlgorithmException, InvalidKeyException {
        return getMessageKey(null);
    }

    private byte[] getMessageKey(HashId sender) throws NoSuchAlgorithmException, InvalidKeyException, IllegalDataSizeException {
        SecretKeySpec hks;

        Key ck;
        if (sender == null) {
            ck = chainKeySender;
        } else {
            ck = chainKeyMap.get(sender);
            if (ck == null) {
                throw new InvalidKeyException();
            }
        }

        hks = new SecretKeySpec(ck.raw(), "HmacSHA256");

        byte[] m1 = new byte[1];
        m1[0] = 1;

        Mac m = getInstance("HmacSHA256");
        m.init(hks);
        m.update(m1);
        byte[] messageKey = m.doFinal();

        m1[0] = 2;
        m.init(hks);
        m.update(m1);
        if (sender != null) {
            chainKeyMap.put(sender, new Key(m.doFinal()));
        } else {
            chainKeySender = new Key(m.doFinal());
        }

        return messageKey;
    }

    /**
     * Encrypts plain text in a group conversation. The resulting data is formed from the HashId of
     * the sender, then followed with cipher text, and finally appended with a 64-byte
     * signature
     *
     * @param plainText
     * @return byte array containing cipher text
     * @throws InvalidKeyException
     * @throws IllegalDataSizeException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws SignatureException
     * @throws IOException
     */
    public byte[] encrypt(byte[] plainText) throws InvalidKeyException, IllegalDataSizeException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, SignatureException, IOException {
        byte[] iv = new byte[16];
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(getMessageKey(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        int padLength = 16 - plainText.length % 16;
        byte[] padding = new byte[plainText.length + padLength];
        System.arraycopy(plainText, 0, padding, 0, plainText.length);
        for (int i = 0; i < padLength; i++) {
            padding[plainText.length + i] = (byte) padLength;
        }
        byte[] encrypted = cipher.doFinal(padding);

        Signature sig = signatureKey.privateKey.sign(encrypted);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(senderId.raw());
        output.write(encrypted);
        output.write(sig.getBytes());
        return output.toByteArray();
    }

    /**
     * Decrypts cipher text in a group conversation
     *
     * @param cipherText
     * @return byte array containing plain text
     * @throws DecryptionFailedException
     * @throws AuthenticationException
     * @throws IllegalDataSizeException
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public byte[] decrypt(byte[] cipherText) throws DecryptionFailedException, AuthenticationException,
            IllegalDataSizeException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        byte[] iv = new byte[16];

        if (cipherText.length <= 64) {
            throw new IllegalDataSizeException();
        }

        int pos = cipherText.length - 64;

        byte[] check = new byte[64];
        System.arraycopy(cipherText, 0, check, 0, 64);
        HashId sender = new HashId(check);

        System.arraycopy(cipherText, pos, check, 0, 64);
        Signature signature = new Signature(check);
        PublicKey k = signatureMap.get(sender);
        if (k == null) {
            throw new InvalidKeyException();
        }

        // Cipher text starts just after the hash id of the sender
        if (k.verify(cipherText, HashId.SIZE, pos - HashId.SIZE, signature) == false) {
            throw new AuthenticationException();
        }

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(getMessageKey(sender), "AES");

        byte[] decrypted;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            decrypted = cipher.doFinal(cipherText, 64, cipherText.length - 128);
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