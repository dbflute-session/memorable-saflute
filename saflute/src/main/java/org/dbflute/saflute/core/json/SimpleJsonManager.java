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
package org.dbflute.saflute.core.json;

import javax.annotation.Resource;

import net.arnx.jsonic.JSON;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.direction.OptionalCoreDirection;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class SimpleJsonManager implements JsonManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(SimpleJsonManager.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The real parser of JSON. (NotNull: after initialization) */
    protected RealJsonParser realJsonParser;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        final OptionalCoreDirection direction = assistOptionalCoreDirection();
        final JsonResourceProvider provider = direction.assistJsonResourceProvider();
        final RealJsonParser provided = provider != null ? provider.provideJsonParser() : null;
        realJsonParser = provided != null ? provided : createDefaultJsonParser();
        showBootLogging();
    }

    protected OptionalCoreDirection assistOptionalCoreDirection() {
        return assistantDirector.assistOptionalCoreDirection();
    }

    protected RealJsonParser createDefaultJsonParser() {
        return new RealJsonParser() {
            public <BEAN> BEAN parseJson(String json, Class<BEAN> beanType) {
                return JSON.decode(json, beanType);
            }

            public String convertToJson(Object bean) {
                return JSON.encode(bean);
            }
        };
    }

    protected void showBootLogging() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[JSON Manager]");
            LOG.info(" realJsonParser: " + DfTypeUtil.toClassTitle(realJsonParser));
        }
    }

    // ===================================================================================
    //                                                                       Parse/Convert
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public <BEAN> BEAN parseJson(String json, Class<BEAN> beanType) {
        return realJsonParser.parseJson(json, beanType);
    }

    /**
     * {@inheritDoc}
     */
    public String convertToJson(Object bean) {
        return realJsonParser.convertToJson(bean);
    }
}