/*
 * Copyright 2015-2017 the original author or authors.
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
package org.dbflute.saflute.core.remoteapi.json;

import org.dbflute.saflute.core.direction.AccessibleConfig;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.lastaflute.core.json.JsonMappingOption;
import org.lastaflute.core.json.engine.GsonJsonEngine;
import org.lastaflute.core.json.engine.RealJsonEngine;

import com.google.gson.GsonBuilder;

/**
 * @author jflute
 */
public class LastalikeJsonEngineFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean nullsSuppressed;
    protected boolean prettyPrintSuppressed;

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public LastalikeJsonEngineFactory suppressNulls() {
        nullsSuppressed = true;
        return this;
    }

    public LastalikeJsonEngineFactory suppressPrettyPrint() {
        prettyPrintSuppressed = true;
        return this;
    }

    // ===================================================================================
    //                                                                              Create
    //                                                                              ======
    public RealJsonEngine create(JsonMappingOption mappingOption) { // like LastaFlute JsonManager
        final boolean serializeNulls = !nullsSuppressed;
        final boolean prettyPrinting = !prettyPrintSuppressed && isDevelopmentHere();
        return new GsonJsonEngine(builder -> {
            setupSerializeNullsSettings(builder, serializeNulls);
            setupPrettyPrintingSettings(builder, prettyPrinting);
        }, op -> {
            op.acceptAnother(mappingOption);
        });
    }

    protected void setupSerializeNullsSettings(GsonBuilder builder, boolean serializeNulls) {
        if (serializeNulls) {
            builder.serializeNulls();
        }
    }

    protected void setupPrettyPrintingSettings(GsonBuilder builder, boolean prettyPrinting) {
        if (prettyPrinting) {
            builder.setPrettyPrinting();
        }
    }

    protected boolean isDevelopmentHere() { // #thinking all right? by jflute
        final AccessibleConfig config = ContainerUtil.getComponent(AccessibleConfig.class);
        return config.is("development.here");
    }
}
