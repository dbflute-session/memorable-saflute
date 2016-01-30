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
package org.dbflute.saflute.web.servlet.cookie;

import javax.annotation.Resource;

import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.security.InvertibleCipher;
import org.dbflute.saflute.web.servlet.OptionalServletDirection;

/**
 * @author jflute
 */
public class SimpleCookieCipher implements CookieCipher {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The invertible cipher for cookie. (NotNull: after initialization) */
    protected InvertibleCipher invertibleCipher;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        final OptionalServletDirection direction = getOptionalServletDirection();
        CookieResourceProvider provider = direction.assistCookieResourceProvider();
        invertibleCipher = provider.provideCipher();
        // no logging here because cookie manager do it
    }

    protected OptionalServletDirection getOptionalServletDirection() {
        return assistantDirector.assistOptionalServletDirection();
    }

    // ===================================================================================
    //                                                                     Encrypt/Decrypt
    //                                                                     ===============
    public String encrypt(String plainText) {
        return invertibleCipher.encrypt(plainText);
    }

    public String decrypt(String cryptedText) {
        return invertibleCipher.decrypt(cryptedText);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + invertibleCipher + "}";
    }
}
