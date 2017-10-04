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

import java.lang.reflect.ParameterizedType;

import net.arnx.jsonic.JSON;

// #thinking jflute should I use Gson? And use LastaFlute extension?
/**
 * @author jflute
 */
public class SaJSonicJsonEngine implements SaRealJsonEngine {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean nullToEmptyWriting;

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public SaJSonicJsonEngine asNullToEmptyWriting() {
        this.nullToEmptyWriting = true;
        return this;
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    @Override
    public String toJson(Object bean) {
        if (nullToEmptyWriting) {
            return new JSON() {
                protected Object preformatNull(Context context, java.lang.reflect.Type type) throws Exception {
                    return String.class.equals(type) ? "" : null;
                };
            }.format(bean);
        } else { // normally here
            return JSON.encode(bean);
        }
    }

    @Override
    public <BEAN> BEAN fromJson(String json, Class<BEAN> beanType) {
        return JSON.decode(json, beanType);
    }

    @Override
    public <BEAN> BEAN fromJsonParameterized(String json, ParameterizedType parameterizedType) {
        return JSON.decode(json, parameterizedType);
    }
}
