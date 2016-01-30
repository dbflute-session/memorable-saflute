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

/**
 * @author jflute
 */
public interface PrimaryCipher {

    /**
     * Encrypt the text. <br>
     * If the specified text is null or empty, it returns the text without encrypting.
     * @param plainText The plain text to be encrypted. (NullAllowed: if null, returns null)
     * @return The crypted text. (NullAllowed: when the text is null)
     */
    String encrypt(String plainText);

    /**
     * Decrypt the text. <br>
     * If the specified text is null or empty, it returns the text without decrypting.
     * @param cryptedText The crypted text to be decrypted. (NullAllowed: if null, returns null)
     * @return The decrypted text. (NullAllowed: when the text is null)
     */
    String decrypt(String cryptedText);
}
