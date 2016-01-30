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
package org.dbflute.saflute.core.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jflute
 */
public class MessageResourceHolder {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(MessageResourceHolder.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The instance of gateway. (NotNull: if accepted) */
    protected MessageResourceGateway gateway;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        // empty for now
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    /**
     * Accept the gateway for message resources of e.g. Struts. <br>
     * You should call this immediately after your application is initialized. <br>
     * And only one setting is allowed.
     * @param specified The instance of gateway. (NotNull)
     */
    public void acceptGateway(MessageResourceGateway specified) {
        LOG.info("...Accepting the gateway of message resources: " + specified);
        if (specified == null) {
            String msg = "The argument 'specified' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (gateway != null) {
            String msg = "The gateway already exists: existing=" + gateway + " specified=" + specified;
            throw new IllegalStateException(msg);
        }
        gateway = specified;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + gateway + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the gateway for message resources of e.g. Struts.
     * @return The instance of gateway. (NotNull: if accepted)
     */
    public MessageResourceGateway getGateway() {
        return gateway;
    }
}
