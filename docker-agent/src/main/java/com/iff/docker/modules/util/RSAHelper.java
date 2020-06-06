/*******************************************************************************
 * Copyright (c) 2015-4-7 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation
 ******************************************************************************/
package com.iff.docker.modules.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * this code copy from:
 * <p>
 * Title: RSAHelper
 * DO NOT use the *ByDefaultKey method to encode or decode the data if you need the data safety and persistent.
 * the *ByDefaultKey method using to encrypt the data temporary.
 * </p>
 * <p>Description: Utility class that helps encrypt and decrypt strings using RSA algorithm</p>
 *
 * @author Aviran Mordo http://aviran.mordos.com
 * @version 1.0
 */
public class RSAHelper {
    protected static final String ALGORITHM = "RSA";

    /**
     * Init java security to add BouncyCastle as an RSA provider
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generate key which contains a pair of privae and public key using 1024 bytes
     *
     * @return key pair
     */
    public static KeyPair generateKey() {
        KeyPair key = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            keyGen.initialize(1024, new SecureRandom());
            key = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("RSAHelper.generateKey", e);
        }
        return key;
    }

    private static String fromBytes(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
        }
        return null;
    }

    private static byte[] toBytes(String value) {
        try {
            return value == null ? null : value.getBytes("UTF-8");
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Generates Public Key from bytes
     *
     * @param key hex encoded string which represents the key
     * @return The PublicKey
     */
    public static PublicKey getPublicKeyFromBytes(byte[] key) {
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(key);
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new RuntimeException("RSAHelper.getPublicKeyFromBytes", e);
        }
        return publicKey;
    }

    /**
     * Generates Public Key from Hex encoded string
     *
     * @param key hex encoded string which represents the key
     * @return The PublicKey
     */
    public static PublicKey getPublicKeyFromHex(String key) {
        return getPublicKeyFromBytes(Hex.decode(key));
    }

    /**
     * Encrypt a text using public key.
     *
     * @param text The original unencrypted text
     * @param key  The public key
     * @return Encrypted text
     */
    public static byte[] encrypt(byte[] text, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
            // encrypt the plaintext using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text);
        } catch (Exception e) {
            throw new RuntimeException("RSAHelper.encrypt", e);
        }
        return cipherText;
    }

    /**
     * Encrypt a text using public key.
     *
     * @param text The original unencrypted text
     * @param key  The public key
     * @return Encrypted text
     */
    public static String encryptToHex(byte[] text, PublicKey key) {
        return Hex.toHexString(encrypt(text, key));
    }

    /**
     * Encrypt a text using public key.
     *
     * @param text The original unencrypted text
     * @param key  The public key
     * @return Encrypted text
     */
    public static String encryptToHex(byte[] text, String key) {
        return Hex.toHexString(encrypt(text, getPublicKeyFromHex(key)));
    }

    /**
     * Encrypt a text using public key.
     *
     * @param text The original unencrypted text
     * @param key  The public key
     * @return Encrypted text
     */
    public static String encryptToHex(String text, String key) {
        return Hex.toHexString(encrypt(toBytes(text), getPublicKeyFromHex(key)));
    }

    /**
     * Decrypt text using private key
     *
     * @param text The encrypted text
     * @param key  The private key
     * @return The unencrypted text
     */
    public static byte[] decrypt(byte[] text, PrivateKey key) {
        byte[] dectyptedText = null;
        try {
            // decrypt the text using the private key
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, key);
            dectyptedText = cipher.doFinal(text);
        } catch (Exception e) {
            throw new RuntimeException("RSAHelper.decrypt", e);
        }
        return dectyptedText;
    }

    /**
     * Decrypt hex encoded text using private key
     *
     * @param text The encrypted text, encoded as hex
     * @param key  The private key
     * @return The unencrypted text encoded as UTF8
     */
    public static String decryptFromHex(String text, PrivateKey key) {
        return fromBytes(decrypt(Hex.decode(text), key));
    }

    /**
     * Decrypt hex encoded text using private key
     *
     * @param text The encrypted text, encoded as hex
     * @param key  The private key
     * @return The unencrypted text encoded as UTF8
     */
    public static String decryptFromHex(String text, String key) {
        return fromBytes(decrypt(Hex.decode(text), getPrivateKeyFromHex(key)));
    }

    /**
     * Convert a Key to string encoded as BASE64
     *
     * @param key The key (private or public)
     * @return A string representation of the key
     */
    public static String getKeyAsHex(Key key) {
        return Hex.toHexString(key.getEncoded());
    }

    /**
     * Generates Private Key from Hex encoded string
     *
     * @param key Hex encoded string which represents the key
     * @return The PrivateKey
     */
    public static PrivateKey getPrivateKeyFromHex(String key) {
        return getPrivateKeyFromBytes(Hex.decode(key));
    }

    /**
     * Generates Private Key from bytes
     *
     * @param bytes Hex encoded string which represents the key
     * @return The PrivateKey
     */
    public static PrivateKey getPrivateKeyFromBytes(byte[] bytes) {
        PrivateKey privateKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (Exception e) {
            throw new RuntimeException("RSAHelper.getPrivateKeyFromBytes", e);
        }
        return privateKey;
    }

    /**
     * Encrypt file using 1024 RSA encryption
     *
     * @param srcFileName  Source file name
     * @param destFileName Destination file name
     * @param key          The key. For encryption this is the Private Key and for decryption this is the public key
     */
    public static void encryptFile(String srcFileName, String destFileName, PublicKey key) {
        encryptDecryptFile(srcFileName, destFileName, key, Cipher.ENCRYPT_MODE);
    }

    /**
     * Decrypt file using 1024 RSA encryption
     *
     * @param srcFileName  Source file name
     * @param destFileName Destination file name
     * @param key          The key. For encryption this is the Private Key and for decryption this is the public key
     */
    public static void decryptFile(String srcFileName, String destFileName, PrivateKey key) {
        encryptDecryptFile(srcFileName, destFileName, key, Cipher.DECRYPT_MODE);
    }

    /**
     * Encrypt and Decrypt files using 1024 RSA encryption
     *
     * @param srcFileName  Source file name
     * @param destFileName Destination file name
     * @param key          The key. For encryption this is the Private Key and for decryption this is the public key
     * @param cipherMode   Cipher Mode
     */
    public static void encryptDecryptFile(String srcFileName, String destFileName, Key key, int cipherMode) {
        OutputStream outputWriter = null;
        InputStream inputReader = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
            //RSA encryption data size limitations are slightly less than the key modulus size,
            //depending on the actual padding scheme used (e.g. with 1024 bit (128 byte) RSA key,
            //the size limit is 117 bytes for PKCS#1 v 1.5 padding. (http://www.jensign.com/JavaScience/dotnet/RSAEncrypt/)
            byte[] buf = cipherMode == Cipher.ENCRYPT_MODE ? new byte[100] : new byte[128];
            int bufl;
            // init the Cipher object for Encryption...
            cipher.init(cipherMode, key);
            // start FileIO
            outputWriter = new FileOutputStream(destFileName);
            inputReader = new FileInputStream(srcFileName);
            while ((bufl = inputReader.read(buf)) != -1) {
                byte[] encText = null;
                if (cipherMode == Cipher.ENCRYPT_MODE) {
                    encText = encrypt(copyBytes(buf, bufl), (PublicKey) key);
                } else {
                    encText = decrypt(copyBytes(buf, bufl), (PrivateKey) key);
                }
                outputWriter.write(encText);
            }
            outputWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException("RSAHelper.encryptDecryptFile", e);
        } finally {
            try {
                if (outputWriter != null) {
                    outputWriter.close();
                }
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (Exception e) {
                // do nothing...
            } // end of inner try, catch (Exception)...
        }
    }

    public static byte[] copyBytes(byte[] arr, int length) {
        byte[] newArr = null;
        if (arr == null || arr.length == length) {
            newArr = arr;
        } else {
            newArr = new byte[length];
            for (int i = 0; i < length; i++) {
                newArr[i] = (byte) arr[i];
            }
        }
        return newArr;
    }

    public static void main(String[] args) {
        KeyPair kp = generateKey();
        String pubKey = getKeyAsHex(kp.getPublic());
        String priKey = getKeyAsHex(kp.getPrivate());
        System.out.println("PublicKey:" + pubKey);
        System.out.println("Prikey:" + priKey);
        String encrypt = encryptToHex("iff", pubKey);
        System.out.println(encrypt);
        String decrypt = decryptFromHex(encrypt, priKey);
        System.out.println(decrypt);
    }
}