/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.saflute.core.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.dbflute.saflute.core.security.exception.CipherFailureException;

/**
 * @author jflute
 */
public class InvertibleCipher {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ALGORITHM_AES = "AES";
    public static final String ALGORITHM_BLOWFISH = "Blowfish";
    public static final String ALGORITHM_DES = "DES";
    public static final String ALGORITHM_RSA = "RSA";
    public static final String CHARSET_UTF8 = "UTF-8";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String algorithm;
    protected final SecretKey key;
    protected final String charset;
    protected Cipher encryptoCipher;
    protected Cipher decryptoCipher;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public InvertibleCipher(String algorithm, SecretKey key, String charset) {
        this.algorithm = algorithm;
        this.key = key;
        this.charset = charset;
    }

    public InvertibleCipher(String algorithm, String key, String charset) {
        this.algorithm = algorithm;
        this.key = createKey(key);
        this.charset = charset;
    }

    protected SecretKey createKey(String key) {
        return new SecretKeySpec(key.getBytes(), algorithm);
    }

    public static InvertibleCipher createAesCipher(String key) {
        return new InvertibleCipher(ALGORITHM_AES, key, CHARSET_UTF8);
    }

    public static InvertibleCipher createBlowfishCipher(String key) {
        return new InvertibleCipher(ALGORITHM_BLOWFISH, key, CHARSET_UTF8);
    }

    public static InvertibleCipher createDesCipher(String key) {
        return new InvertibleCipher(ALGORITHM_DES, key, CHARSET_UTF8);
    }

    public static InvertibleCipher createRsaCipher(String key) {
        return new InvertibleCipher(ALGORITHM_RSA, key, CHARSET_UTF8);
    }

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    protected synchronized void initialize() {
        if (encryptoCipher != null) {
            return;
        }
        assertInit();
        doInitializeCipher();
    }

    protected void assertInit() {
        if (this.key == null) {
            throw new IllegalStateException("Not found himitu kagi.");
        }
        if (this.charset == null) {
            throw new IllegalStateException("Not found charset.");
        }
    }

    protected void doInitializeCipher() {
        try {
            encryptoCipher = Cipher.getInstance(algorithm);
            encryptoCipher.init(Cipher.ENCRYPT_MODE, key);
            decryptoCipher = Cipher.getInstance(algorithm);
            decryptoCipher.init(Cipher.DECRYPT_MODE, key);
        } catch (NoSuchAlgorithmException e) {
            throw new CipherFailureException("Failed by unknown algorithm: " + algorithm, e);
        } catch (NoSuchPaddingException e) {
            throw new CipherFailureException("Failed by no such padding: " + algorithm, e);
        } catch (InvalidKeyException e) {
            throw new CipherFailureException("Failed by invalid key. (cannot show it for security)", e);
        }
    }

    // ===================================================================================
    //                                                                     Encrypt/Decrypt
    //                                                                     ===============
    /**
     * Encrypt the text. <br>
     * If the specified text is null or empty, it returns the text without encrypting.
     * @param plainText The plain text to be encrypted. (NullAllowed: if null, returns null)
     * @return The crypted text. (NullAllowed: when the text is null)
     */
    public synchronized String encrypt(String plainText) {
        if (encryptoCipher == null) {
            initialize();
        }
        if (plainText == null || plainText.length() == 0) {
            return plainText;
        }
        return new String(Hex.encodeHex(doEncrypt(plainText)));
    }

    protected byte[] doEncrypt(String plainText) {
        try {
            return encryptoCipher.doFinal(plainText.getBytes(charset));
        } catch (IllegalBlockSizeException e) {
            throw new CipherFailureException("Failed by illegal block size: " + plainText, e);
        } catch (BadPaddingException e) {
            throw new CipherFailureException("Failed by bad padding: " + plainText, e);
        } catch (UnsupportedEncodingException e) {
            throw new CipherFailureException("Failed by unsupported encoding: " + charset, e);
        }
    }

    /**
     * Decrypt the text. <br>
     * If the specified text is null or empty, it returns the text without decrypting.
     * @param cryptedText The crypted text to be decrypted. (NullAllowed: if null, returns null)
     * @return The decrypted text. (NullAllowed: when the text is null)
     */
    public synchronized String decrypt(String cryptedText) {
        if (decryptoCipher == null) {
            initialize();
        }
        if (cryptedText == null || cryptedText.length() == 0) {
            return cryptedText;
        }
        try {
            return new String(doDecrypt(cryptedText), charset);
        } catch (UnsupportedEncodingException e) {
            throw new CipherFailureException("Failed by unsupported encoding: " + charset, e);
        }
    }

    protected byte[] doDecrypt(String cryptedText) {
        try {
            return decryptoCipher.doFinal(Hex.decodeHex(cryptedText.toCharArray()));
        } catch (IllegalBlockSizeException e) {
            throw new CipherFailureException("Failed by illegal block size: " + cryptedText, e);
        } catch (BadPaddingException e) {
            throw new CipherFailureException("Failed by bad padding: " + cryptedText, e);
        } catch (DecoderException e) {
            throw new CipherFailureException("Failed by decode failure: " + cryptedText, e);
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + algorithm + "}"; // don't show secret key for security
    }
}
