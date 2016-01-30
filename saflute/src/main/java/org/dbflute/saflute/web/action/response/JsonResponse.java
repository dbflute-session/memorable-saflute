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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dbflute.saflute.web.action.interceptor.RomanticActionCustomizer;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class JsonResponse implements ApiResponse {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final Object DUMMY = new Object();
    protected static final JsonResponse INSTANCE_OF_EMPTY = new JsonResponse(DUMMY).emptyResponse();
    protected static final JsonResponse INSTANCE_OF_SKIP = new JsonResponse(DUMMY).skipResponse();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Object jsonObj;
    protected String callback;
    protected Map<String, String> headerMap; // lazy loaded (for when no use)
    protected boolean forcedlyJavaScript;
    protected boolean emptyResponse;
    protected boolean skipResponse;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Construct JSON response. <br>
     * This needs {@link RomanticActionCustomizer} in your customizer.dicon.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. normal JSON response</span>
     * return new JsonResponse(bean);
     * 
     * <span style="color: #3F7E5E">// e.g. JSONP response</span>
     * return new JsonResponse(bean).asJsonp("callback");
     * </pre>
     * @param jsonObj The JSON object to send response. (NotNull)
     */
    public JsonResponse(Object jsonObj) {
        if (jsonObj == null) {
            throw new IllegalArgumentException("The argument 'jsonObj' should not be null.");
        }
        this.jsonObj = jsonObj;
    }

    // ===================================================================================
    //                                                                              Header
    //                                                                              ======
    /** {@inheritDoc} */
    public void header(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument 'value' should not be null.");
        }
        prepareHeaderMap().put(name, value);
    }

    /** {@inheritDoc} */
    public Map<String, String> getHeaderMap() {
        return headerMap != null ? Collections.unmodifiableMap(headerMap) : DfCollectionUtil.emptyMap();
    }

    protected Map<String, String> prepareHeaderMap() {
        if (headerMap == null) {
            headerMap = new LinkedHashMap<String, String>(4);
        }
        return headerMap;
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public JsonResponse asJsonp(String callback) {
        this.callback = callback;
        return this;
    }

    public JsonResponse forcedlyJavaScript() {
        forcedlyJavaScript = true;
        return this;
    }

    public static JsonResponse empty() { // user interface
        return INSTANCE_OF_EMPTY;
    }

    protected JsonResponse emptyResponse() { // internal use
        emptyResponse = true;
        return this;
    }

    public boolean isEmptyResponse() { // for framework
        return emptyResponse;
    }

    public static JsonResponse skip() { // user interface
        return INSTANCE_OF_SKIP;
    }

    protected JsonResponse skipResponse() { // internal use
        skipResponse = true;
        return this;
    }

    public boolean isSkipResponse() { // for framework
        return skipResponse;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String classTitle = DfTypeUtil.toClassTitle(this);
        final String jsonExp = jsonObj != null ? DfTypeUtil.toClassTitle(jsonObj) : null;
        return classTitle + ":{" + jsonExp + ", " + callback + ", " + forcedlyJavaScript + ", " + emptyResponse + ", " + skipResponse + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Object getJsonObj() {
        return jsonObj;
    }

    public String getCallback() {
        return callback;
    }

    public boolean isForcedlyJavaScript() {
        return forcedlyJavaScript;
    }
}
