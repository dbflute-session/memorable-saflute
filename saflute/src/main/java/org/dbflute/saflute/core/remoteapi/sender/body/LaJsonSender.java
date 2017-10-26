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
package org.dbflute.saflute.core.remoteapi.sender.body;

import org.dbflute.remoteapi.sender.body.FlJsonSender;
import org.dbflute.saflute.core.remoteapi.supplement.LastalikeJsonEngineFactory;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.lastaflute.core.json.JsonMappingOption;
import org.lastaflute.core.json.engine.RealJsonEngine;

/**
 * @author jflute
 */
public class LaJsonSender extends FlJsonSender {

    protected final RealJsonEngine jsonEngine; // to parse JSON response and request as JsonBody

    // actually requestManager is unneeded here, but for migration
    public LaJsonSender(RequestManager requestManager, JsonMappingOption mappingOption) {
        this.jsonEngine = createLastalikeJsonEngineFactory().create(mappingOption);
    }

    protected LastalikeJsonEngineFactory createLastalikeJsonEngineFactory() {
        return new LastalikeJsonEngineFactory();
    }

    @Override
    protected String toJson(Object param) {
        return jsonEngine.toJson(param);
    }
}
