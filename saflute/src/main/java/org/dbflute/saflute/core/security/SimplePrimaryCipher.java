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

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.direction.OptionalCoreDirection;
import org.dbflute.saflute.core.direction.exception.FwRequiredAssistNotFoundException;

/**
 * @author jflute
 */
public class SimplePrimaryCipher implements PrimaryCipher {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(SimplePrimaryCipher.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The invertible cipher for primary values. (NotNull: after initialization) */
    protected InvertibleCipher invertibleCipher;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        final OptionalCoreDirection direction = getOptionalCoreDirection();
        final SecurityResourceProvider provider = direction.assistSecurityResourceProvider();
        invertibleCipher = provider.providePrimaryInvertibleCipher();
        if (invertibleCipher == null) {
            String msg = "The provider returned null invertible cipher: " + provider;
            throw new FwRequiredAssistNotFoundException(msg);
        }
        showBootLogging();
    }

    protected OptionalCoreDirection getOptionalCoreDirection() {
        return assistantDirector.assistOptionalCoreDirection();
    }

    protected void showBootLogging() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Primary Cipher]");
            LOG.info(" invertibleCipher: " + invertibleCipher);
        }
    }

    // ===================================================================================
    //                                                                     Encrypt/Decrypt
    //                                                                     ===============
    /**
     * {@inheritDoc}
     */
    public String encrypt(String plainText) {
        return invertibleCipher.encrypt(plainText);
    }

    /**
     * {@inheritDoc}
     */
    public String decrypt(String cryptedText) {
        return invertibleCipher.decrypt(cryptedText);
    }
}
