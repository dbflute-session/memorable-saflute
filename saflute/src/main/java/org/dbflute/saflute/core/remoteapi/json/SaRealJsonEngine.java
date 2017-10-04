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

/**
 * @author jflute
 */
public interface SaRealJsonEngine {

    /**
     * @param bean The instance of bean to encode. (NotNull)
     * @return The encoded JSON string. (NotNull)
     */
    String toJson(Object bean);

    /**
     * @param <BEAN> The type of bean.
     * @param json The string of JSON to be parsed. (NotNull)
     * @param beanType The type of bean to convert. (NotNull)
     * @return The new-created bean that has the JSON values. (NotNull)
     */
    <BEAN> BEAN fromJson(String json, Class<BEAN> beanType);

    /**
     * @param <BEAN> The type of bean.
     * @param json The string of JSON to be parsed. (NotNull)
     * @param parameterizedType The parameterized type of bean to convert. (NotNull)
     * @return The new-created bean that has the JSON values. (NotNull)
     */
    <BEAN> BEAN fromJsonParameterized(String json, ParameterizedType parameterizedType);
}
