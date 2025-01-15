package com.qiscus.sdk.chat.core.data.local;

import static com.qiscus.utils.jupukdata.JupukData.getFileKey;

import android.annotation.SuppressLint;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import com.qiscus.sdk.chat.core.util.QiscusLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class QiscusDataManagement {

    private static final String TAG = QiscusDataManagement.class.getSimpleName();

    // Constants for encryption and decryption
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String ALGORITHM = "AES";
    private static final String BLOCK_MODE = "CBC";
    private static final String PADDING = "PKCS7Padding";
    private static final String TRANSFORMATIONS = ALGORITHM + "/" + BLOCK_MODE + "/" + PADDING;
    // Minimum length for the custom encryption key
    private static final int MINIMUM_KEY_LENGTH = 8;
    private static final int KEY_LENGTH = 32;
    // Length of the encryption key
    private static final KeyStore keyStore = createKeyStore();
    private static SecretKey secretKey = null;
    private static Cipher cipherDecrypt = null;
    private static Cipher cipherEncrypt = null;
    // Cipher instance for encryption
    private static String customKey = "";
    private static boolean usedOldMethod = false;

    private QiscusDataManagement() {
    }

    /**
     * key configuration
     */
    public static void setCustomKey(String key) {
        if (key.length() == MINIMUM_KEY_LENGTH) {
            customKey = key;
            return;
        }
        throw new IllegalArgumentException(
                "Key must be " + MINIMUM_KEY_LENGTH + " characters long. You're characters long is "
                        + key.length()
        );
    }

    public static void validateCustomKey(String key) {
        if (customKey.isEmpty()) setCustomKey(key.substring(0, MINIMUM_KEY_LENGTH));
    }

    public static void forceUsedOldMethod(boolean isOldMethod) {
        usedOldMethod = isOldMethod;
    }

    public static boolean isUsedOldMethod() {
        return usedOldMethod;
    }

    private static boolean isUsedNewMethod() {
        return !usedOldMethod && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Encrypt and decrypt
     */
    public static String generateSecretKey() {
        return (getFileKey() + customKey);
    }

    private static synchronized void validateCipherEncrypt() throws NoSuchPaddingException,
            NoSuchAlgorithmException {
        if (cipherEncrypt == null) cipherEncrypt = Cipher.getInstance(TRANSFORMATIONS);
    }

    private static synchronized void validateCipherDecrypt() throws NoSuchPaddingException,
            NoSuchAlgorithmException {
        if (cipherDecrypt == null) cipherDecrypt = Cipher.getInstance(TRANSFORMATIONS);
    }

    public static String encrypt(String plainText) {
        try {
            validateCipherEncrypt();

            final SecretKey secretKey;
            if (isUsedNewMethod()) secretKey = getOrCreateKeyStore();
            else secretKey = getOrCreateSecretKey();
            cipherEncrypt.init(Cipher.ENCRYPT_MODE, secretKey);

            final byte[] iv = cipherEncrypt.getIV();
            final byte[] raw = plainText.getBytes(StandardCharsets.UTF_8);
            final byte[] encryptedBytes = cipherEncrypt.doFinal(raw);

            final byte[] encryptedDataWithIV = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, encryptedDataWithIV, 0, iv.length);
            System.arraycopy(
                    encryptedBytes, 0, encryptedDataWithIV, iv.length, encryptedBytes.length
            );
            return Base64.encodeToString(encryptedDataWithIV, Base64.DEFAULT);

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException |
                 IllegalBlockSizeException | BadPaddingException e) {
            QiscusLogger.print(TAG, e.getMessage());
        }
        return "";
    }

    public static String decrypt(String encryptedText) {
        final byte[] raw = Base64.decode(encryptedText, Base64.DEFAULT);
        try {
            validateCipherDecrypt();

            final byte[] ivData = Arrays.copyOfRange(raw, 0, cipherDecrypt.getBlockSize());
            final byte[] encryptedData = Arrays.copyOfRange(raw, cipherDecrypt.getBlockSize(), raw.length);
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(ivData);

            final SecretKey secretKey;
            if (isUsedNewMethod()) secretKey = getOrCreateKeyStore();
            else secretKey = getOrCreateSecretKey();
            cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            final byte[] decryptedData = cipherDecrypt.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException e) {
            QiscusLogger.print(TAG, e.getMessage());
        }
        return "";
    }

    /**
     * Set secret key
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey createKey() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        final int purposes = KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT;
        @SuppressLint("WrongConstant") final KeyGenParameterSpec keyGenParameterSpec =
                new KeyGenParameterSpec.Builder(generateSecretKey(), purposes)
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(PADDING)
                        .setUserAuthenticationRequired(false)
                        .setRandomizedEncryptionRequired(true)
                        .build();

        final KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(keyGenParameterSpec);
        return keyGenerator.generateKey();
    }

    private static KeyStore createKeyStore() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                final KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
                keyStore.load(null);
                return keyStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | IOException |
                     CertificateException e) {
                QiscusLogger.print(TAG, e.getMessage());
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey getOrCreateKeyStore() {
        if (!usedOldMethod) {
            try {
                if (secretKey == null && keyStore != null) {
                    final KeyStore.SecretKeyEntry existingKey = (KeyStore.SecretKeyEntry)
                            keyStore.getEntry(generateSecretKey(), null);
                    secretKey = existingKey != null ? existingKey.getSecretKey() : createKey();
                }
            } catch (KeyStoreException | UnrecoverableEntryException | NoSuchAlgorithmException |
                     InvalidAlgorithmParameterException e) {
                QiscusLogger.print(TAG, e.getMessage());
            }
        }
        usedOldMethod = secretKey == null;
        return !usedOldMethod ? secretKey : getOrCreateSecretKey();
    }

    private static synchronized SecretKey getOrCreateSecretKey() {
        if (secretKey == null) {
            usedOldMethod = true;
            secretKey = createSecretKey(generateSecretKey());
        }
        return secretKey;
    }

    public static SecretKey createSecretKey(String key) {
        if (key.length() == KEY_LENGTH) {
            final byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            return new SecretKeySpec(byteKey, ALGORITHM);
        }
        throw new IllegalArgumentException(
                "Key must be " + KEY_LENGTH + " characters long. You're characters long is "
                        + key.length()
        );
    }

    /**
     * Set value to be encrypted
     */
    public static String set(Long value) {
        if (value == null) return null;
        return encrypt(String.valueOf(value));
    }

    public static String set(Integer value) {
        if (value == null) return null;
        return encrypt(String.valueOf(value));
    }

    public static String set(Boolean value) {
        if (value == null) return null;
        return encrypt(String.valueOf(value));
    }

    public static String set(String value) {
        if (value == null) return null;
        else if (value.isEmpty()) return "";
        return encrypt(value);
    }

    /**
     * get value from encrypted value
     */
    public static Long getLong(String encryptedValue) {
        if (encryptedValue == null) return null;
        else if (encryptedValue.isEmpty()) return 0L;
        return Long.parseLong(decrypt(encryptedValue));
    }

    public static Integer getInteger(String encryptedValue) {
        if (encryptedValue == null) return null;
        else if (encryptedValue.isEmpty()) return 0;
        return Integer.parseInt(decrypt(encryptedValue));
    }

    public static Boolean getBoolean(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isEmpty()) return false;
        return Integer.parseInt(decrypt(encryptedValue)) == 1;
    }

    public static String getString(String encryptedValue) {
        if (encryptedValue == null) return null;
        else if (encryptedValue.isEmpty()) return "";
        return decrypt(encryptedValue);
    }

    public static String getString(String encryptedValue, String rawValue) {
        if (encryptedValue == null || encryptedValue.isEmpty()) return rawValue;
        final String result = decrypt(encryptedValue);
        if (result.isEmpty()) return rawValue;
        return result;
    }

}
