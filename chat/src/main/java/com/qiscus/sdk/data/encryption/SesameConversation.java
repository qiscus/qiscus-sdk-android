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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * This class represents a Sesame conversation
 */

public class SesameConversation {
    final Bundle senderBundle;
    final String senderName;
    final HashId selfDeviceId;
    final String recipientName;
    BundlePublicCollection recipientPublic = null;
    HashMap<HashId, Ratchet> ratchets = new HashMap<>();
    HashMap<HashId, SesameConversationSecret> secrets = new HashMap<>();
    HashMap<String, SesameContact> contacts = new HashMap<>();

    /**
     * Creates a new SesameConversation
     *
     * @param senderName      The sender username
     * @param selfDeviceId    The sender device id
     * @param senderBundle    The sender bundle
     * @param recipientName   The recipient username
     * @param recipientPublic The recipient public bundles for all devices
     */
    public SesameConversation(String senderName, HashId selfDeviceId, Bundle senderBundle, String recipientName, BundlePublicCollection recipientPublic) {
        this.senderName = senderName;
        this.senderBundle = senderBundle;
        this.selfDeviceId = selfDeviceId;
        this.recipientName = recipientName;
        this.recipientPublic = recipientPublic;
    }

    public void initializeSender() throws IllegalDataSizeException, NoSuchAlgorithmException, EncryptionFailedException {
        populateSecrets(true);
    }

    public void populateSecrets(boolean isSender) throws NoSuchAlgorithmException, IllegalDataSizeException, EncryptionFailedException {
        secrets = new HashMap<>();
        ratchets = new HashMap<>();
        Set<HashId> pubIds = recipientPublic.getIds();
        Iterator<HashId> it = pubIds.iterator();
        while (it.hasNext()) {
            HashId id = it.next();
            BundlePublic bundlePublic = recipientPublic.get(id);

            Ratchet r = new Ratchet();
            if (isSender) {
                KeyPair ephKey = new KeyPair();
                SharedKey sk = X3dhMessage.getSharedKeySender(ephKey,
                        senderBundle.bundlePrivate,
                        bundlePublic,
                        Constants.RidonSesameSharedKey);

                r.initSender(bundlePublic.spk.publicKey, new Key(sk.key));
                byte[] m = Constants.RidonSecretMessage.getBytes();

                byte[] ad = new byte[PublicKey.ESIZE * 2];
                System.arraycopy(senderBundle.bundlePublic.identity.encode(), 0, ad, 0, PublicKey.ESIZE);
                System.arraycopy(bundlePublic.identity.encode(), 0, ad, PublicKey.ESIZE, PublicKey.ESIZE);

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(ad);

                byte[] adHash = md.digest();

                X3dhMessage msg = new X3dhMessage(senderBundle.bundlePublic.identity, ephKey.publicKey, sk.preKeyId, sk.key, m, adHash);
                byte[] msgEncoded = msg.encode();
                SesameConversationSecret secret = new SesameConversationSecret(msgEncoded, msgEncoded.length, adHash);
                secrets.put(id, secret);
            } else {
                secrets.put(id, new SesameConversationSecret());
            }
            ratchets.put(id, r);
        }
    }

    public byte[] initializeRecipient(HashId id, byte[] message) throws SignatureException, IllegalDataSizeException, InvalidKeyException, NoSuchAlgorithmException, EncryptionFailedException {
        ByteBuffer b = ByteBuffer.wrap(message, 0, 8);
        if (b.getInt() != Constants.RidonMagix) {
            throw new SignatureException();
        }

        b = ByteBuffer.wrap(message, 8, 8);
        int size = b.getInt();
        byte[] msg = new byte[size];
        int remaining = message.length - size - 16;
        byte[] data = new byte[remaining];

        System.arraycopy(message, 16, msg, 0, size);
        System.arraycopy(message, size + 16, data, 0, remaining);

        populateSecrets(false);
        BundlePublic pub = recipientPublic.get(id);
        X3dhMessage x3dhMessage = X3dhMessage.decode(msg);

        final byte[] sharedKeyRecipient = X3dhMessage.getSharedKeyRecipient(x3dhMessage.ephKey, x3dhMessage.preKeyId, senderBundle.bundlePrivate, pub, Constants.RidonSesameSharedKey);
        KeyPair pair = new KeyPair(senderBundle.bundlePrivate.spk, senderBundle.bundlePublic.spk.publicKey);

        Ratchet r = ratchets.get(id);
        r.initRecipient(pair, new Key(sharedKeyRecipient));
        ratchets.put(id, r);

        byte[] ad = new byte[Key.ESIZE * 2];
        System.arraycopy(pub.identity.encode(), 0, ad, 0, Key.ESIZE);
        System.arraycopy(senderBundle.bundlePublic.identity.encode(), 0, ad, Key.ESIZE, Key.ESIZE);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(ad);

        byte[] adHash = md.digest();

        SesameConversationSecret secret = new SesameConversationSecret(adHash);
        secrets.put(id, secret);

        return data;
    }

    public void deleteStaleUser(String username) {
        Date zero = new Date(0);

        SesameContact contact = contacts.get(username);
        if (contact != null && contact.staleTime.equals(zero)) {
            contacts.remove(username);
        }
    }

    public void addNewDeviceIfEmpty(String id) {
        SesameContact contact = contacts.get(id);
        if (contact == null) return;

        if (contact.devices.isEmpty()) {
            Set<HashId> pubIds = recipientPublic.getIds();
            Iterator<HashId> it = pubIds.iterator();
            while (it.hasNext()) {
                HashId hashId = it.next();
                BundlePublic bundlePublic = recipientPublic.get(hashId);
                SesameDevice device = new SesameDevice(hashId, bundlePublic.identity);
                contact.devices.put(hashId, device);
            }
        } else {
            Set<HashId> remoteIds = recipientPublic.getIds();
            Iterator<HashId> it = remoteIds.iterator();
            while (it.hasNext()) {
                HashId idRemote = it.next();
                BundlePublic bundlePublic = recipientPublic.get(idRemote);

                Set<HashId> localIds = contact.devices.keySet();
                Iterator<HashId> itLocal = localIds.iterator();
                while (itLocal.hasNext()) {
                    HashId idLocal = itLocal.next();
                    SesameDevice device = contact.devices.get(idLocal);
                    if (!device.publicKey.equals(bundlePublic.identity)) {
                        contact.devices.remove(idLocal);
                        contact.devices.put(idLocal, new SesameDevice(idLocal, bundlePublic.identity));
                    }
                }
            }
        }
    }

    public void prepEncrypt() {
        if (senderName == recipientName) {
            return;
        }

        SesameContact contact = contacts.get(recipientName);
        if (contact != null) {
            deleteStaleUser(recipientName);
            addNewDeviceIfEmpty(recipientName);
        } else {
            addNewContactIfEmpty(recipientName);
        }
    }

    public void addNewContactIfEmpty(String id) {
        SesameContact contact = new SesameContact(id);

        Set<HashId> pubIds = recipientPublic.getIds();
        Iterator<HashId> it = pubIds.iterator();
        while (it.hasNext()) {
            HashId hashId = it.next();
            BundlePublic bundlePublic = recipientPublic.get(hashId);
            contact.devices.put(hashId, new SesameDevice(hashId, bundlePublic.identity));
        }
        contacts.put(id, contact);
    }

    public HashId fetchActiveSession(String id) {
        SesameContact contact = contacts.get(id);
        if (contact != null && contact.activeSessions.size() > 0) {
            return contact.activeSessions.get(0);
        }
        return null;
    }

    public byte[] encrypt(byte[] data) throws EncryptionFailedException, NoSuchAlgorithmException, IllegalDataSizeException, InvalidKeyException, IOException {
        prepEncrypt();
        HashMap<HashId, byte[]> retval = new HashMap<>();
        HashId id = fetchActiveSession(recipientName);

        if (id != null) {
            byte[] msg = doEncrypt(id, data);
            retval.put(id, msg);
        } else {
            Set<HashId> ids = secrets.keySet();
            Iterator<HashId> it = ids.iterator();
            while (it.hasNext()) {
                HashId hashId = it.next();
                SesameConversationSecret secret = secrets.get(hashId);
                retval.put(hashId, doEncrypt(hashId, data));
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(retval.size());
        out.write(buffer.array());

        Set<HashId> ids = retval.keySet();
        Iterator<HashId> it = ids.iterator();
        while (it.hasNext()) {
            HashId hashId = it.next();
            byte[] enc = retval.get(hashId);
            buffer.clear();
            buffer.putInt(enc.length);
            out.write(buffer.array());
            out.write(enc);
        }

        return out.toByteArray();
    }

    /**
     * Prepares a HashMap of the recipient and the encrypted data
     *
     * @param raw
     * @return
     * @throws IOException
     */
    public static HashMap<HashId, byte[]> unpackEncrypted(final byte[] raw) throws IOException, InvalidKeyException {
        ByteArrayInputStream input = new ByteArrayInputStream(raw);
        HashMap<HashId, byte[]> retval = new HashMap<>();

        byte[] b = new byte[4];
        input.read(b);
        ByteBuffer buffer = ByteBuffer.wrap(b);
        int size = buffer.getInt();

        byte[] h = new byte[HashId.SIZE];
        for (int i = 0; i < size; i++) {
            input.read(b);
            buffer = ByteBuffer.wrap(b);
            int len = buffer.getInt();
            byte[] data = new byte[len];
            input.read(data);
            System.arraycopy(data, HashId.SIZE, h, 0, HashId.SIZE);
            retval.put(new HashId(h), data);
        }
        return retval;
    }

    public byte[] doEncrypt(HashId id, byte[] data) throws IOException, EncryptionFailedException, IllegalDataSizeException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        SesameConversationSecret secret = secrets.get(id);
        if (secret == null) {
            throw new EncryptionFailedException();
        }

        Ratchet ratchet = ratchets.get(id);
        if (ratchet == null) {
            throw new EncryptionFailedException();
        }

        byte[] msg = ratchet.encrypt(data, secret.ad);

        ByteArrayOutputStream ret = new ByteArrayOutputStream();
        ret.write(selfDeviceId.raw());
        ret.write(id.raw());

        if (secret.size > 0 && secret.size == secret.message.length) {
            ByteBuffer b = ByteBuffer.allocate(8);
            b.putInt(Constants.RidonMagix);
            ret.write(b.array());
            b.clear();
            b.putInt(secret.size);
            ret.write(b.array());
            b.clear();
            ret.write(secret.message);
        }
        ret.write(msg);

        return ret.toByteArray();
    }

    public void resetActiveSession(HashId id) {
        SesameConversationSecret secret = secrets.get(id);
        if (secret != null && secret.message.length > 0) {
            secret.message = new byte[0];
            secrets.put(id, secret);
        }

        ArrayList<HashId> list = new ArrayList<>();
        list.add(id);

        final SesameContact contact = contacts.get(recipientName);
        if (contact != null) {
            for (int i = 0; i < contact.activeSessions.size(); i++) {
                HashId activeId = contact.activeSessions.get(i);
                if (activeId != null && !id.equals(activeId)) {
                    list.add(activeId);
                }
            }
            contact.activeSessions = list;
            contacts.put(recipientName, contact);
        }
    }

    public byte[] decrypt(byte[] raw) throws NoSuchAlgorithmException, IllegalDataSizeException, EncryptionFailedException, SignatureException, InvalidKeyException, DecryptionFailedException, IOException, SesameMessageRecipientMismatchException, AuthenticationException, TooManySkippedMessagesException {
        ByteArrayInputStream input = new ByteArrayInputStream(raw);
        byte[] b = new byte[HashId.SIZE];
        input.read(b);
        HashId senderId = new HashId(b);
        input.read(b);

        HashId recipientId = new HashId(b);

        if (!recipientId.equals(selfDeviceId)) {
            throw new SesameMessageRecipientMismatchException();
        }

        byte[] data = new byte[input.available()];
        input.read(data);
        SesameConversationSecret secret = secrets.get(senderId);
        if (secret == null) {
            final byte[] msgData = initializeRecipient(senderId, data);
            secret = secrets.get(senderId); // this should be populated now after init
            data = msgData;
        }

        Ratchet ratchet = ratchets.get(senderId);
        if (ratchet == null) {
            throw new DecryptionFailedException();
        }
        byte[] decrypted = ratchet.decrypt(data, secret.ad);
        resetActiveSession(senderId);
        return decrypted;
    }
}