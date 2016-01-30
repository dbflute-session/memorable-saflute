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
package org.dbflute.saflute.web.action.response;

import java.util.Map;

import org.dbflute.saflute.web.action.interceptor.RomanticActionCustomizer;

/**
 * The response type of action return. <br>
 * You can define the type as execute method of action
 * if you set {@link RomanticActionCustomizer} in your customizer.dicon.
 * @author jflute
 */
public interface ActionResponse {

    /**
     * @param name The name of header. (NotNull)
     * @param value The value of header. (NotNull)
     */
    void header(String name, String value);

    /**
     * @return The read-only map for headers, map:{header-name = header-value}. (NotNull)
     */
    Map<String, String> getHeaderMap();
}
