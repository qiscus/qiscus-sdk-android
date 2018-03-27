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
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static javax.crypto.Mac.getInstance;

/**
 * This represents a Ratchet mechanism
 */
public class Ratchet implements Serializable {
    public KeyPair pairSender;
    public PublicKey publicRecipient;

    public Key rootKey;
    public Key chainKeySender;
    public Key chainKeyRecipient;
    public Key nextHeader;
    public Key header;
    public int messageNumberSender;
    public int messageNumberRecipient;

    public int chainLength;
    public HashMap<Key, RatchetMessageBuffer> skippedMessages;

    public Ratchet() {
        skippedMessages = new HashMap<>();
    }

    public void build(KeyPair pairSender,
                      PublicKey publicRecipient,
                      Key rootKey,
                      Key chainKeySender,
                      Key chainKeyRecipient,
                      Key nextHeader,
                      Key header,
                      int messageNumberSender,
                      int messageNumberRecipient,
                      int chainLength,
                      HashMap<Key, RatchetMessageBuffer> skippedMessages) {
        this.pairSender = pairSender;
        this.publicRecipient = publicRecipient;
        this.rootKey = rootKey;
        this.chainKeySender = chainKeySender;
        this.chainKeyRecipient = chainKeyRecipient;
        this.nextHeader = nextHeader;
        this.header = header;
        this.messageNumberSender = messageNumberSender;
        this.messageNumberRecipient = messageNumberRecipient;
        this.chainLength = chainLength;
        this.skippedMessages = skippedMessages;
    }

    /**
     * Initializes a Ratchet from the sender side
     *
     * @param remotePublicKey PublicKey of the receiver
     * @param rootKey
     */
    public void initSender(PublicKey remotePublicKey, Key rootKey) throws IllegalDataSizeException {
        KeyPair pair = new KeyPair();
        byte[] dh = pair.privateKey.shareSecret(remotePublicKey);

        Key rk = rootKey;
        if (rk == null) {
            rk = this.rootKey;
        }

        byte[] salt = Constants.getRidonSalt512();
        Kdf kdf = Kdf.kdfSha512(dh, rk.raw());
        byte[] kdfResult = kdf.get(Constants.RidonRatchetInfo, 64);

        this.pairSender = pair;
        this.publicRecipient = remotePublicKey;
        byte[] k = new byte[32];
        System.arraycopy(kdfResult, 0, k, 0, 32);
        this.rootKey = new Key(k);
        System.arraycopy(kdfResult, 32, k, 0, 32);
        this.chainKeySender = new Key(k);
        this.messageNumberSender = 0;
        this.messageNumberRecipient = 0;
        this.chainLength = 0;
    }

    /**
     * Initializes Ratchet as a Recipient
     *
     * @param pair    KeyPair of the recipient
     * @param rootKey The first root key
     */
    public void initRecipient(KeyPair pair, Key rootKey) {
        this.pairSender = pair;
        if (rootKey != null) {
            this.rootKey = rootKey;
        }

        messageNumberSender = 0;
        messageNumberRecipient = 0;
        chainLength = 0;
    }

    /**
     * Encodes a Ratchet into a byte sequence
     *
     * @return byte sequence
     * @throws IOException
     */
    public byte[] encode() throws IOException {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        s.write(pairSender.encode());
        s.write(publicRecipient.encode());
        s.write(rootKey.encode());
        s.write(chainKeySender.encode());
        s.write(chainKeyRecipient.encode());
        s.write(nextHeader.encode());
        s.write(header.encode());

        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(messageNumberSender);
        s.write(b.array());
        b.clear();
        b.putInt(messageNumberRecipient);
        s.write(b.array());
        b.clear();
        b.putInt(chainLength);
        s.write(b.array());
        b.clear();
        b.putInt(skippedMessages.size());
        s.write(b.array());
        Set<Key> keyIds = skippedMessages.keySet();
        Iterator<Key> it = keyIds.iterator();
        while (it.hasNext()) {
            Key id = it.next();
            RatchetMessageBuffer buf = skippedMessages.get(id);

            s.write(id.encode());
            s.write(buf.encode());
        }
        return s.toByteArray();
    }

    /**
     * Decodes raw data into a Ratchet
     *
     * @param raw The raw data
     * @return Ratchet
     */
    public static Ratchet decode(final byte[] raw) throws InvalidKeyException, IOException, IllegalDataSizeException {
        ByteArrayInputStream in = new ByteArrayInputStream(raw);

        byte[] b = new byte[65 + 33];
        in.read(b);
        KeyPair pairSender = KeyPair.decode(b);
        b = new byte[33];
        in.read(b);
        PublicKey publicRecipient = PublicKey.decode(b, 0);
        in.read(b);
        Key rootKey = Key.decode(b, 0);
        in.read(b);
        Key chainKeySender = Key.decode(b, 0);
        in.read(b);
        Key chainKeyRecipient = Key.decode(b, 0);
        in.read(b);
        Key nextHeader = Key.decode(b, 0);
        in.read(b);
        Key header = Key.decode(b, 0);
        b = new byte[4];
        in.read(b);
        ByteBuffer bf = ByteBuffer.wrap(b, 0, 4);
        int messageNumberSender = bf.getInt();

        in.read(b);
        bf = ByteBuffer.wrap(b, 0, 4);
        int messageNumberRecipient = bf.getInt();

        in.read(b);
        bf = ByteBuffer.wrap(b, 0, 4);
        int chainLength = bf.getInt();

        in.read(b);
        bf = ByteBuffer.wrap(b, 0, 4);
        int size = bf.getInt();

        HashMap<Key, RatchetMessageBuffer> skippedMessages = new HashMap<>();
        b = new byte[33];
        byte[] b37 = new byte[37];
        for (int i = 0; i < size; i++) {
            in.read(b);
            Key key = Key.decode(b, 0);
            in.read(b37);
            RatchetMessageBuffer buffer = RatchetMessageBuffer.decode(b37);
            skippedMessages.put(key, buffer);
        }

        Ratchet r = new Ratchet();
        r.build(pairSender, publicRecipient, rootKey,
                chainKeySender, chainKeyRecipient, nextHeader,
                header, messageNumberSender, messageNumberRecipient,
                chainLength, skippedMessages);
        return r;
    }

    /**
     * Encrypts plain text with additional data
     *
     * @param plainText Plain text
     * @param ad        Additional data
     * @return
     */
    public byte[] encrypt(final byte[] plainText, final byte[] ad) throws NoSuchAlgorithmException,
            InvalidKeyException, IllegalDataSizeException, EncryptionFailedException, IOException {
        byte[] m = new byte[1];
        m[0] = 1;

        SecretKeySpec hks = new SecretKeySpec(chainKeySender.raw(), "HmacSHA512");
        Mac mac = getInstance("HmacSHA512");
        mac.init(hks);
        mac.update(m);
        byte[] sum = mac.doFinal();

        byte[] b = new byte[32];
        System.arraycopy(sum, 0, b, 0, 32);
        chainKeySender = new Key(b);
        byte[] mk = new byte[32];
        System.arraycopy(sum, 32, mk, 0, 32);


        RatchetMessageHeader header = new RatchetMessageHeader(pairSender.publicKey, chainLength, messageNumberSender);
        byte[] hs = header.encode();
        byte[] adAll = new byte[ad.length + hs.length];
        System.arraycopy(ad, 0, adAll, 0, ad.length);
        System.arraycopy(hs, 0, adAll, ad.length, hs.length);

        Aead aead = new Aead(mk, Constants.RidonRatchetInfo);
        byte[] encrypted = aead.encrypt(plainText, adAll);
        messageNumberSender++;

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        s.write(hs);
        s.write(encrypted);
        return s.toByteArray();
    }

    public Key findSkippedKey(RatchetMessageHeader header) {
        try {
            RatchetMessageBuffer buffer = skippedMessages.get(new Key(header.publicKey.raw()));
            if (buffer == null) {
                return null;
            }
            if (buffer.number != header.messageNumber) {
                return null;
            }
            skippedMessages.remove(header.publicKey);
            return buffer.key;
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] trySkippedMessages(RatchetMessageHeader header, ByteArrayInputStream in, byte[] ad)
            throws IOException, DecryptionFailedException, IllegalDataSizeException,
            TooManySkippedMessagesException, AuthenticationException {
        Key mk = findSkippedKey(header);
        if (mk == null) {
            return null;
        }

        Aead aead = new Aead(mk.raw(), Constants.RidonRatchetInfo);

        byte[] hs = header.encode();
        byte[] adAll = new byte[ad.length + hs.length];
        System.arraycopy(ad, 0, adAll, 0, ad.length);
        System.arraycopy(hs, 0, adAll, ad.length, hs.length);

        int size = in.available();
        byte[] data = new byte[size];
        in.read(data);
        return aead.decrypt(data, adAll);
    }

    public byte[] decrypt(byte[] cipherText, byte[] ad) throws DecryptionFailedException, IOException,
            InvalidKeyException, IllegalDataSizeException, TooManySkippedMessagesException,
            NoSuchAlgorithmException, AuthenticationException, InvalidKeyException {
        ByteArrayInputStream in = new ByteArrayInputStream(cipherText);

        byte[] hs = new byte[RatchetMessageHeader.SIZE];
        in.read(hs);
        RatchetMessageHeader h = RatchetMessageHeader.decode(hs);

        byte[] e = trySkippedMessages(h, in, ad);

        if (e != null) {
            return e;
        }

        if (h.publicKey.equals(publicRecipient) == false) {
            skipMessages(chainLength);
            turn(h.publicKey);
        }
        skipMessages(h.messageNumber);

        SecretKeySpec hks = new SecretKeySpec(chainKeyRecipient.raw(), "HmacSHA512");
        byte[] m1 = new byte[1];
        m1[0] = 1;

        Mac m = getInstance("HmacSHA512");
        m.init(hks);
        m.update(m1);
        byte[] hmac = m.doFinal();

        byte[] part = new byte[32];
        System.arraycopy(hmac, 0, part, 0, 32);
        chainKeyRecipient = new Key(part);
        System.arraycopy(hmac, 32, part, 0, 32);

        int size = in.available();
        byte[] data = new byte[size];
        in.read(data);
        Aead aead = new Aead(part, Constants.RidonRatchetInfo);

        byte[] adAll = new byte[ad.length + hs.length];
        System.arraycopy(ad, 0, adAll, 0, ad.length);
        System.arraycopy(hs, 0, adAll, ad.length, hs.length);

        byte[] decrypted = aead.decrypt(data, adAll);

        messageNumberRecipient++;

        return decrypted;
    }

    public void skipMessages(int num) throws TooManySkippedMessagesException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalDataSizeException {
        if (messageNumberRecipient + Constants.MaxSkippedMessages < num) {
            throw new TooManySkippedMessagesException();
        }

        if (chainKeyRecipient == null) {
            return;
        }

        while (true) {
            if (messageNumberRecipient >= num) {
                break;
            }

            byte[] m = new byte[1];
            m[0] = 1;

            SecretKeySpec hks = new SecretKeySpec(chainKeyRecipient.raw(), "HmacSHA512");
            Mac mac = getInstance("HmacSHA512");
            mac.init(hks);
            mac.update(m);
            byte[] sum = mac.doFinal();

            byte[] b = new byte[32];
            System.arraycopy(sum, 0, b, 0, 32);
            chainKeyRecipient = new Key(b);
            System.arraycopy(sum, 32, b, 0, 32);

            RatchetMessageBuffer buffer = new RatchetMessageBuffer(messageNumberRecipient, new Key(b));
            skippedMessages.put(new Key(publicRecipient.raw()), buffer);
            messageNumberRecipient++;
        }
    }

    public void turn(PublicKey remote) throws IllegalDataSizeException {
        chainLength = messageNumberSender;
        messageNumberSender = 0;
        messageNumberRecipient = 0;
        publicRecipient = remote;

        byte[] dh = pairSender.privateKey.shareSecret(remote);

        byte[] salt = Constants.getRidonSalt512();
        Kdf kdf = Kdf.kdfSha512(dh, rootKey.raw());
        byte[] kdfResult = kdf.get(Constants.RidonRatchetInfo, 64);

        KeyPair pair = new KeyPair();
        byte[] b = new byte[32];

        System.arraycopy(kdfResult, 0, b, 0, 32);
        rootKey = new Key(b);

        System.arraycopy(kdfResult, 32, b, 0, 32);
        chainKeyRecipient = new Key(b);
        pairSender = pair;

        dh = pairSender.privateKey.shareSecret(publicRecipient);
        kdf = Kdf.kdfSha512(dh, rootKey.raw());
        kdfResult = kdf.get(Constants.RidonRatchetInfo, 64);

        pair = new KeyPair();
        System.arraycopy(kdfResult, 0, b, 0, 32);
        rootKey = new Key(b);
        System.arraycopy(kdfResult, 32, b, 0, 32);
        chainKeySender = new Key(b);

    }

}
