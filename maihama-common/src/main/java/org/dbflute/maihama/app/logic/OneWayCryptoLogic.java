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
package org.dbflute.maihama.app.logic;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author jflute
 */
public class OneWayCryptoLogic {

    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // *very simple implementation here
    // so you should re-implement this as new logic for your project
    // and delete this comment after your implementation by jflute (2014/12/24)
    // _/_/_/_/_/_/_/_/_/_/

    private static final String ENCODING = "UTF-8";
    private static final String ALGORITHM = "SHA-256";

    public String encrypt(String plainText) {
        final String encoding = ENCODING;
        final MessageDigest digest = createDigest();
        try {
            digest.update(plainText.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            String msg = "Unknown encoding: " + encoding;
            throw new IllegalStateException(msg);
        }
        return convertToCryptoString(digest.digest());
    }

    protected MessageDigest createDigest() {
        final String algorithm = ALGORITHM;
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            String msg = "Failed to get instance of digest: " + algorithm;
            throw new IllegalStateException(msg, e);
        }
    }

    protected String convertToCryptoString(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte bt : bytes) {
            sb.append(String.format("%02x", bt));
        }
        return sb.toString();
    }
}
